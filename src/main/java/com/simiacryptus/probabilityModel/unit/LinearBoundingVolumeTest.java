package com.simiacryptus.probabilityModel.unit;

import junit.framework.Assert;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.junit.Test;

import com.simiacryptus.data.DoubleRange;
import com.simiacryptus.lang.LOG;
import com.simiacryptus.probabilityModel.rules.pca.LinearBoundingVolume;
import com.simiacryptus.probabilityModel.volume.DoubleVolume;

public class LinearBoundingVolumeTest
{
  @Test
  public void test()
  {
    LinearBoundingVolume v;
    {
      final DoubleVolume totalRange = new DoubleVolume(
          new DoubleRange(0, 1), new DoubleRange(0, 1), new DoubleRange(0, 1));
      v = new LinearBoundingVolume(totalRange);
      Assert.assertEquals(1, v.getVolume().value, 0.01);
      Assert.assertTrue(v.contains(new double[]{0.1,0.1,0.1}));
      Assert.assertTrue(!v.contains(new double[]{-0.1,-0.1,-0.1}));
      Assert.assertEquals(1.0, v.getVolume().value, 0.01);
      Assert.assertEquals(1.0, v.getCanonicalBounds().getVolume().value, 0.01);
      for(int i=0;i<100;i++)
      {
        Assert.assertTrue(v.contains(v.sample()));
      }
      LOG.d(v.toString());
    }

    {
      RealVector vector = new ArrayRealVector(new double[]{1,1,1});
      vector = vector.mapMultiply(1./vector.getNorm());
      final ArrayRealVector boundaryPointA = new ArrayRealVector(new double[]{0,0,0});
      final double p1 = 0.4;
      final ArrayRealVector boundaryPointB = new ArrayRealVector(new double[]{p1,0,0});
      final DoubleRange range = new DoubleRange(
          vector.dotProduct(boundaryPointA), 
          vector.dotProduct(boundaryPointB));
      LinearBoundingVolume x = v.slice(vector, range);
      Assert.assertEquals(p1, x.getVolume().value, 0.01);
      Assert.assertTrue(x.contains(new double[]{0.1,0.1,0.1}));
      Assert.assertTrue(!x.contains(new double[]{0.8,0.8,0.8}));
      Assert.assertTrue(!x.contains(new double[]{-0.1,-0.1,-0.1}));
      Assert.assertEquals(2.4, x.getCanonicalBounds().getVolume().value, 0.01);
      for(int i=0;i<100;i++)
      {
        Assert.assertTrue(x.contains(x.sample()));
      }
      LOG.d(x.toString());
    }
    {
      RealVector vector = new ArrayRealVector(new double[]{1,1,0});
      vector = vector.mapMultiply(1./vector.getNorm());
      final ArrayRealVector boundaryPointA = new ArrayRealVector(new double[]{0,0,0});
      final double p1 = 0.999999;
      final ArrayRealVector boundaryPointB = new ArrayRealVector(new double[]{p1,0,0});
      final DoubleRange range = new DoubleRange(
          vector.dotProduct(boundaryPointA), 
          vector.dotProduct(boundaryPointB));
      LinearBoundingVolume x = v.slice(vector, range);
      Assert.assertEquals(p1, x.getVolume().value, 0.01);
      Assert.assertTrue(x.contains(new double[]{0.1,0.1,0.1}));
      Assert.assertTrue(!x.contains(new double[]{0.8,0.8,0.8}));
      Assert.assertTrue(!x.contains(new double[]{-0.1,-0.1,-0.1}));
      Assert.assertEquals(2, x.getCanonicalBounds().getVolume().value, 0.01);
      for(int i=0;i<100;i++)
      {
        Assert.assertTrue(x.contains(x.sample()));
      }
      LOG.d(x.toString());
    }
    
//    final LinearMetric metric = new LinearMetric(new ArrayRealVector(new double[]{1,1,1}), 1, new ArrayRealVector(new double[]{1,1,1}));
//    final LinearMetricRule linearMetricRule = new LinearMetricRule(totalRange, metric, new double[]{0.9,1.1});
//    new LinearVolume(totalRange, metric, new Double())
//    v.slice(linearMetricRule.metric., bounds)
    
  }
}
