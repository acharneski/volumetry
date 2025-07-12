package com.simiacryptus.lang;

public class MathUtil
{
  
  private static final double log2 = Math.log(2);
  
  public static double log2(final double value)
  {
    return Math.log(value) / log2;
  }

  public static int doubleCompare(double left, int right, double tolerance)
  {
    if(left<right)
    {
      if(left<right-tolerance)
      {
        return -1;
      }
      else
      {
        return 0;
      }
    }
    else
    {
      if(left<right+tolerance)
      {
        return 0;
      }
      else
      {
        return 1;
      }
    }
  }
  
}
