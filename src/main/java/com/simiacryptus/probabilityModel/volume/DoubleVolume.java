package com.simiacryptus.probabilityModel.volume;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

import javax.naming.OperationNotSupportedException;

import org.json.JSONException;
import org.json.JSONObject;

import com.simiacryptus.data.DoubleRange;
import com.simiacryptus.data.VolumeMetric;
import com.simiacryptus.lang.JsonUtil;

@SuppressWarnings("serial")
public class DoubleVolume extends ArrayList<DoubleRange> implements SpacialVolume
{
  
  public static final Random random = new Random();
  
  public DoubleVolume(final Collection<? extends DoubleRange> array)
  {
    super(array);
    for (DoubleRange item : array)
      if (null == item) throw new NullPointerException();
    if (0 == array.size())
    {
      throw new InvalidParameterException();
    }
    assert (validate());
  }
  
  public DoubleVolume(int dimensions)
  {
    super(dimensions);
    for (int i = 0; i < dimensions; i++)
    {
      add(DoubleRange.UNBOUNDED);
    }
    assert (validate());
  }
  
  public DoubleVolume(final DoubleRange... ranges)
  {
    super(Arrays.asList(ranges));
    assert (validate());
  }
  
  @Override
  public DoubleRange set(int index, DoubleRange element)
  {
    assert (validate());
    if (null == element) throw new NullPointerException();
    final DoubleRange set = super.set(index, element);
    assert (validate());
    return set;
  }
  
  @Override
  public boolean add(DoubleRange e)
  {
    assert (validate());
    if (null == e) throw new NullPointerException();
    final boolean add = super.add(e);
    assert (validate());
    return add;
  }
  
  @Override
  public void add(int index, DoubleRange element)
  {
    assert (validate());
    if (null == element) throw new NullPointerException();
    super.add(index, element);
    assert (validate());
  }
  
  @Override
  public DoubleRange remove(int index)
  {
    throw new NullPointerException();
  }
  
  public String getHash()
  {
    final StringBuilder sb = new StringBuilder();
    for (DoubleRange r : this)
    {
      if (sb.length() > 0) sb.append("-");
      sb.append(Integer.toHexString(r.hashCode()));
    }
    return sb.toString();
  }
  
  @Override
  public boolean remove(Object o)
  {
    throw new NullPointerException();
  }
  
  @Override
  public boolean addAll(Collection<? extends DoubleRange> c)
  {
    assert (validate());
    for (DoubleRange item : c)
      if (null == item) throw new NullPointerException();
    final boolean addAll = super.addAll(c);
    assert (validate());
    return addAll;
  }
  
  private boolean validate()
  {
    for (DoubleRange item : this)
      if (null == item) return false;
    return true;
  }
  
  @Override
  public boolean addAll(int index, Collection<? extends DoubleRange> c)
  {
    assert (validate());
    for (DoubleRange item : c)
      if (null == item) throw new NullPointerException();
    final boolean addAll = super.addAll(index, c);
    assert (validate());
    return addAll;
  }
  
  @Override
  protected void removeRange(int fromIndex, int toIndex)
  {
    throw new NullPointerException();
  }
  
  @Override
  public boolean contains(final double[] point)
  {
    assert (validate());
    for (int i = 0; i < this.size(); i++)
    {
      if (!this.get(i).contains(point[i]))
      {
        return false;
      }
    }
    return true;
  }
  
  @Override
  public int dimensions()
  {
    assert (validate());
    return this.size();
  }
  
  /*
   * (non-Javadoc)
   * @see com.simiacryptus.data.SpacialVolume#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj)
  {
    assert (validate());
    if (this == obj)
    {
      return true;
    }
    if (obj == null)
    {
      return false;
    }
    if (this.getClass() != obj.getClass())
    {
      return false;
    }
    if (!super.equals(obj))
    {
      return false;
    }
    return true;
  }
  
  @Override
  public DoubleVolume getBounds()
  {
    assert (validate());
    return this;
  }
  
  @Override
  public DoubleRange getRange(final int d)
  {
    assert (validate());
    return this.get(d);
  }
  
  /*
   * (non-Javadoc)
   * @see com.simiacryptus.data.SpacialVolume#getVolume()
   */
  @Override
  public VolumeMetric getVolume()
  {
    assert (validate());
    int dimension = 0;
    double value = 1;
    for (final DoubleRange range : this)
    {
      if (Math.abs(range.from) == Double.MAX_VALUE) continue;
      if (Math.abs(range.to) == Double.MAX_VALUE) continue;
      final double size = range.size();
      if (0 == size) continue;
      if (Double.isInfinite(size)) continue;
      value *= size;
      dimension++;
    }
    if (Double.isInfinite(value))
    {
      value = Double.MAX_VALUE;
    }
    return new VolumeMetric(value, dimension);
  }
  
  /*
   * (non-Javadoc)
   * @see com.simiacryptus.data.SpacialVolume#intersect(com.simiacryptus.data.DoubleVolume)
   */
  @Override
  public SpacialVolume intersect(final SpacialVolume right)
  {
    if(null==right) 
    {
      return null;
    }
    assert (validate());
    if (right instanceof DoubleVolume)
    {
      final DoubleVolume right2 = (DoubleVolume) right;
      final DoubleRange[] intersection = new DoubleRange[right2.size()];
      for (int i = 0; i < intersection.length; i++)
      {
        final DoubleRange a = this.get(i);
        final DoubleRange b = right2.get(i);
        final DoubleRange intersect = a.intersect(b);
        if (null == intersect)
        {
          return null;
        }
        intersection[i] = intersect;
      }
      return new DoubleVolume(intersection);
    }
    else
    {
      return right.intersect(this);
    }
  }
  
  /*
   * (non-Javadoc)
   * @see com.simiacryptus.data.SpacialVolume#intersects(com.simiacryptus.data.DoubleVolume)
   */
  @Override
  public boolean intersects(final SpacialVolume range)
  {
    assert (validate());
    if (range instanceof DoubleVolume)
    {
      final DoubleVolume range2 = (DoubleVolume) range;
      for (int i = 0; i < this.size(); i++)
      {
        final DoubleRange a = range2.get(i);
        final DoubleRange b = this.get(i);
        if (!a.intersects(b))
        {
          return false;
        }
      }
      return true;
    }
    else
    {
      return range.intersects(this);
    }
  }
  
  @Override
  public Iterable<double[]> points()
  {
    assert (validate());
    return new Iterable<double[]>() {
      @Override
      public Iterator<double[]> iterator()
      {
        return new Iterator<double[]>() {
          
          @Override
          public boolean hasNext()
          {
            return true;
          }
          
          @Override
          public double[] next()
          {
            return DoubleVolume.this.sample();
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
  
  /*
   * (non-Javadoc)
   * @see com.simiacryptus.data.SpacialVolume#sample(java.util.Random)
   */
  @Override
  public double[] sample()
  {
    assert (validate());
    final double[] point = new double[this.size()];
    for (int i = 0; i < point.length; i++)
    {
      point[i] = this.get(i).sample(random);
    }
    return point;
  }
  
  /*
   * (non-Javadoc)
   * @see com.simiacryptus.data.SpacialVolume#toJson()
   */
  @Override
  public JSONObject toJson() throws JSONException
  {
    assert (validate());
    final JSONObject json = new JSONObject();
    json.put("dimensions", JsonUtil.toJsonArray(this));
    json.put("hash", getHash());
    return json;
  }
  
  public static DoubleVolume unbounded(int dimensions)
  {
    final DoubleRange[] array = new DoubleRange[dimensions];
    Arrays.fill(array, DoubleRange.UNBOUNDED);
    return new DoubleVolume(array);
  }

  @Override
  public boolean isUnbounded()
  {
    for(DoubleRange d : this)
    {
      if(d.isUnbounded()) return true;
    }
    return false;
  }

  public DoubleVolume include(double[] point)
  {
    final DoubleVolume copy = new DoubleVolume();
    int i = 0;
    for(DoubleRange d : this)
    {
      copy.add(d.include(point[i++]));
    }
    return copy;
  }

  @Override
  public SpacialVolume union(SpacialVolume right)
  {
    assert (validate());
    if (right instanceof DoubleVolume)
    {
      final DoubleVolume right2 = (DoubleVolume) right;
      final DoubleRange[] unionArray = new DoubleRange[right2.size()];
      for (int i = 0; i < unionArray.length; i++)
      {
        final DoubleRange a = this.get(i);
        final DoubleRange b = right2.get(i);
        final DoubleRange union = a.union(b);
        if (null == union)
        {
          return null;
        }
        unionArray[i] = union;
      }
      return new DoubleVolume(unionArray);
    }
    else
    {
      return right.union(this);
    }
  }
  
}