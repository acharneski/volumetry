package com.simiacryptus.probabilityModel.kdtree;

import java.util.Arrays;
import java.util.Map.Entry;
import java.util.TreeMap;

public class KDTree
{
  
  public enum Side
  {
    Left, Right
  }
  
  public static double distance(final double[] point, final double[] neighbor)
  {
    double distanceSq = 0;
    for (int i = 0; i < point.length; i++)
    {
      final double d = point[i] - neighbor[i];
      distanceSq += d * d;
    }
    return Math.sqrt(distanceSq);
  }
  
  final double[][] points;
  
  final int        dim;
  
  public KDTree(final double[][] dataPoints)
  {
    this(dataPoints, dataPoints.length);
  }
  
  public KDTree(final double[][] dataPoints, final int length)
  {
    this.points = Arrays.copyOf(dataPoints, length);
    this.dim = dataPoints[0].length;
    this.getRoot().sort();
  }
  
  public TreeMap<Double, double[]> getNearestPoints(final double[] point, final int size)
  {
    final KDTreeNode root = this.getRoot();
    final KDTreeNode leaf = root.findLeaf(point, size);
    final TreeMap<Double, double[]> set = new TreeMap<Double, double[]>();
    for (final double[] p : leaf.getPoints())
    {
      set.put(distance(point, p), p);
    }
    final double minDistance = set.lastEntry().getKey();
    for (final double[] p : root.getRoughNeighborhood(point, minDistance))
    {
      set.put(distance(point, p), p);
    }
    return truncate(set, size);
  }

  private TreeMap<Double, double[]> truncate(final TreeMap<Double, double[]> set, final int size)
  {
    TreeMap<Double, double[]> truncated = new TreeMap<Double, double[]>();
    for(Entry<Double, double[]> e : set.entrySet())
    {
      if(truncated.size() >= size) break;
      truncated.put(e.getKey(), e.getValue());
    }
    return truncated;
  }
  
  public double[][] getNearset(final double[] point, final int size)
  {
    final TreeMap<Double, double[]> set = this.getNearestPoints(point, size);
    final double[][] pts = new double[size][];
    for (int i = 0; i < size; i++)
    {
      pts[i] = 0 < set.size() ? set.pollFirstEntry().getValue() : null;
    }
    return pts;
  }
  
  public KDTreeNode getRoot()
  {
    return new KDTreeNode(this);
  }

  public int size()
  {
    return points.length;
  }
  
}
