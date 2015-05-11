package com.simiacryptus.probabilityModel.benchmark.base;

import com.simiacryptus.probabilityModel.volume.DoubleVolume;

public interface DataDistribution
{
  
  public abstract double getDensity(double[] p);

  public abstract DoubleVolume getVolume();

}