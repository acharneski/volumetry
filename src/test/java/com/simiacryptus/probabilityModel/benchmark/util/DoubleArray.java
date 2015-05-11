package com.simiacryptus.probabilityModel.benchmark.util;

import java.util.Arrays;

public class DoubleArray
{
  
  protected double[] values = new double[16];
  protected int position = 0;
  protected double sum = 0;
  protected double sumSq = 0;
  private int count = 0;

  public DoubleArray()
  {
    super();
  }

  public DoubleArray(DoubleArray toCopy)
  {
    super();
    this.values = Arrays.copyOf(toCopy.values, toCopy.position);
    this.count = toCopy.count;
    this.sum = toCopy.sum;
    this.sumSq = toCopy.sumSq;
    this.position = toCopy.position;
  }

  public void add(double v)
  {
    if (values.length <= position)
    {
      values = Arrays.copyOf(values, values.length * 2);
    }
    values[position++] = v;
    if(Double.isInfinite(v)) return;
    if(Double.isNaN(v)) return;
    this.count = getCount() + 1;
    sum += v;
    sumSq += v*v;
  }

  public double mean()
  {
    return sum / getCount();
  }

  public double stdDev()
  {
    final double mean = mean();
    final double meanSq = mean * mean;
    return Math.sqrt((sumSq / getCount()) - meanSq);
  }

  public double get(int v)
  {
    return values[v];
  }

  public double sum()
  {
    return sum;
  }

  public double normalize(double v)
  {
    return (v - mean()) / stdDev();
  }
  
  public double unnormalize(double v)
  {
    return (v * stdDev()) + mean();
  }

  protected void recalculateStatistics()
  {
    this.count = 0;
    sum = 0;
    sumSq = 0;
    for(int i=0;i<position;i++)
    {
      final double x = values[i];
      if(Double.isInfinite(x)) continue;
      if(Double.isNaN(x)) continue;
      this.count = getCount() + 1;
      sum += x;
      sumSq += x * x;
    }
  }

  public int getCount()
  {
    return count;
  }

}