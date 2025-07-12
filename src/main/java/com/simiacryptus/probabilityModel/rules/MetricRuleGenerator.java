package com.simiacryptus.probabilityModel.rules;

import com.simiacryptus.data.DoubleRange;
import com.simiacryptus.data.VolumeMetric;
import com.simiacryptus.lang.LOG;
import com.simiacryptus.probabilityModel.model.PointNode;
import com.simiacryptus.probabilityModel.optimizer.IndexScanOptimizer;
import com.simiacryptus.probabilityModel.optimizer.OptimizerResult;
import com.simiacryptus.probabilityModel.rules.fitness.IndexFitnessFunction;
import com.simiacryptus.probabilityModel.rules.fitness.SplitFitness;
import com.simiacryptus.probabilityModel.rules.metrics.DimensionMetric;
import com.simiacryptus.probabilityModel.rules.metrics.Metric;
import com.simiacryptus.probabilityModel.util.JvmArrays;
import com.simiacryptus.probabilityModel.volume.DoubleVolume;
import com.simiacryptus.probabilityModel.volume.SpacialVolume;

import java.util.*;

public class MetricRuleGenerator implements RuleGenerator
{
  public static class VolumeInfo
  {
    final double[] sortedVolumes;
    final double   totalVolume;
    
    public VolumeInfo(final double[] sortedVolumes, final double totalVolume)
    {
      super();
      this.sortedVolumes = sortedVolumes;
      this.totalVolume = totalVolume;
    }
  }
  
  public static int          DEBUG       = 0;
  private final SplitFitness splitFitness;
  
  private int                splitPoints = 1;
  
  public MetricRuleGenerator(final SplitFitness splitFitness)
  {
    this.splitFitness = splitFitness;
  }
  
  private RuleCandidate getCandidate(final PointNode node, final Metric metric, final double[] sortedMetricValues,
      final double[] sortedMetricVolumes, final double totalVolume)
  {
    final IndexFitnessFunction function = new IndexFitnessFunction(this.splitFitness, totalVolume, sortedMetricValues, sortedMetricVolumes);
    final IndexScanOptimizer optimizer = new IndexScanOptimizer(this.getPartitions(), sortedMetricValues.length);
    final OptimizerResult<int[], Double> optimizeResult = optimizer.optimize(node, function);
    final double[] metricValue = JvmArrays.lookup(optimizeResult.parameter, sortedMetricValues);
    final PartitionRule rule = getRule(node, metric, metricValue);
    for(SpacialVolume sv : rule.getSubVolumes())
    {
      // TODO: This is a hack to shortcut debugging PCA rule generation; debug me properly! 
      if(null == sv) return null;
    }
    final RuleCandidate candidate = new RuleCandidate(rule, optimizeResult.result);
    candidate.detail = function.getVolumeDataDensities(optimizeResult.parameter);
    return candidate;
  }

  protected PartitionRule getRule(final PointNode node, final Metric metric, final double[] metricValue)
  {
    return new MetricRule(node.getUnboundableRegion(), metric, metricValue);
  }
  
  public Collection<? extends Metric> getCandidateMetrics(final PointNode node)
  {
    final SpacialVolume region = node.getRegion();
    final ArrayList<Metric> metrics = new ArrayList<Metric>();
    final int dimensions = region.dimensions();
    for (int dimension = 0; dimension < dimensions; dimension++)
    {
      metrics.add(new DimensionMetric(dimension));
    }
    return metrics;
  }
  
  public int getPartitions()
  {
    return this.getSplitPoints();
  }
  
  public RuleCandidate getRuleCandidate(final PointNode node, final Metric metric)
  {
    double[][] dataPoints = node.getDataPoints().toArray(new double[][] {});
    final double[] sortedMetricValues = this.getSortedMetricValues(dataPoints, metric);
    final VolumeInfo metricVolumes = this.getSortedMetricVolumes(node.getRegion(), metric, sortedMetricValues);
    if (null == metricVolumes)
    {
      return null;
    }
    //TestUtil.openJson(JsonConverter.toJson(node, Integer.MAX_VALUE));
    return this.getCandidate(
        node, metric, sortedMetricValues, 
        metricVolumes.sortedVolumes, metricVolumes.totalVolume);
  }
  
  protected double[] getSortedMetricValues(final double[][] dataPoints, final Metric metric)
  {
    final double[] metricValues = metric.evaluate(dataPoints);
    final int[] metricValueIndex = new int[dataPoints.length];
    for (int i = 0; i < metricValues.length; i++)
    {
      metricValueIndex[i] = i;
    }
    JvmArrays.sort(metricValueIndex, new Comparator<Integer>() {
      @Override
      public int compare(final Integer o1, final Integer o2)
      {
        return Double.compare(metricValues[o1], metricValues[o2]);
      }
    });
    final double[] sortedMetricValues = JvmArrays.lookup(metricValueIndex, metricValues);
    return sortedMetricValues;
  }
  
  protected VolumeInfo getSortedMetricVolumes(final SpacialVolume region, final Metric metric, final double[] sortedValues)
  {
    if (metric instanceof DimensionMetric && region instanceof DoubleVolume)
    {
      final DimensionMetric dimensionMetric = (DimensionMetric) metric;
      final DoubleVolume volume = new DoubleVolume((DoubleVolume)region);
      return getSortedVolumes_continuous(volume, dimensionMetric, sortedValues);
    }
    else
    {
      return getSortedVolumes_montecarlo(region, metric, sortedValues);
    }
  }

  private VolumeInfo getSortedVolumes_montecarlo(final SpacialVolume region, final Metric metric, final double[] sortedValues)
  {
    final double[] sortedVolumes = new double[sortedValues.length];
    int totalPoints = sortedValues.length;
    final Iterator<double[]> iterator = region.points().iterator();
    for (int i = 0; i < totalPoints; i++)
    {
      final double[] point = iterator.next();
      if (null == point)
      {
        return null;
      }
      assert(region.contains(point));
      final double value = metric.evaluate(point);
      int index = Arrays.binarySearch(sortedValues, value);
      if (0 > index)
      {
        index = -index;
      }
      if (index < sortedVolumes.length)
      {
        sortedVolumes[index]++;
      }
    }
    for (int i = 1; i < sortedValues.length; i++)
    {
      if (0 == sortedVolumes[i])
      {
        sortedVolumes[i] = 1;
        totalPoints++;
      }
      sortedVolumes[i] += sortedVolumes[i - 1];
      assert 0 == i || sortedVolumes[i] > sortedVolumes[i - 1];
    }
    return new VolumeInfo(sortedVolumes, totalPoints);
  }

  private VolumeInfo getSortedVolumes_continuous(final DoubleVolume region, final DimensionMetric metric, final double[] sortedValues)
  {
    final double[] sortedVolumes = new double[sortedValues.length];
    final int dimensions = region.dimensions();
    DoubleRange dimRange = region.get(metric.dimension);
    final double minValue = sortedValues[0];
    final double maxValue = sortedValues[sortedValues.length-1];
    if(dimRange.from == -Double.MAX_VALUE)
    {
      dimRange = new DoubleRange(minValue - (maxValue - minValue), dimRange.to);
    }
    if(dimRange.to == Double.MAX_VALUE)
    {
      dimRange = new DoubleRange(dimRange.from, maxValue + (maxValue - minValue));
    }
    for (int i = 0; i < sortedValues.length; i++)
    {
      final double to = sortedValues[i];
      final double from = -Double.MAX_VALUE;
      final DoubleRange intersectRange = new DoubleRange(from, to).intersect(dimRange);
      final double value;
      if(null != intersectRange)
      {
        region.set(metric.dimension, intersectRange);
        VolumeMetric volumeMetric = region.getVolume();
        value = volumeMetric.dimension < dimensions ? 0. : volumeMetric.value;
      }
      else
      {
        value = 0;
      }
      sortedVolumes[i] = value;
      assert 0 == i || sortedVolumes[i] >= sortedVolumes[i - 1];
    }
    region.set(metric.dimension, dimRange);
    return new VolumeInfo(sortedVolumes, region.getVolume().value);
  }
  
  @Override
  public int getSplitPoints()
  {
    return this.splitPoints;
  }

  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append(getClass().getSimpleName());
    builder.append("[");
    builder.append(splitFitness);
    builder.append("]");
    
//    builder.append("@");
//    builder.append(Integer.toHexString(System.identityHashCode(this)));
    return builder.toString();
  }

  @Override
  public PartitionRule getRule(PointNode node)
  {
    final TreeSet<RuleCandidate> candidates = new TreeSet<RuleCandidate>();
    for (final Metric metric : this.getCandidateMetrics(node))
    {
      final RuleCandidate ruleCandidate = this.getRuleCandidate(node, metric);
      if (null == ruleCandidate)
      {
        continue;
      }
      // if (ruleCandidate.rule.metric.minFitness > ruleCandidate.fitness) continue;
      candidates.add(ruleCandidate);
    }
    if (0 == candidates.size())
    {
      return null;
    }
    final RuleCandidate bestCandidate = candidates.last();
    if (DEBUG > 1)
    {
      LOG.d("Calcualted rule for %s points with fitness %s: %s", node.getDataPoints().size(), bestCandidate.fitness, bestCandidate);
      if (DEBUG > 2)
      {
        try
        {
          LOG.d("Space: %s", node.getRegion().toJson().toString(2));
          for (final RuleCandidate c : candidates)
          {
            LOG.d("Candidate: %s", c);
          }
        }
        catch (final Exception e)
        {
          e.printStackTrace();
        }
      }
    }
    else if (DEBUG > 0)
    {
      LOG.d("Calcualted rule for %s points with fitness %s: %s", node.getDataPoints().size(), bestCandidate.fitness, bestCandidate.rule);
    }
    return bestCandidate.rule;
  }
  
  
}