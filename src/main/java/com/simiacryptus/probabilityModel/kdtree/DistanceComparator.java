package com.simiacryptus.probabilityModel.kdtree;

import java.util.Comparator;

final class DistanceComparator implements Comparator<double[]>
{
  private final double[] point;
  
  public DistanceComparator(final double[] point)
  {
    this.point = point;
  }
  
  @Override
  public int compare(final double[] o1, final double[] o2)
  {
    return Double.valueOf(KDTree.distance(this.point, o1)).compareTo(KDTree.distance(this.point, o2));
  }
}