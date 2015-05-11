package com.simiacryptus.probabilityModel.rules.fitness;

import java.util.List;

import com.simiacryptus.probabilityModel.model.PointNode;
import com.simiacryptus.probabilityModel.rules.fitness.VolumeDataDensity.VolumeDataNormalizer;

public abstract class LinearSplitFitness implements SplitFitness
{
  
  
  /* (non-Javadoc)
   * @see com.simiacryptus.probabilityModel.rules.fitness.SplitFitness#getFitness(java.util.List)
   */
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