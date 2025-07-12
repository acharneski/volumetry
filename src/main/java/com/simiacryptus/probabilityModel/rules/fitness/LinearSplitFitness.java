package com.simiacryptus.probabilityModel.rules.fitness;

import com.simiacryptus.probabilityModel.model.PointNode;
import com.simiacryptus.probabilityModel.rules.fitness.VolumeDataDensity.VolumeDataNormalizer;

import java.util.List;

public abstract class LinearSplitFitness implements SplitFitness
{
  
  
  @Override
  public double getFitness(PointNode node, final List<VolumeDataDensity> list)
  {
    final List<VolumeDataDensity> normalizedList = this.process(list);
    double total = 0;
    for (final VolumeDataDensity item : normalizedList)
    {
      total += this.getValue(node, item);
    }
    return total;
  }
  
  protected abstract double getValue(PointNode node, VolumeDataDensity item);
  
  protected List<VolumeDataDensity> process(List<VolumeDataDensity> list) {
    list = new VolumeDataNormalizer().process(list);
    //list = new VolumeDataDensity.DataSmoother().process(list);
    return list;
  }

  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append(getClass().getSimpleName());
    return builder.toString();
  }
  
  
  
}