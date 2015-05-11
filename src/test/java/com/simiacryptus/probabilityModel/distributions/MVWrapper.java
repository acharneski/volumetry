package com.simiacryptus.probabilityModel.distributions;

import java.util.Random;

import org.apache.commons.math3.distribution.MultivariateRealDistribution;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;

import com.simiacryptus.probabilityModel.Distribution;


public final class MVWrapper implements MultivariateRealDistribution
{
  private final Distribution inner;
  Random                     r = new Random();
  
  public MVWrapper(final Distribution inner)
  {
    this.inner = inner;
  }
  
  @Override
  public double density(final double[] x)
  {
    return this.inner.getDensity().evaluate(x)[0];
  }
  
  @Override
  public int getDimension()
  {
    return this.inner.getDimension();
  }
  
  @Override
  public void reseedRandomGenerator(final long seed)
  {
    this.r.setSeed(seed);
  }
  
  @Override
  public double[] sample()
  {
    return this.inner.sample(this.r);
  }
  
  @Override
  public double[][] sample(final int sampleSize) throws NotStrictlyPositiveException
  {
    final double[][] x = new double[sampleSize][];
    for (int i = 0; i < x.length; i++)
    {
      x[i] = this.sample();
    }
    return x;
  }
}