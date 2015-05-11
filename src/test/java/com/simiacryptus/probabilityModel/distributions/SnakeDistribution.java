package com.simiacryptus.probabilityModel.distributions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.analysis.interpolation.LoessInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

import com.simiacryptus.data.RealFunction;
import com.simiacryptus.lang.NotImplementedException;

public final class SnakeDistribution extends DistributionBase
{
  private final List<PolynomialSplineFunction> parametricFuctions;
  private final int                            dims;
  private final double                         radius;
  
  public SnakeDistribution(int dims, Random random)
  {
    this.dims = dims;
    final List<PolynomialSplineFunction> parametricFuctions = new ArrayList<PolynomialSplineFunction>();
    final int snakeNodes = 20;
    for (int j = 0; j < dims; j++)
    {
      final double[] xval = new double[snakeNodes];
      final double[] yval = new double[snakeNodes];
      for (int i = 0; i < xval.length; i++)
      {
        xval[i] = random.nextDouble();
        yval[i] = (double) i / (xval.length - 1);
      }
      parametricFuctions.add(new LoessInterpolator().interpolate(yval, xval));
    }
    final double radius = 0.01;
    
    this.parametricFuctions = parametricFuctions;
    this.radius = radius;
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
  
  @Override
  public double[] sample(final Random random)
  {
    final double[] pt = new double[dims];
    final double parametric = random.nextDouble();
    for (int i = 0; i < pt.length; i++)
    {
      final PolynomialSplineFunction f = parametricFuctions.get(i);
      pt[i] = f.value(parametric);
      pt[i] += random.nextGaussian() * radius;
    }
    return pt;
  }
}