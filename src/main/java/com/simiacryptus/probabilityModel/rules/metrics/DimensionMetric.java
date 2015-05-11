package com.simiacryptus.probabilityModel.rules.metrics;

import org.json.JSONException;
import org.json.JSONObject;

import com.simiacryptus.data.DoubleRange;

public class DimensionMetric extends Metric
{
  
  public final int dimension;
  
  public DimensionMetric(final int dimension, DoubleRange range)
  {
    super();
    this.dimension = dimension;
  }
  
  @Override
  public double evaluate(final double[] point)
  {
    return point[this.dimension];
  }
  
  @Override
  public JSONObject toJson() throws JSONException
  {
    final JSONObject json = new JSONObject();
    json.put("class", this.getClass().getSimpleName());
    json.put("dimension", this.dimension);
    return json;
  }
  
}
