package com.simiacryptus.probabilityModel.benchmark.statistics;

import org.json.JSONException;
import org.json.JSONObject;

import com.simiacryptus.probabilityModel.benchmark.base.DataDistribution;
import com.simiacryptus.probabilityModel.benchmark.base.DataSampler;
import com.simiacryptus.probabilityModel.benchmark.util.Distribution1d;
import com.simiacryptus.probabilityModel.benchmark.util.DoubleArrayMath;
import com.simiacryptus.probabilityModel.volume.SpacialVolume;

public class DensityComparison
{
  private Object jsonValue;

  public DensityComparison(DataDistribution left, DataDistribution right) throws JSONException
  {
    DoubleArrayMath leftProbs = new DoubleArrayMath();
    DoubleArrayMath rightProbs = new DoubleArrayMath();
    SpacialVolume volume = left.getVolume().union(right.getVolume());
    for (int i = 0; i < 10000; i++)
    {
      double[] p = volume.sample();
      leftProbs.add(left.getDensity(p));
      rightProbs.add(right.getDensity(p));
    }
    if(left instanceof DataSampler)
    {
      for (double[] p : ((DataSampler)left).getPoints(10000))
      {
        leftProbs.add(left.getDensity(p));
        rightProbs.add(right.getDensity(p));
      }
    }
    if(right instanceof DataSampler)
    {
      for (double[] p : ((DataSampler)right).getPoints(10000))
      {
        leftProbs.add(left.getDensity(p));
        rightProbs.add(right.getDensity(p));
      }
    }
    DoubleArrayMath leftNorm1Probs = leftProbs.product(1./leftProbs.sum());
    DoubleArrayMath rightNorm1Probs = rightProbs.product(1./rightProbs.sum());

    DoubleArrayMath leftNorm2Probs = leftProbs.product(1./leftProbs.sumSq());
    DoubleArrayMath rightNorm2Probs = rightProbs.product(1./rightProbs.sumSq());

    
    
    //double mean = densityDifference.mean();
    //double maxDensityHistoDiff = new Distribution1d(modelDensities).maxDifference(new Distribution1d(dataDensities));
    JSONObject json = new JSONObject();
    
    {
      double stdDev = leftNorm1Probs.subtract(rightNorm1Probs).stdDev();
      json.put("stdDev_norm1", stdDev==0?"Infinity":1./stdDev);
    }
    {
      double total = 0;
      for(int i=0;i<leftNorm1Probs.getCount();i++)
      {
        total += Math.abs(leftNorm1Probs.get(i) - rightNorm1Probs.get(i));
      }
      json.put("diff_norm1", total);
    }
    {
      double total = 0;
      for(int i=0;i<leftNorm1Probs.getCount();i++)
      {
        double x = leftNorm1Probs.get(i) - rightNorm1Probs.get(i);
        total += x*x;
      }
      json.put("dist_norm1", Math.sqrt(total/leftNorm1Probs.getCount()));
    }
    {
      double total = 0;
      for(int i=0;i<leftNorm1Probs.getCount();i++)
      {
        double x = leftNorm2Probs.get(i) * rightNorm2Probs.get(i);
        total += x;
      }
      json.put("angle_norm2", ((2/Math.PI)*Math.acos(total)));
    }

    jsonValue = json;
  }

  public Object jsonValue() throws JSONException
  {
    return jsonValue;
  }
}