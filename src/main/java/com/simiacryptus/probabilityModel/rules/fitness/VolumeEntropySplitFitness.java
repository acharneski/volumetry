package com.simiacryptus.probabilityModel.rules.fitness;

import com.simiacryptus.lang.MathUtil;
import com.simiacryptus.probabilityModel.model.PointNode;

public class VolumeEntropySplitFitness extends LinearSplitFitness
{
  
  @Override
  public double getValue(PointNode node, final VolumeDataDensity item)
  {
    if(0 >= item.data) return -Double.MAX_VALUE;
    if(0 >= item.volume) return -Double.MAX_VALUE;
    final double fitness = item.volume * MathUtil.log2(item.data);
    assert !Double.isInfinite(fitness);
    assert !Double.isNaN(fitness);
    return -fitness;
  }
}