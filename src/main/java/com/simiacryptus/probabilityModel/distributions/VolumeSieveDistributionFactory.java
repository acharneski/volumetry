package com.simiacryptus.probabilityModel.distributions;

import java.util.Random;

import com.simiacryptus.data.RealFunction;
import com.simiacryptus.probabilityModel.Distribution;
import com.simiacryptus.probabilityModel.volume.SpacialVolume;

public final class VolumeSieveDistributionFactory extends DistributionBase
{
  
  public final Distribution  inner;
  public final SpacialVolume range;
  
  public VolumeSieveDistributionFactory(final Distribution inner, final SpacialVolume range2)
  {
    this.inner = inner;
    this.range = range2;
  }
  
  @Override
  public boolean equals(final Object obj)
  {
    if (this == obj)
    {
      return true;
    }
    if (obj == null)
    {
      return false;
    }
    if (this.getClass() != obj.getClass())
    {
      return false;
    }
    final VolumeSieveDistributionFactory other = (VolumeSieveDistributionFactory) obj;
    if (this.inner == null)
    {
      if (other.inner != null)
      {
        return false;
      }
    }
    else if (!this.inner.equals(other.inner))
    {
      return false;
    }
    if (this.range == null)
    {
      if (other.range != null)
      {
        return false;
      }
    }
    else if (!this.range.equals(other.range))
    {
      return false;
    }
    return true;
  }
  
  @Override
  public RealFunction getDensity()
  {
    final RealFunction density = this.inner.getDensity();
    return null == density ? null : new RealFunction() {
      
      @Override
      public double[] evaluate(final double[] value)
      {
        if (!VolumeSieveDistributionFactory.this.range.contains(value))
        {
          return new double[] { 0. };
        }
        return density.evaluate(value);
      }
      
      @Override
      public int inputDimension()
      {
        return density.inputDimension();
      }
      
      @Override
      public int outputDimension()
      {
        return density.outputDimension();
      }
    };
  }
  
  @Override
  public int getDimension()
  {
    return this.range.dimensions();
  }
  
  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + (this.inner == null ? 0 : this.inner.hashCode());
    result = prime * result + (this.range == null ? 0 : this.range.hashCode());
    return result;
  }
  
  @Override
  public double[] sample(final Random random)
  {
    double[] sample;
    do
    {
      sample = this.inner.sample(random);
    } while (!this.range.contains(sample));
    return sample;
  }
  
  @Override
  public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("VolumeSieve [");
    builder.append(this.inner);
    builder.append("]");
    return builder.toString();
  }
  
}