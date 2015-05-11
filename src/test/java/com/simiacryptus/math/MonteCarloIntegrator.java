package com.simiacryptus.math;

import java.util.Random;

import com.simiacryptus.lang.Function;

public class MonteCarloIntegrator
{
  public int                        sampleCount = 1000;
  public Function<double[], Double> function;
  public double[][]                 range;
  public Random                     random      = new Random();
  
  public double[] getNextPoint()
  {
    final double[] point = new double[this.range.length];
    this.getNextPoint(point);
    return point;
  }
  
  public void getNextPoint(final double[] point)
  {
    for (int dimension = 0; dimension < this.range.length; dimension++)
    {
      final double[] dimRange = this.range[dimension];
      point[dimension] = dimRange[0] + this.random.nextDouble() * (dimRange[1] - dimRange[0]);
    }
  }
  
  public double getVolume()
  {
    double volume = 1.;
    for (final double[] dimRange : this.range)
    {
      volume *= dimRange[1] - dimRange[0];
    }
    return volume;
  }
  
  public double integrate()
  {
    double sum = 0.;
    final double[] point = this.getNextPoint();
    for (int sample = 0; sample < this.sampleCount; sample++)
    {
      this.getNextPoint(point);
      sum += this.function.evaluate(point);
    }
    return this.getVolume() * sum / this.sampleCount;
  }
  
}
