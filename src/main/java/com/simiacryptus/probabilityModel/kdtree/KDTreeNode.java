package com.simiacryptus.probabilityModel.kdtree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.simiacryptus.probabilityModel.kdtree.KDTree.Side;

class KDTreeNode
{
  
  private final KDTree kdTree;
  public final int        startIndex;
  public final int        endIndex;
  public final KDTreeNode parent;
  public final int        dimension;
  public final Side       side;
  
  public KDTreeNode(KDTree kdTree)
  {
    super();
    this.kdTree = kdTree;
    this.parent = null;
    this.side = null;
    this.startIndex = 0;
    this.endIndex = kdTree.points.length;
    this.dimension = 0;
  }
  
  public KDTreeNode(final KDTreeNode parent, final Side side, final int startIndex, final int endIndex, final int dimension)
  {
    super();
    this.kdTree = parent.kdTree;
    this.parent = parent;
    this.side = side;
    this.startIndex = startIndex;
    this.endIndex = endIndex;
    this.dimension = dimension;
  }
  
  KDTreeNode findLeaf(final double[] point, final int size)
  {
    final double[] splitPoint = this.getSplitPoint();
    final double delta = splitPoint[this.dimension] - point[this.dimension];
    
    final KDTreeNode left = this.getLeft();
    if (null != left)
    {
      final int leftSize = left.size();
      if (leftSize >= size)
      {
        if (delta > 0)
        {
          return left.findLeaf(point, size);
        }
      }
    }
    final KDTreeNode right = this.getRight();
    if (null != right)
    {
      final int rightSize = right.size();
      if (rightSize >= size)
      {
        if (delta < 0)
        {
          return right.findLeaf(point, size);
        }
      }
    }
    return this;
  }
  
  public KDTreeNode getLeft()
  {
    final int from = this.startIndex;
    final int to = this.getSplitIndex();
    if (from >= to)
    {
      return null;
    }
    return new KDTreeNode(this, Side.Left, from, to, (this.dimension + 1) % this.kdTree.dim);
  }
  
  public double[][] getPoints()
  {
    return Arrays.copyOfRange(this.kdTree.points, this.startIndex, this.endIndex);
  }
  
  public KDTreeNode getRight()
  {
    final int from = this.getSplitIndex() + 1;
    final int to = this.endIndex;
    if (from >= to)
    {
      return null;
    }
    return new KDTreeNode(this, Side.Right, from, to, (this.dimension + 1) % this.kdTree.dim);
  }
  
  public List<double[]> getRoughNeighborhood(final double[] point, final double minDistance)
  {
    final ArrayList<double[]> list = new ArrayList<double[]>();
    
    final double[] splitPoint = this.getSplitPoint();
    final double delta = splitPoint[this.dimension] - point[this.dimension];
    final boolean matches = Math.abs(delta) <= minDistance;
    if (matches)
    {
      list.add(splitPoint);
    }
    if (delta >= 0 || matches)
    {
      final KDTreeNode left = this.getLeft();
      if (null != left)
      {
        list.addAll(left.getRoughNeighborhood(point, minDistance));
      }
    }
    if (delta <= 0 || matches)
    {
      final KDTreeNode right = this.getRight();
      if (null != right)
      {
        list.addAll(right.getRoughNeighborhood(point, minDistance));
      }
    }
    return list;
  }
  
  private int getSplitIndex()
  {
    return (this.startIndex + this.endIndex) / 2;
  }
  
  private double[] getSplitPoint()
  {
    return this.kdTree.points[this.getSplitIndex()];
  }
  
  public double getSplitValue()
  {
    return this.getSplitPoint()[this.dimension];
  }
  
  private int size()
  {
    return this.endIndex - this.startIndex;
  }
  
  public void sort()
  {
    Arrays.sort(this.kdTree.points, this.startIndex, this.endIndex, new Comparator<double[]>() {
      @Override
      public int compare(final double[] o1, final double[] o2)
      {
        return Double.valueOf(o1[dimension]).compareTo(o2[dimension]);
      }
    });
    final KDTreeNode left = this.getLeft();
    if (null != left)
    {
      left.sort();
    }
    final KDTreeNode right = this.getRight();
    if (null != right)
    {
      right.sort();
    }
  }
  
}