package com.simiacryptus.probabilityModel.rules;

import org.json.JSONException;
import org.json.JSONObject;

import com.simiacryptus.probabilityModel.volume.SpacialVolume;

public interface PartitionRule
{
  
  public abstract int evaluate(final double[] point);
  
  public abstract int getPartitions();
  
  public abstract SpacialVolume[] getSubVolumes();
  
  public abstract JSONObject toJson() throws JSONException;
  
}