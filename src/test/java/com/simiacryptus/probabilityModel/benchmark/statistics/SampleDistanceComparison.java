package com.simiacryptus.probabilityModel.benchmark.statistics;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.json.JSONException;
import org.json.JSONObject;

import com.simiacryptus.probabilityModel.benchmark.base.DataSampler;
import com.simiacryptus.probabilityModel.benchmark.util.Distribution1d;
import com.simiacryptus.probabilityModel.kdtree.KDTree;

public class SampleDistanceComparison
{

  private JSONObject jsonValue = new JSONObject();

  public SampleDistanceComparison(DataSampler left, DataSampler right, int samples) throws JSONException
  {
    Collection<double[]> pointsA = left.getPoints(samples);
    Collection<double[]> pointsB = right.getPoints(samples);
    jsonValue.put("avg dist", compare(pointsA, pointsB));
  }


  public static class PointPair implements Comparable<PointPair>
  {
    public final double[] a;
    public final double[] b;
    public final double distance;
    public PointPair(double[] from, double[] to)
    {
      super();
      this.a = from;
      this.b = to;
      this.distance = KDTree.distance(from, to);
    }
    @Override
    public int compareTo(PointPair o)
    {
      int r = Double.compare(distance, o.distance);
      if(0 == r) r = Integer.valueOf(hashCode()).compareTo(o.hashCode());
      return r;
    }
    
    
  }
  
  private double compare(Collection<double[]> pointsA, Collection<double[]> pointsB)
  {
    HashSet<double[]> remainingPointsA = new HashSet<double[]>(pointsA);
    HashSet<double[]> remainingPointsB = new HashSet<double[]>(pointsB);
    HashMap<double[], PointPair> finalLinksA = new HashMap<double[], SampleDistanceComparison.PointPair>();
    HashMap<double[], PointPair> finalLinksB = new HashMap<double[], SampleDistanceComparison.PointPair>();
    while(0 < remainingPointsA.size())
    {
      int counter = 0;
      int neighborhood = 5;
      int batch = (int) (1 + remainingPointsA.size() - Math.sqrt(remainingPointsA.size()));
      HashSet<double[]> newPointsA = new HashSet<double[]>();
      HashSet<double[]> newPointsB = new HashSet<double[]>();
      for(PointPair item : buildLinks(remainingPointsA, remainingPointsB, neighborhood))
      {
        if(batch < ++counter) break;
        if(finalLinksA.containsKey(item.a)) continue;
        if(finalLinksB.containsKey(item.b)) continue;
        finalLinksA.put(item.a, item);
        finalLinksB.put(item.b, item);
        newPointsA.add(item.a);
        newPointsB.add(item.b);
      }
      remainingPointsA.removeAll(newPointsA);
      remainingPointsB.removeAll(newPointsB);
    }
    double total = 0;
    for(PointPair item : finalLinksA.values())
    {
      total += item.distance;
    }
    return total / finalLinksA.size();
  }

  private TreeSet<PointPair> buildLinks(Collection<double[]> pointsA, HashSet<double[]> pointsB, int neighborhood)
  {
    KDTree treeB = new KDTree(pointsB.toArray(new double[][]{}));
    TreeSet<PointPair> links = new TreeSet<SampleDistanceComparison.PointPair>();
    for(double[] point : pointsA)
    {
      for(double[] n : treeB.getNearestPoints(point, neighborhood).values())
      {
        links.add(new PointPair(point, n));
      }
    }
    return links;
  }

  public Object jsonValue() throws JSONException
  {
    return jsonValue;
  }
  
}
