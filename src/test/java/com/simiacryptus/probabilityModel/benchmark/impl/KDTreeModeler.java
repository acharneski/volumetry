package com.simiacryptus.probabilityModel.benchmark.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.simiacryptus.lang.NotImplementedException;
import com.simiacryptus.probabilityModel.benchmark.base.DataDistribution;
import com.simiacryptus.probabilityModel.benchmark.base.DataLearner;
import com.simiacryptus.probabilityModel.benchmark.base.TestObject;
import com.simiacryptus.probabilityModel.kdtree.KDTree;
import com.simiacryptus.probabilityModel.volume.DoubleVolume;

public class KDTreeModeler extends TestObject implements DataLearner
{
  private final ArrayList<double[]> list       = new ArrayList<double[]>();
  private KDTree                    kdTree;
  private final DoubleVolume        volume;
  private final int neighborhoodSize;
  
  public KDTreeModeler(DoubleVolume volume)
  {
    this.volume = volume;
    // Minimum number of points needed to define a volume in # of dims
    // Still a fudge parameter, but the intuitive meaning is attractive.
    this.neighborhoodSize = volume.dimensions() + 1;
  }
  
  @Override
  public void addPoints(Collection<double[]> points)
  {
    list.addAll(points);
  }
  
  @Override
  public void train()
  {
    kdTree = new KDTree(list.toArray(new double[][] {}));
  }
  
  @Override
  public double getDensity(double[] p)
  {
    final Iterator<Double> iterator = kdTree.getNearestPoints(p, neighborhoodSize).keySet().iterator();
    for (int i = 0; i < neighborhoodSize - 1; i++)
      iterator.next();
    final double density = neighborhoodSize / Math.pow(iterator.next(), volume.dimensions());
    return density/kdTree.size();
  }
  
  @Override
  public void reset()
  {
    list.clear();
    kdTree = null;
  }
  
  @Override
  public DoubleVolume getVolume()
  {
    return volume;
  }

  @Override
  public Collection<double[]> getPoints(int count)
  {
    throw new NotImplementedException();
  }
  
}
