package com.simiacryptus.probabilityModel.benchmark.util;

public final class DoubleArrayMath extends DoubleArray
{

  public DoubleArrayMath()
  {
    super();
  }

  public DoubleArrayMath(DoubleArray toCopy)
  {
    super(toCopy);
  }

  public DoubleArrayMath sum(double right)
  {
    return DoubleArrayMath.sum(this, right);
  }

  public DoubleArrayMath product(double right)
  {
    return DoubleArrayMath.product(this, right);
  }

  public DoubleArrayMath sum(DoubleArray right)
  {
    return DoubleArrayMath.sum(this, right);
  }

  public DoubleArrayMath product(DoubleArray right)
  {
    return DoubleArrayMath.product(this, right);
  }

  public static DoubleArrayMath sum(DoubleArray left, double right)
  {
    DoubleArrayMath result = new DoubleArrayMath(left);
    final int c = result.position;
    final double[] v = result.values;
    for(int i=0;i<c;i++)
    {
      v[i] += right;
    }
    result.recalculateStatistics();
    return result;
  }

  public static DoubleArrayMath product(DoubleArray left, double right)
  {
    DoubleArrayMath result = new DoubleArrayMath(left);
    final int c = result.position;
    final double[] v = result.values;
    for(int i=0;i<c;i++)
    {
      v[i] *= right;
    }
    result.recalculateStatistics();
    return result;
  }

  public static DoubleArrayMath sum(DoubleArray left, DoubleArray right)
  {
    if(left.position != right.position) throw new IllegalArgumentException();
    DoubleArrayMath result = new DoubleArrayMath(left);
    final int c = result.position;
    final double[] l = result.values;
    final double[] r = right.values;
    for(int i=0;i<c;i++)
    {
      l[i] += r[i];
    }
    result.recalculateStatistics();
    return result;
  }

  public static DoubleArrayMath product(DoubleArray left, DoubleArray right)
  {
    if(left.position != right.position) throw new IllegalArgumentException();
    DoubleArrayMath result = new DoubleArrayMath(left);
    final int c = result.position;
    final double[] l = result.values;
    final double[] r = right.values;
    for(int i=0;i<c;i++)
    {
      l[i] *= r[i];
    }
    result.recalculateStatistics();
    return result;
  }

  public static DoubleArrayMath divide(DoubleArray left, DoubleArray right)
  {
    if(left.position != right.position) throw new IllegalArgumentException();
    DoubleArrayMath result = new DoubleArrayMath(left);
    final int c = result.position;
    final double[] l = result.values;
    final double[] r = right.values;
    for(int i=0;i<c;i++)
    {
      l[i] /= r[i];
    }
    result.recalculateStatistics();
    return result;
  }

  public DoubleArrayMath divide(DoubleArrayMath right)
  {
    return divide(this, right);
  }

  public DoubleArrayMath log()
  {
    final DoubleArrayMath copy = new DoubleArrayMath(this);
    final int c = copy.position;
    final double[] l = copy.values;
    for(int i=0;i<c;i++)
    {
      l[i] = Math.log(l[i]);
    }
    recalculateStatistics();
    return copy;
  }

  public DoubleArrayMath subtract(DoubleArrayMath right)
  {
    return sum(right.product(-1));
  }

  public double sumSq()
  {
    return product(this).sum;
  }
  
}