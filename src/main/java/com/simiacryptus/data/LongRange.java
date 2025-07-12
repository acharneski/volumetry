package com.simiacryptus.data;

import org.json.JSONException;
import org.json.JSONObject;

public class LongRange
{
  public final long from;
  public final long to;
  public final int  bitDepth;
  
  public LongRange(final long from, final long to)
  {
    super();
    if (from > to) { throw new IllegalArgumentException(); }
    this.from = from;
    this.to = to;
    this.bitDepth = (int) Math.ceil(Math.log(1 + (to - from)) / Math.log(2));
  }
  
  public boolean contains(final long value)
  {
    if (this.from > value) { return false; }
    if (this.to <= value) { return false; }
    return true;
  }

  @Override
  public boolean equals(final Object obj)
  {
    if (this == obj) { return true; }
    if (obj == null) { return false; }
    if (this.getClass() != obj.getClass()) { return false; }
    final LongRange other = (LongRange) obj;
    if (this.from != other.from) { return false; }
    if (this.size() != other.size()) { return false; }
    return true;
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (this.from ^ this.from >>> 32);
    result = prime * result + (int) (this.size() ^ this.size() >>> 32);
    return result;
  }

  public long size()
  {
    final long size = this.to - this.from;
    if (0 == size) { return 1; }
    return size;
  }

  public JSONObject toJson() throws JSONException
  {
    final JSONObject dimJson = new JSONObject();
    dimJson.put("from", this.from);
    dimJson.put("to", this.to);
    dimJson.put("bitDepth", this.bitDepth);
    return dimJson;
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