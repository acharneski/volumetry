package com.simiacryptus.probabilityModel.distributions;

import java.util.Random;

import com.simiacryptus.data.RealFunction;
import com.simiacryptus.lang.NotImplementedException;

public final class LogisticDistribution extends DistributionBase
{
  private final int dims;
  
  public LogisticDistribution(int dims)
  {
    this.dims = dims;
  }
  
  @SuppressWarnings("deprecation")
  @Override
  public RealFunction getDensity()
  {
    throw new NotImplementedException();
  }
  
  @Override
  public int getDimension()
  {
    return dims;
  }
  
  @SuppressWarnings("unused")
  @Override
  public double[] sample(Random random)
  {
    final double[] p = new double[dims];
    final double min;
    final double max;
    //if (random.nextBoolean())
    if (false)
    {
      min = .85;
      max = 0.9;
    }
    else
    {
      min = .75;
      max = 1;
    }
    final double span = max-min;
    final double fate = random.nextDouble();
    final double a = fate*span+min;
    p[0] = fate;
    double r = 4 * a;
    double r2 = (1-(r-3))+3;
    for (int i = 1; i < dims; i++)
    {
      double x = random.nextDouble();
      final double rx = ((i%2)==0)?r2:r;
      for (int j = 0; j < 1000; j++)
      {
        x = rx * x * (1 - x);
      }
      p[i] = x;
    }
    return p;
  }
}