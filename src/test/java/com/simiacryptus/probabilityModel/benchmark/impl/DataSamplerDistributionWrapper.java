package com.simiacryptus.probabilityModel.benchmark.impl;

import java.util.Iterator;
import java.util.Random;

import org.jzy3d.plot3d.pipelines.NotImplementedException;

import com.simiacryptus.data.RealFunction;
import com.simiacryptus.probabilityModel.Distribution;
import com.simiacryptus.probabilityModel.benchmark.base.DataSampler;

public final class DataSamplerDistributionWrapper implements Distribution
{
  private final DataSampler sampler;
  private Iterator<double[]> iterator;
  
  public DataSamplerDistributionWrapper(DataSampler sampler)
  {
    this.sampler = sampler;
  }
  
  @Override
  public double[][] sample(int count, Random random)
  {
    return sampler.getPoints(count).toArray(new double[][]{});
  }
  
  @Override
  public synchronized double[] sample(Random random)
  {
    return getIterator().next();
  }

  private synchronized Iterator<double[]> getIterator()
  {
    if (null == this.iterator || !this.iterator.hasNext())
    {
      this.iterator = sampler.getPoints(100).iterator();
    }
    return iterator;
  }
  
  @Override
  public int getDimension()
  {
    throw new NotImplementedException();
  }
  
  @Override
  public RealFunction getDensity()
  {
    throw new NotImplementedException();
  }
}