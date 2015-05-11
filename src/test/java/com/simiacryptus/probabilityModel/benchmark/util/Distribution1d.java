package com.simiacryptus.probabilityModel.benchmark.util;

import java.util.Arrays;

public final class Distribution1d extends DoubleArray
{
  boolean  sorted   = false;
  
  public Distribution1d()
  {
    super();
  }

  public Distribution1d(DoubleArray toCopy)
  {
    super(toCopy);
  }

  private void sort()
  {
    if(!sorted)
    {
      Arrays.sort(values);
      sorted = true;
    }
  }
  
  @Override
  public void add(double v)
  {
    sorted = false;
    super.add(v);
  }

  public double maxDifference(Distribution1d right)
  {
    sort();
    double maxDiff = -Double.MAX_VALUE;
    for(int i=0;i<position;i++)
    {
      double fraction = ((double)i) / position;
      int indexR = (int) (fraction * right.position);
      final double leftV = values[i];
      final double rightV = right.values[indexR];
      double diff = Math.abs(leftV - rightV);
      if(maxDiff < diff) 
      {
        maxDiff = diff;
      }
    }
    return maxDiff;
  }
  
  public double getFraction(double v)
  {
    return ((double) indexOf(v)) / position;
  }
  
  public int indexOf(double v)
  {
    sort();
    int index = Arrays.binarySearch(values, v);
    if (0 > index)
    {
      index = -index;
    }
    return index;
  }
  
  public double fromFraction(double v)
  {
    sort();
    return values[(int) v * position];
  }
}