package com.simiacryptus.probabilityModel.distributions;

import java.util.Random;

import com.simiacryptus.probabilityModel.Distribution;

public abstract class DistributionBase implements Distribution
{

  @Override
  public double[][] sample(int count, Random random)
  {
    final double[][] points = new double[count][];
    for(int i=0;i<count;i++) points[i] = sample(random);
    return points;
  }
}