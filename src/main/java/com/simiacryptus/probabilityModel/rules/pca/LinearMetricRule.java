package com.simiacryptus.probabilityModel.rules.pca;

import com.simiacryptus.data.DoubleRange;
import com.simiacryptus.probabilityModel.rules.MetricRule;
import com.simiacryptus.probabilityModel.rules.metrics.Metric;
import com.simiacryptus.probabilityModel.volume.SpacialVolume;

public final class LinearMetricRule extends MetricRule
{
  public LinearMetricRule(SpacialVolume range, Metric metric, double[] splitValue)
  {
    super(range, metric, splitValue);
  }
  
  @Override
  protected SpacialVolume getSubVolume(int n)
  {
    if (metric instanceof LinearMetric)
    {
      final DoubleRange metricRange = getMetricRange(n);
      return LinearVolume.intersect(range, (LinearMetric) metric, metricRange);
    }
    else
    {
      return super.getSubVolume(n);
    }
  }
}