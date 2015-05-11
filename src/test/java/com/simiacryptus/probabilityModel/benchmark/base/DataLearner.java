package com.simiacryptus.probabilityModel.benchmark.base;

import java.util.Collection;

public interface DataLearner extends DataSampler, DataDistribution
{
  
  public abstract void addPoints(Collection<double[]> points);
  
  public abstract void train();
  
  public abstract void reset();
  
}
