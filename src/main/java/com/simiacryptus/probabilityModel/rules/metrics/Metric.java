package com.simiacryptus.probabilityModel.rules.metrics;

import org.json.JSONException;
import org.json.JSONObject;


public abstract class Metric
{
  public double minFitness = 0;
  
  public Metric()
  {
    super();
  }

  public abstract double evaluate(double[] point);
  
  public double[] evaluate(final double[][] dataPoints)
  {
    final double[] values = new double[dataPoints.length];
    for (int i = 0; i < values.length; i++)
    {
      values[i] = this.evaluate(dataPoints[i]);
    }
    return values;
  }
  
  public abstract JSONObject toJson() throws JSONException;
  
}
