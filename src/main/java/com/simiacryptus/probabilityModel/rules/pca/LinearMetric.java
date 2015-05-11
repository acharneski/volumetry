package com.simiacryptus.probabilityModel.rules.pca;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.json.JSONException;
import org.json.JSONObject;

import com.simiacryptus.probabilityModel.rules.metrics.Metric;

public final class LinearMetric extends Metric
{
  public final RealVector eigenvector;
  private final double     eigenvalue;
  public final RealVector centroid;
  
  public LinearMetric(RealVector eigenvector, double eigenvalue, RealVector centroid)
  {
    this.eigenvector = eigenvector;
    this.eigenvalue = eigenvalue;
    this.centroid = centroid;
  }
  
  @Override
  public JSONObject toJson() throws JSONException
  {
    final JSONObject json = new JSONObject();
    json.put("centroid", centroid.toArray());
    json.put("eigenVector", eigenvector.toArray());
    json.put("eigenValue", eigenvalue);
    return json;
  }
  
  @Override
  public double evaluate(double[] point)
  {
    return eigenvector.dotProduct(new ArrayRealVector(point).subtract(centroid));
  }
}