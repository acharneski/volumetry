package com.simiacryptus.probabilityModel.benchmark.base;

import java.util.Collection;

import com.simiacryptus.probabilityModel.volume.DoubleVolume;

public interface DataSampler
{
  
  public abstract Collection<double[]> getPoints(int count);

  public abstract DoubleVolume getVolume();
  
}