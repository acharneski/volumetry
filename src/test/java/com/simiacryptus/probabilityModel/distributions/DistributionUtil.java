package com.simiacryptus.probabilityModel.distributions;

import java.util.Random;

import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;

import com.simiacryptus.probabilityModel.Distribution;


public class DistributionUtil
{
  
  public static Distribution newRandomGaussian(final int dims, final Random distRandom)
  {
    return new DistributionAdapter(newRandomGaussianRaw(dims, distRandom));
  }
  
  public static MultivariateNormalDistribution newRandomGaussianRaw(final int dims, final Random distRandom)
  {
    final double[] means = new double[dims];
    for (int i = 0; i < means.length; i++)
    {
      means[i] = 0.1 + 0.3 * distRandom.nextDouble();
    }
    final double[][] diaganals = new double[dims][];
    for (int i = 0; i < diaganals.length; i++)
    {
      diaganals[i] = new double[dims];
      diaganals[i][i] = 1;
    }
    final RandomGenerator rng = new JDKRandomGenerator();
    rng.setSeed(distRandom.nextInt());
    final MultivariateNormalDistribution multivariateNormalDistribution = new MultivariateNormalDistribution(rng, means, diaganals);
    return multivariateNormalDistribution;
  }
  
}
