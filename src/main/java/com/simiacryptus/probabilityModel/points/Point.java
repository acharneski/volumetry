package com.simiacryptus.probabilityModel.points;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.util.Arrays;

public class Point
{
  private final double[] coords;
  
  public Point(double[] coords)
  {
    super();
    this.coords = Arrays.copyOf(coords, coords.length);
  }

  public double get(int d)
  {
    return coords[d];
  }
  
  public Point set(int d, double v)
  {
    coords[d] = v;
    return this;
  }
  
  public Point set(double[] v)
  {
    assert (v.length == coords.length);
    for (int d = 0; d < v.length; d++)
      coords[d] = v[d];
    return this;
  }
  
  public RealVector asRealVector()
  {
    return new ArrayRealVector(coords);
  }
  
  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(coords);
    return result;
  }
  
  @Override
  public boolean equals(Object obj)
  {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Point other = (Point) obj;
    if (!Arrays.equals(coords, other.coords)) return false;
    return true;
  }
  
  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append(Arrays.toString(coords));
    return builder.toString();
  }

  public double[] asArray()
  {
    return Arrays.copyOf(coords, coords.length);
  }
  
}
