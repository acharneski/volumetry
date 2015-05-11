package com.simiacryptus.probabilityModel.model;

import java.util.Collection;

import com.simiacryptus.probabilityModel.volume.DoubleVolume;

public final class PointModel extends DistributionModel<PointNode>
{
  private final DoubleVolume range;
  
  public PointModel(final DoubleVolume range)
  {
    this.range = range;
  }
  
  public void addDataPoint(final double[] point)
  {
    this.getRoot().addDataPoint(point);
  }
  
  @Override
  protected PointNode constructRoot()
  {
    return new PointNode(this.range, this);
  }
  
  public Collection<double[]> getDataPoints()
  {
    return this.getRoot().getDataPoints();
  }
}