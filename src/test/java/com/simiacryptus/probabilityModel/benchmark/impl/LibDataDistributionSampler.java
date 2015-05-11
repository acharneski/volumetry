package com.simiacryptus.probabilityModel.benchmark.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import com.simiacryptus.probabilityModel.Distribution;
import com.simiacryptus.probabilityModel.benchmark.base.DataDistribution;
import com.simiacryptus.probabilityModel.benchmark.base.DataSampler;
import com.simiacryptus.probabilityModel.benchmark.base.TestObject;
import com.simiacryptus.probabilityModel.volume.DoubleVolume;

public final class LibDataDistributionSampler extends TestObject implements DataDistribution, DataSampler
{
  private final DoubleVolume volume;
  private final Distribution dist;
  final Random random = new Random();
  
  public LibDataDistributionSampler(DoubleVolume volume, Distribution dist)
  {
    this.volume = volume;
    this.dist = dist;
  }
  
  @Override
  public DoubleVolume getVolume()
  {
    return volume;
  }
  
  @Override
  public Collection<double[]> getPoints(int count)
  {
    final ArrayList<double[]> list = new ArrayList<double[]>();
    while(list.size() < count)
    {
      final double[] dataPoint = dist.sample(random);
      if (volume.contains(dataPoint))
      {
        list.add(dataPoint);
      }
    }
    return list;
  }
  
  @Override
  public double getDensity(double[] p)
  {
    return dist.getDensity().evaluate(p)[0];
  }

  @Override
  public String getName()
  {
    return this.toString();
  }

  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append(getClass().getSimpleName());
    builder.append("[");
    builder.append(dist);
    builder.append("]");
    return builder.toString();
  }
  
  
}