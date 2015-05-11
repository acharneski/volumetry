package com.simiacryptus.probabilityModel;

import java.util.Random;

import com.simiacryptus.data.RealFunction;

public interface Distribution
{
  RealFunction getDensity();
  
  int getDimension();
  
  double[] sample(Random random);
  
  double[][] sample(int count, Random random);
}