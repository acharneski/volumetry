package com.simiacryptus.probabilityModel.distributions;

import java.util.Random;

import com.simiacryptus.data.RealFunction;
import com.simiacryptus.probabilityModel.Distribution;

public class RemappedDistribution extends DistributionBase
{
  
  private final Distribution inner;
  private final RealFunction mapping;
  private final RealFunction inverseMapping;
  
  public RemappedDistribution(final RealFunction mapping, final Distribution inner)
  {
    this.mapping = mapping;
    this.inner = inner;
    this.inverseMapping = null;
  }
  
  public RemappedDistribution(final RealFunction mapping, final Distribution inner, final RealFunction inverseMapping)
  {
    this.mapping = mapping;
    this.inner = inner;
    this.inverseMapping = inverseMapping;
  }
  
  @Override
  public RealFunction getDensity()
  {
    final RealFunction density = this.inner.getDensity();
    return null == density ? null : new RealFunction() {
      
      @Override
      public double[] evaluate(final double[] value)
      {
        return density.evaluate(RemappedDistribution.this.inverseMapping.evaluate(value));
      }
      
      @Override
      public int inputDimension()
      {
        return density.outputDimension();
      }
      
      @Override
      public int outputDimension()
      {
        return density.inputDimension();
      }
    };
  }
  
  @Override
  public int getDimension()
  {
    return this.mapping.outputDimension();
  }
  
  @Override
  public double[] sample(final Random random)
  {
    return this.mapping.evaluate(this.inner.sample(random));
  }
  
}
