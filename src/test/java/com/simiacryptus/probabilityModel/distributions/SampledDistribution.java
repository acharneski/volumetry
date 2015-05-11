package com.simiacryptus.probabilityModel.distributions;

import java.util.Random;

import org.jzy3d.plot3d.pipelines.NotImplementedException;

import com.simiacryptus.data.RealFunction;

public class SampledDistribution extends DistributionBase
{
  
  private double[][] points;
  
  public SampledDistribution(final double[][] points)
  {
    this.points = points;
  }
  
  @Override
  public RealFunction getDensity()
  {
    throw new NotImplementedException();
  }
  
  @Override
  public int getDimension()
  {
    return this.points[0].length;
  }
  
  @Override
  public double[] sample(final Random random)
  {
    return this.points[random.nextInt(this.points.length)];
  }
  
}
