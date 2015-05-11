package com.simiacryptus.probabilityModel.benchmark.statistics;

import java.util.Collection;

import org.json.JSONException;
import org.json.JSONObject;

import com.simiacryptus.probabilityModel.benchmark.base.DataDistribution;
import com.simiacryptus.probabilityModel.benchmark.base.DataSampler;

public class SamplerDistributionEncodingEntropy
{

  private double entropy = 0;
  private int invalid = 0;
  private int count;

  public SamplerDistributionEncodingEntropy(DataSampler data, DataDistribution model, int sampleSize)
  {
    Collection<double[]> points = data.getPoints(sampleSize);
    for(double[] p : points)
    {
      final double density = model.getDensity(p);
      if(0 >= density)
      {
        this.invalid++;
        continue;
      }
      if(Double.isInfinite(density)) 
      {
        this.invalid++;
        continue;
      }
      this.count = points.size();
      entropy += Math.log(density);
    }
    assert(0 < entropy && !Double.isInfinite(entropy));
  }

  public Object jsonValue() throws JSONException
  {
    JSONObject json = new JSONObject();
    json.put("bits per point", (Double.isInfinite(entropy)|Double.isNaN(entropy))?Double.toString(entropy):(entropy /= (count * Math.log(2))));
    json.put("invalid point fraction", ((double)invalid) / count);
    return json;
  }
  
}
