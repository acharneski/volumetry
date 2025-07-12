package com.simiacryptus.probabilityModel.rules.metrics;

import com.simiacryptus.data.VolumeMetric;
import com.simiacryptus.lang.LOG;
import com.simiacryptus.probabilityModel.ModelSampler;
import com.simiacryptus.probabilityModel.model.DistributionModel.BinaryNodeFunction;
import com.simiacryptus.probabilityModel.model.PointModel;
import com.simiacryptus.probabilityModel.model.ScalarModel;
import com.simiacryptus.probabilityModel.rules.MetricRule;
import com.simiacryptus.probabilityModel.visitors.ModelPartitioner;
import com.simiacryptus.probabilityModel.volume.DoubleVolume;

import java.util.ArrayList;
import java.util.Random;

public class MetricSurface
{
  
  public static int        DEBUG                 = 1;
  
  private final Metric     metric;
  private final PointModel upperModel;
  private final PointModel lowerModel;
  private Random           random                = new Random();
  private int              modelUpdateFrequency  = 10000;
  
  private final double     maxThreshold;
  private final double     minThreshold;
  private final double     centralValue;
  
  private final double     probabilitySmoothness = 5;
  private final boolean    volumeSmooth          = true;
  
  public MetricSurface(final MetricRule rule, final double value, final double tolerance)
  {
    this.centralValue = value;
    this.maxThreshold = value * (1 + tolerance);
    this.minThreshold = value * (1 - tolerance);
    this.metric = rule.metric;
    
    final DoubleVolume bounds = rule.range.getBounds();
    this.lowerModel = new PointModel(bounds);
    this.updateModel(lowerModel);
    
    this.upperModel = new PointModel(bounds);
    this.updateModel(upperModel);
  }
  
  public void add(final double[][] points)
  {
    for (final double[] point : points)
    {
      add(point);
    }
  }
  
  public double add(final double[] point)
  {
    final double evaluate = this.metric.evaluate(point);
    getModel(evaluate).addDataPoint(point);
    return evaluate;
  }
  
  private PointModel getModel(final double evaluate)
  {
    final PointModel model;
    if (evaluate < centralValue)
    {
      model = lowerModel;
    }
    else
    {
      model = upperModel;
    }
    return model;
  }
  
  public double[][] findBorderPoints(final int size)
  {
    final ArrayList<double[]> list = new ArrayList<double[]>();
    while (list.size() < size)
    {
      ModelSampler modelDistribution = new ModelSampler(getBorderModel());
      double totalSqError = 0;
      for (double[] point : modelDistribution.sample(getBatchSize(), random))
      {
        final double eval = add(point);
        totalSqError += Math.pow((eval - this.centralValue) / this.centralValue, 2);
        if (eval < this.maxThreshold && eval > minThreshold)
        {
          list.add(point);
        }
      }
      LOG.d("RMS Error: %s", Math.sqrt(totalSqError / modelUpdateFrequency));
    }
    return list.toArray(new double[][] {});
  }
  
  private ScalarModel getBorderModel()
  {
    this.updateModel(lowerModel);
    this.updateModel(upperModel);
    final ScalarModel probabilityModel = lowerModel.evaluate(new BinaryNodeFunction() {
      @Override
      public double evaluate(double left, double right, VolumeMetric volume)
      {
        final double v;
        if (left > 0 && right > 0)
        {
          v = 1;
        }
        else if (left == 0 && right == 0)
        {
          v = 1;
        }
        else
        {
          double volumeSmoothness = probabilitySmoothness;
          if (volumeSmooth) volumeSmoothness *= volume.value;
          if (left == 0) v = volumeSmoothness / (right + 2. * volumeSmoothness);
          else if (right == 0) v = volumeSmoothness / (left + 2. * volumeSmoothness);
          else throw new RuntimeException();
        }
        return v * volume.value;
      }
    }, upperModel);
    return probabilityModel;
  }
  
  public int getBatchSize()
  {
    return this.modelUpdateFrequency;
  }
  
  private void updateModel(final PointModel model)
  {
    if (0 == model.getDataPoints().size()) return;
    new ModelPartitioner(new RoundRobinRuleGenerator()).setMinPointThreshold(10).setRewriteRules(false).visit(model, Integer.MAX_VALUE);
    if (DEBUG > 0)
    {
      LOG.d("Updated model (%s nodes; %s points)", model.getNodeCount(), model.getWeight());
    }
  }

}
