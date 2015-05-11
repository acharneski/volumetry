package com.simiacryptus.probabilityModel.volume;

import org.json.JSONException;
import org.json.JSONObject;

import com.simiacryptus.data.DoubleRange;
import com.simiacryptus.data.VolumeMetric;

public interface SpacialVolume
{
  
  public abstract boolean contains(double[] point);
  
  public abstract int dimensions();
  
  @Override
  public abstract boolean equals(Object obj);
  
  public abstract DoubleVolume getBounds();
  
  public abstract DoubleRange getRange(int d);
  
  public abstract VolumeMetric getVolume();
  
  public abstract SpacialVolume intersect(SpacialVolume right);
  
  public abstract boolean intersects(SpacialVolume range);
  
  public abstract Iterable<double[]> points();
  
  public abstract double[] sample();
  
  public abstract JSONObject toJson() throws JSONException;

  public abstract boolean isUnbounded();

  public abstract SpacialVolume union(SpacialVolume region);
  
}