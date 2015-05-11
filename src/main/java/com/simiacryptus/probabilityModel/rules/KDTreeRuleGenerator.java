package com.simiacryptus.probabilityModel.rules;

import java.util.ArrayList;
import java.util.Collection;

import com.simiacryptus.probabilityModel.kdtree.KDTree;
import com.simiacryptus.probabilityModel.model.PointNode;
import com.simiacryptus.probabilityModel.rules.fitness.SplitFitness;
import com.simiacryptus.probabilityModel.rules.metrics.KDTreeMetric;
import com.simiacryptus.probabilityModel.rules.metrics.Metric;

public class KDTreeRuleGenerator extends MetricRuleGenerator
{
  
  private final KDTree kdTree;
  
  public KDTreeRuleGenerator(final SplitFitness splitFitness, final double[][] dataPoints)
  {
    super(splitFitness);
    this.kdTree = new KDTree(dataPoints);
  }
  
  @Override
  public Collection<Metric> getCandidateMetrics(PointNode node)
  {
    @SuppressWarnings("unchecked")
    final ArrayList<Metric> metrics = (ArrayList<Metric>) super.getCandidateMetrics(node);
    metrics.add(this.getKDTreeMetric());
    return metrics;
  }
  
  public KDTreeMetric getKDTreeMetric()
  {
    final KDTreeMetric metric = new KDTreeMetric(this.kdTree);
    metric.minFitness = 1;
    return metric;
  }
  
}
