package com.simiacryptus.probabilityModel.distributions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.distribution.MixtureMultivariateRealDistribution;
import org.apache.commons.math3.distribution.MultivariateRealDistribution;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.Pair;

import com.simiacryptus.data.RealFunction;
import com.simiacryptus.probabilityModel.Distribution;
import com.simiacryptus.util.TestUtil;

public enum Test3dDistributions implements Distribution
{
  MV2Normal_1(DistributionUtil.newRandomGaussian(3, TestUtil.random)),
  MV2Normal_2(DistributionUtil.newRandomGaussian(3, TestUtil.random)),
  MV2Normal_3(DistributionUtil.newRandomGaussian(3, TestUtil.random)),
  MV2Normal_4(DistributionUtil.newRandomGaussian(3, TestUtil.random)),
  MV2Normal_M12 {
    @Override
    protected MultivariateRealDistribution getDistribution(final Random random)
    {
      final RandomGenerator rng = new JDKRandomGenerator();
      rng.setSeed(random.nextInt());
      final List<Pair<Double, MultivariateRealDistribution>> components = new ArrayList<Pair<Double, MultivariateRealDistribution>>();
      components.add(new Pair<Double, MultivariateRealDistribution>(0.5, new MVWrapper(MV2Normal_1)));
      components.add(new Pair<Double, MultivariateRealDistribution>(0.5, new MVWrapper(MV2Normal_2)));
      return new MixtureMultivariateRealDistribution<MultivariateRealDistribution>(rng, components);
    }
  },
  MV2Normal_M34 {
    @Override
    protected MultivariateRealDistribution getDistribution(final Random random)
    {
      final RandomGenerator rng = new JDKRandomGenerator();
      rng.setSeed(random.nextInt());
      final List<Pair<Double, MultivariateRealDistribution>> components = new ArrayList<Pair<Double, MultivariateRealDistribution>>();
      components.add(new Pair<Double, MultivariateRealDistribution>(0.5, new MVWrapper(MV2Normal_3)));
      components.add(new Pair<Double, MultivariateRealDistribution>(0.5, new MVWrapper(MV2Normal_4)));
      return new MixtureMultivariateRealDistribution<MultivariateRealDistribution>(rng, components);
    }
  },
  MV3Snake_1(new SnakeDistribution(3, TestUtil.random)),
  MV3Logistic(new LogisticDistribution(3)), 
  MV2Logistic(new LogisticDistribution(2)), ;
  
  private final Long   seed;
  private Distribution distribution;
  
  Test3dDistributions()
  {
    this.seed = TestUtil.random.nextLong();
  }
  
  Test3dDistributions(final Distribution distribution)
  {
    this.seed = TestUtil.random.nextLong();
    this.distribution = distribution;
  }
  
  @Override
  public RealFunction getDensity()
  {
    return this.getDistribution().getDensity();
  }
  
  @Override
  public int getDimension()
  {
    return 3;
  }
  
  public Distribution getDistribution()
  {
    if (null == this.distribution)
    {
      synchronized (this)
      {
        if (null == this.distribution)
        {
          this.distribution = new DistributionAdapter(this.getDistribution(this.getRandom()));
        }
      }
    }
    return this.distribution;
  }
  
  protected MultivariateRealDistribution getDistribution(final Random random)
  {
    throw new UnsupportedOperationException();
  }
  
  protected Random getRandom()
  {
    return new Random(this.getSeed());
  }
  
  private long getSeed()
  {
    return this.seed;
  }
  
  @Override
  public double[] sample(final Random random)
  {
    return this.getDistribution().sample(random);
  }

  @Override
  public double[][] sample(int count, Random random)
  {
    final double[][] points = new double[count][];
    for(int i=0;i<count;i++) points[i] = sample(random);
    return points;
  }
  
}