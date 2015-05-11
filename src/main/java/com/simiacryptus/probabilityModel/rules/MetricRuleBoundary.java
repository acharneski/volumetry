package com.simiacryptus.probabilityModel.rules;

import java.util.ArrayList;
import java.util.Arrays;

import com.simiacryptus.probabilityModel.rules.metrics.MetricSurface;

public class MetricRuleBoundary
{
  
  public static int           DEBUG = 1;
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
  
  public double[][] findBorderPoints(final double tolerance, final int size)
  {
    final ArrayList<double[]> list = new ArrayList<double[]>();
    if (null != this.max && null != this.min)
    {
      list.addAll(Arrays.asList(this.max.findBorderPoints(size / 2)));
      list.addAll(Arrays.asList(this.min.findBorderPoints(size / 2)));
    }
    else if (null != this.max)
    {
      list.addAll(Arrays.asList(this.max.findBorderPoints(size / 2)));
    }
    else if (null != this.min)
    {
      list.addAll(Arrays.asList(this.min.findBorderPoints(size / 2)));
    }
    else
    {
      throw new IllegalStateException();
    }
    return list.toArray(new double[][] {});
  }
}
