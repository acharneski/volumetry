package com.simiacryptus.probabilityModel.benchmark.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import com.simiacryptus.probabilityModel.benchmark.base.DataDistribution;
import com.simiacryptus.probabilityModel.benchmark.base.DataSampler;
import com.simiacryptus.probabilityModel.volume.DoubleVolume;

public class SieveSampler implements DataSampler
{
  private final DoubleVolume        volume;
  private final Random              random     = new Random();
  private double                    maxDensity = -1;
  public final DataDistribution dist;
  
  public SieveSampler(DoubleVolume volume, DataDistribution dist)
  {
    this.volume = volume;
    this.dist = dist;
  }
  
  public Collection<double[]> getPoints(int count)
  {
    final ArrayList<double[]> list = new ArrayList<double[]>();
    if (maxDensity < 0)
    {
      fill(list, 100);
      list.clear();
    }
    fill(list, count);
    return list;
  }
  
  private void fill(final ArrayList<double[]> list, int count)
  {
    while (list.size() < count)
    {
      final double[] dataPoint = volume.sample();
      final double density = dist.getDensity(dataPoint);
      if (density > maxDensity) maxDensity = density;
      if ((density / maxDensity) > random.nextDouble())
      {
        list.add(dataPoint);
      }
    }
  }
  
  @Override
  public DoubleVolume getVolume()
  {
    return volume;
  }
  
}
