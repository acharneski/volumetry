package com.simiacryptus.lang;

public class MathUtil
{
  
  private static final double log2 = Math.log(2);
  
  public static double log2(final double value)
  {
    return Math.log(value) / log2;
  }

  public static double distance(double[] value, double[] result)
  {
    assert(value.length == result.length);
    double distance = 0;
    for(int i=0;i<value.length;i++)
    {
      distance += Math.pow(result[i] - value[i], 2);
    }
    distance = Math.sqrt(distance);
    return distance;
  }

  public static double min(double... args)
  {
    double winner = args[0];
    for(int i=1;i<args.length;i++)
    {
      double v = args[i];
      if(winner > v) winner = v;
    }
    return winner;
  }

  public static double max(double... args)
  {
    double winner = args[0];
    for(int i=1;i<args.length;i++)
    {
      double v = args[i];
      if(winner > v) winner = v;
    }
    return winner;
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
