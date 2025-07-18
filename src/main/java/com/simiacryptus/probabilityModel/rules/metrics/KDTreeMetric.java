package com.simiacryptus.probabilityModel.rules.metrics;

import com.simiacryptus.probabilityModel.kdtree.KDTree;
import com.simiacryptus.util.ObjUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map.Entry;
import java.util.TreeMap;

public class KDTreeMetric extends Metric
{
  
  private final KDTree tree;
  
  private int          truncate     = 0;
  
  private int          neighborhood = 10;

  public KDTreeMetric(final KDTree tree)
  {
    super();
    this.tree = tree;
  }
  
  @Override
  public double evaluate(final double[] point)
  {
    double total = 0;
    final TreeMap<Double, double[]> set = this.tree.getNearestPoints(point, this.getNeighborhood());
    Entry<Double, double[]> nextClosest;
    for (int i = 0; i < this.getTruncate(); i++)
    {
      nextClosest = set.pollFirstEntry();
      // total += nextClosest.getKey();
    }
    for (int i = 0; i < this.getNeighborhood() - this.getTruncate(); i++)
    {
      nextClosest = set.pollFirstEntry();
      total += nextClosest.getKey();
    }
    return total;
  }
  
  public int getNeighborhood()
  {
    return this.neighborhood;
  }
  
  public int getTruncate()
  {
    return this.truncate;
  }

  @Override
  public JSONObject toJson() throws JSONException
  {
    try
    {
      return ObjUtil.toJson(this);
    }
    catch (final Exception e)
    {
      throw new RuntimeException(e);
    }
  }
  
  @Override
  public String toString()
  {
    try
    {
      return this.toJson().toString(2);
    }
    catch (final JSONException e)
    {
      return e.toString();
    }
  }
  
}
