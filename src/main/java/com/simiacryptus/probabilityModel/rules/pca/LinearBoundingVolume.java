package com.simiacryptus.probabilityModel.rules.pca;

import com.simiacryptus.data.DoubleRange;
import com.simiacryptus.data.VolumeMetric;
import com.simiacryptus.lang.MathUtil;
import com.simiacryptus.probabilityModel.points.Point;
import com.simiacryptus.probabilityModel.volume.DoubleVolume;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.linear.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class LinearBoundingVolume
{

  private final RealMatrix toBounds;
  private final RealMatrix toCoords;
  private final DoubleVolume transformedBounds;
  private DoubleVolume canonicalBounds;
  private VolumeMetric volume;

  public LinearBoundingVolume(DoubleVolume bounds)
  {
    this(MatrixUtils.createRealIdentityMatrix(bounds.dimensions()), bounds);
  }

  public LinearBoundingVolume(RealMatrix matrix, DoubleVolume bounds)
  {
    this.toBounds = matrix;
    final LUDecomposition luDecomposition = new LUDecomposition(this.toBounds);
    this.toCoords = luDecomposition.getSolver().getInverse();
    this.transformedBounds = new DoubleVolume(bounds);
    this.volume = transformedBounds.getVolume().multiply(1./Math.abs(luDecomposition.getDeterminant()));
    if(this.volume.dimension < bounds.dimensions()) 
    {
      throw new IllegalArgumentException("Volume has collapsed at least one dimension");
    }
  }

  public LinearBoundingVolume slice(RealVector vector, DoubleRange bounds)
  {
    assert(MathUtil.doubleCompare(vector.getNorm(), 1, 0.01) == 0);
    DoubleRange newBound = DoubleRange.UNBOUNDED;
    for(Point p : getCoordinateVertices())
    {
      final double dot = vector.dotProduct(p.asRealVector());
      newBound = newBound.include(dot);
    }
    newBound = newBound.intersect(bounds);
    if(null == newBound)
    {
      return null;
    }
    
    TreeSet<LinearBoundingVolume> candidates = new TreeSet<LinearBoundingVolume>(new Comparator<LinearBoundingVolume>() {
      @Override
      public int compare(LinearBoundingVolume o1, LinearBoundingVolume o2)
      {
        final VolumeMetric v2 = o2.getVolume();
        final VolumeMetric v1 = o1.getVolume();
        int compare = 0;
        if(0 == compare) compare = -Double.compare(v1.dimension, v2.dimension);
        if(0 == compare) compare = Double.compare(v1.value, v2.value);
        return compare;
      }
    });
    for(int i=0;i<transformedBounds.dimensions();i++)
    {
      RealMatrix trialMatrix = toBounds.copy();
      trialMatrix.setRowVector(i, vector);
      DoubleVolume trialBounds = new DoubleVolume(transformedBounds);
      trialBounds.set(i, newBound);
      if(0 == new LUDecomposition(trialMatrix).getDeterminant()) continue;
      LinearBoundingVolume trialVolume = new LinearBoundingVolume(trialMatrix, trialBounds);
      candidates.add(trialVolume);
    }
    return candidates.first();
  }

  public DoubleVolume getCanonicalBounds()
  {
    if (null == canonicalBounds)
    {
      synchronized (this)
      {
        if (null == canonicalBounds)
        {
          canonicalBounds = DoubleVolume.unbounded(transformedBounds.dimensions());
          for(Point p : getCoordinateVertices())
          {
            canonicalBounds = canonicalBounds.include(p.asArray());
          }
        }
      }
    }
    return canonicalBounds;
  }

  public static RealVector multiply(RealMatrix right, RealVector left)
  {
    final RealVector result = right.operate(left);
    for(int d1=0;d1<left.getDimension();d1++)
    {
      final double element = left.getEntry(d1);
      if(element == Double.MAX_VALUE || element == -Double.MAX_VALUE)
      {
        for(int d2=0;d2<result.getDimension();d2++)
        {
          if(0 == right.getEntry(d1, d2)) continue;
          result.setEntry(d2, result.getEntry(d2)<0?-Double.MAX_VALUE:Double.MAX_VALUE);
        }
      }
    }
    for(int d2=0;d2<result.getDimension();d2++)
    {
      final double entry = result.getEntry(d2);
      assert(!Double.isInfinite(entry));
      assert(!Double.isNaN(entry));
    }
    return result;
  }

  public List<Point> getCoordinateVertices()
  {
    
    final ArrayList<Point> list = new ArrayList<Point>();
    for(Point p : getVectorspaceVertices())
    {
      list.add(new Point(multiply(toCoords, p.asRealVector()).toArray()));
    }
    return list;
  }

  
  public List<Point> getVectorspaceVertices()
  {
    final int dimensions = transformedBounds.dimensions();
    List<Point> vertices = new ArrayList<Point>();
    for (int n = 0; n < (1 << dimensions); n++)
    {
      double[] p = new double[dimensions];
      for (int d = 0; d < dimensions; d++)
      {
        final DoubleRange dimBound = this.transformedBounds.get(d);
        p[d] = (0 == (n & (1 << d))) ? dimBound.from : dimBound.to;
      }
      vertices.add(new Point(p));
    }
    return vertices;
  }

  public double[] sample()
  {
    return multiply(toCoords, new ArrayRealVector(transformedBounds.sample())).toArray();
  }

  public VolumeMetric getVolume()
  {
    return this.volume;
  }

  public boolean contains(double[] p)
  {
    final double[] test = multiply(toBounds, new ArrayRealVector(p)).toArray();
    for(int i=0;i<test.length;i++)
    {
      if(!transformedBounds.get(i).contains(test[i])) return false;
    }
    return true;
  }

  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    for(int i=0;i<transformedBounds.dimensions();i++)
    {
      final DoubleRange bound = transformedBounds.get(i);
      final double[] vector = toBounds.getRow(i);
      builder.append(String.format("%s < %s < %s\n", transformedBounds.get(i).from, Arrays.toString(vector), bound.to));
    }
    return builder.toString();
  }

  public JSONObject toJson() throws OutOfRangeException, JSONException
  {
    final JSONArray vectorSpace = new JSONArray();
    for(int i=0;i<transformedBounds.dimensions();i++)
    {
      final JSONObject dim = new JSONObject();
      dim.put("vector", toBounds.getRow(i));
      dim.put("bound", transformedBounds.get(i).toJson());
      vectorSpace.put(dim);
    }
    final JSONObject json = new JSONObject();
    json.put("vertices", getCoordinateVertices());
    json.put("vectors", vectorSpace);
    json.put("outer", getCanonicalBounds().toJson());
    return json;
  }
  
  
  
}
