package com.simiacryptus.probabilityModel.distributions;

import java.util.Random;

import org.apache.commons.math3.distribution.MultivariateRealDistribution;

import com.simiacryptus.data.RealFunction;

public final class DistributionAdapter extends DistributionBase
{
  private final class DensityFunction implements RealFunction
  {
    @Override
    public double[] evaluate(final double[] value)
    {
      return new double[] { multivariateNormalDistribution.density(value) };
    }
    
    @Override
    public int inputDimension()
    {
      return multivariateNormalDistribution.getDimension();
    }
    
    @Override
    public int outputDimension()
    {
      return 1;
    }
  }

  private final MultivariateRealDistribution multivariateNormalDistribution;
  
  public DistributionAdapter(final MultivariateRealDistribution multivariateNormalDistribution)
  {
    this.multivariateNormalDistribution = multivariateNormalDistribution;
  }
  
  @Override
  public RealFunction getDensity()
  {
    return new DensityFunction();
  }
  
  @Override
  public int getDimension()
  {
    return this.multivariateNormalDistribution.getDimension();
  }
  
  @Override
  public double[] sample(final Random random)
  {
    this.multivariateNormalDistribution.reseedRandomGenerator(random.nextLong());
    return this.multivariateNormalDistribution.sample();
  }
}