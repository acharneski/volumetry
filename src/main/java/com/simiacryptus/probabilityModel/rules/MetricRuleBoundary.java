package com.simiacryptus.probabilityModel.rules;

import com.simiacryptus.probabilityModel.rules.metrics.MetricSurface;

public class MetricRuleBoundary
{

  private final MetricSurface max;
  private final MetricSurface min;
  
  public MetricRuleBoundary(final MetricRule rule, final int partition)
  {
    
    if (rule.splitValue.length == partition)
    {
      this.max = null;
    }
    else
    {
      this.max = new MetricSurface(rule, rule.splitValue[partition], 0.01);
    }
    if (0 == partition)
    {
      this.min = null;
    }
    else
    {
      this.min = new MetricSurface(rule, rule.splitValue[partition - 1], 0.01);
    }
  }
  
  public void add(final double[][] points)
  {
    if (null != this.min)
    {
      this.min.add(points);
    }
    if (null != this.max)
    {
      this.max.add(points);
    }
  }

}
