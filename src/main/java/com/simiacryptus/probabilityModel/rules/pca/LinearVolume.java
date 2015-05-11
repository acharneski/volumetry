package com.simiacryptus.probabilityModel.rules.pca;

import java.util.Iterator;

import javax.naming.OperationNotSupportedException;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.json.JSONException;
import org.json.JSONObject;

import com.simiacryptus.data.DoubleRange;
import com.simiacryptus.data.VolumeMetric;
import com.simiacryptus.lang.NotImplementedException;
import com.simiacryptus.probabilityModel.volume.DoubleVolume;
import com.simiacryptus.probabilityModel.volume.SpacialVolume;
import com.simiacryptus.util.ObjectUtil;

public class LinearVolume implements SpacialVolume
{
  
  public SpacialVolume parent;
  public DoubleRange bounds;
  private RealVector vector;
  protected boolean isZeroVolume = false;
  protected LinearBoundingVolume boundingBox;
  private int sampledPoints = 0;
  private int matchedPoints = 0;
  private LinearMetric metric;
  private DoubleRange metricRange;
  
  public static LinearBoundingVolume getBoundingBox(SpacialVolume volume)
  {
    if(volume instanceof LinearVolume)
    {
      return ((LinearVolume)volume).boundingBox;
    }
    else
    {
      return new LinearBoundingVolume(volume.getBounds());
    }
  }

  
  public static LinearVolume intersect(SpacialVolume range, LinearMetric metric, DoubleRange metricRange)
  {
    // TODO: These variable names suck...
    final LinearVolume linearVolume = new LinearVolume();
    linearVolume.parent = range;
    linearVolume.metric = metric;
    linearVolume.metricRange = metricRange;
    assert(Math.abs(metric.eigenvector.getNorm() - 1.) < 0.01);
    final double offset = metric.eigenvector.dotProduct(metric.centroid);
    linearVolume.vector = metric.eigenvector;
    linearVolume.bounds = new DoubleRange(metricRange.from + offset, metricRange.to + offset);
    final LinearBoundingVolume rangeBound = getBoundingBox(range);
    linearVolume.boundingBox = rangeBound.slice(linearVolume.vector, linearVolume.bounds);
    if(null == linearVolume.boundingBox)
    {
      return null;
    }
    return linearVolume;
  }

  @Override
  public boolean contains(double[] point)
  {
    if(!bounds.contains(this.vector.dotProduct(new ArrayRealVector(point)))) return false;
    if(!parent.contains(point)) return false;
    return true;
  }
  
  @Override
  public int dimensions()
  {
    return parent.dimensions();
  }
  
  @Override
  public DoubleVolume getBounds()
  {
    return boundingBox.getCanonicalBounds();
  }
  
  @Override
  public DoubleRange getRange(int d)
  {
    return getBounds().get(d);
  }
  
  @Override
  public VolumeMetric getVolume()
  {
    VolumeMetric volumeScalar = boundingBox.getVolume();
    if(sampledPoints > 0)
    {
      volumeScalar = volumeScalar.multiply(((double)matchedPoints) / sampledPoints);
    }
    return volumeScalar;
  }
  
  @Override
  public SpacialVolume intersect(SpacialVolume right)
  {
    final SpacialVolume newParent = this.parent.intersect(right);
    if(null == newParent) return null;
    return LinearVolume.intersect(newParent, metric, metricRange);
  }
  
  @Override
  public boolean intersects(SpacialVolume range)
  {
    return getBounds().intersects(range) && parent.intersects(range);
  }
  
  @Override
  public Iterable<double[]> points()
  {
    return new Iterable<double[]>() {
      
      @Override
      public Iterator<double[]> iterator()
      {
        return new Iterator<double[]>() {
          @Override
          public boolean hasNext()
          {
            return !isZeroVolume;
          }
          
          @Override
          public double[] next()
          {
            if (isZeroVolume)
            {
              return null;
            }
            return sample();
          }
          
          @Override
          public void remove()
          {
            throw new RuntimeException(new OperationNotSupportedException());
          }
        };
      }
    };
  }
  
  @Override
  public double[] sample()
  {
    double[] p;
    do{
      sampledPoints++;
      p = boundingBox.sample();
    } while(!parent.contains(p));
    matchedPoints++;
    return p;
  }
  
  @Override
  public JSONObject toJson() throws JSONException
  {
    final JSONObject json = new JSONObject();
    json.put("parent", parent.toJson());
    json.put("box", null==boundingBox?"null":boundingBox.toJson());
    json.put("bounds", bounds.toJson());
    return json;
  }
  
  @Override
  public boolean isUnbounded()
  {
    return getBounds().isUnbounded();
  }
  
  @SuppressWarnings("deprecation")
  @Override
  public SpacialVolume union(SpacialVolume region)
  {
    throw new NotImplementedException();
  }


  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append(ObjectUtil.getId(this));
    builder.append(" ");
    try
    {
      builder.append(toJson().toString(2));
    }
    catch (JSONException e)
    {
      builder.append(e.toString());
    }
    return builder.toString();
  }
 
  
  
}