package com.simiacryptus.probabilityModel.volume;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.json.JSONException;
import org.json.JSONObject;

import com.simiacryptus.data.DoubleRange;
import com.simiacryptus.data.LongRange;
import com.simiacryptus.data.LongVolume;
import com.simiacryptus.data.ResolutionMapping;
import com.simiacryptus.lang.JsonUtil;

@SuppressWarnings("serial")
public class VolumeMapping extends ArrayList<ResolutionMapping>
{
  
  public VolumeMapping(final Collection<ResolutionMapping> array)
  {
    super(array);
  }
  
  public VolumeMapping(final ResolutionMapping... ranges)
  {
    super(Arrays.asList(ranges));
  }
  
  public double[] canonicalValue(final double[] point)
  {
    final double[] values = new double[point.length];
    for (int i = 0; i < values.length; i++)
    {
      values[i] = this.get(i).canonicalValue(point[i]);
    }
    return values;
  }
  
  public boolean equals(final double[] p, final double[] d)
  {
    for (int i = 0; i < this.size(); i++)
    {
      if (!this.get(i).equals(p[i], d[i]))
      {
        return false;
      }
    }
    return true;
  }
  
  @Override
  public boolean equals(final Object obj)
  {
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
  
  public double[] fromIndex(final long[] point)
  {
    final double[] values = new double[point.length];
    for (int i = 0; i < values.length; i++)
    {
      values[i] = this.get(i).fromIndex(point[i]);
    }
    return values;
  }
  
  public DoubleVolume fromIndex(final LongVolume range)
  {
    final ArrayList<DoubleRange> list = new ArrayList<DoubleRange>();
    for (int i = 0; i < this.size(); i++)
    {
      list.add(this.get(i).fromIndex(range.getRange(i)));
    }
    return new DoubleVolume(list);
  }
  
  public DoubleVolume getDoubleRange()
  {
    final DoubleRange[] longRanges = new DoubleRange[this.size()];
    for (int i = 0; i < longRanges.length; i++)
    {
      longRanges[i] = this.get(i);
    }
    return new DoubleVolume(this);
  }
  
  public LongVolume getLongRange()
  {
    final LongRange[] longRanges = new LongRange[this.size()];
    for (int i = 0; i < longRanges.length; i++)
    {
      longRanges[i] = this.get(i).getIndexRange();
    }
    return new LongVolume(longRanges);
  }
  
  public long[] toIndex(final double[] point)
  {
    final long[] values = new long[point.length];
    for (int i = 0; i < values.length; i++)
    {
      values[i] = this.get(i).toIndex(point[i]);
    }
    return values;
  }
  
  public LongVolume toIndex(final SpacialVolume volume)
  {
    final LongRange[] longRanges = new LongRange[volume.dimensions()];
    for (int i = 0; i < longRanges.length; i++)
    {
      longRanges[i] = this.get(i).toIndex(volume.getRange(i));
    }
    return new LongVolume(longRanges);
  }
  
  public JSONObject toJson() throws JSONException
  {
    final JSONObject json = new JSONObject();
    json.put("dimensions", JsonUtil.toJsonArray(this));
    return json;
  }
  
  @Override
  public String toString()
  {
    try
    {
      return this.toJson().toString();
    }
    catch (final JSONException e)
    {
      return e.getMessage();
    }
  }
  
}