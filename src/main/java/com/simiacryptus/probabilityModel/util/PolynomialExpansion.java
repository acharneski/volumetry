package com.simiacryptus.probabilityModel.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

public class PolynomialExpansion
{
  
  private static class PolyTerm implements Comparable<PolyTerm>
  {
    private final TreeMap<Integer, Integer> powers = new TreeMap<Integer, Integer>();
    
    public PolyTerm(final int index, final int power)
    {
      super();
      this.powers.put(power == 0 ? 0 : index, power);
    }
    
    public PolyTerm(final PolyTerm toCopy)
    {
      super();
      this.powers.putAll(toCopy.powers);
    }
    
    @Override
    public int compareTo(final PolyTerm right)
    {
      int compareTo = Integer.valueOf(this.getHighestPower()).compareTo(right.getHighestPower());
      if (0 == compareTo)
      {
        compareTo = Integer.valueOf(this.getSumPower()).compareTo(right.getSumPower());
      }
      if (0 == compareTo)
      {
        final Iterator<Entry<Integer, Integer>> iteratorLeft = this.powers.entrySet().iterator();
        final Iterator<Entry<Integer, Integer>> iteratorRight = right.powers.entrySet().iterator();
        while (0 == compareTo && iteratorLeft.hasNext() && iteratorRight.hasNext())
        {
          final Entry<Integer, Integer> leftEntry = iteratorLeft.next();
          final Entry<Integer, Integer> rightEntry = iteratorRight.next();
          if (0 == compareTo)
          {
            compareTo = Integer.valueOf(leftEntry.getKey()).compareTo(rightEntry.getKey());
          }
          if (0 == compareTo)
          {
            compareTo = Integer.valueOf(leftEntry.getValue()).compareTo(rightEntry.getValue());
          }
        }
        if (0 == compareTo && iteratorLeft.hasNext())
        {
          compareTo = 1;
        }
        if (0 == compareTo && iteratorRight.hasNext())
        {
          compareTo = -1;
        }
      }
      return compareTo;
    }
    
    public double evaluate(final double[] input)
    {
      double value = 1;
      for (final Entry<Integer, Integer> e : this.powers.entrySet())
      {
        value *= Math.pow(input[e.getKey()], e.getValue());
      }
      return value;
    }
    
    public int getHighestPower()
    {
      int highestPower = 0;
      for (final int exponent : this.powers.values())
      {
        if (exponent > highestPower)
        {
          highestPower = exponent;
        }
      }
      return highestPower;
    }
    
    public int getSumPower()
    {
      int sumPower = 0;
      for (final int exponent : this.powers.values())
      {
        sumPower += exponent;
      }
      return sumPower;
    }
    
    public boolean hasCommonElement(final PolyTerm right)
    {
      for (final int index : this.powers.keySet())
      {
        if (right.powers.containsKey(index))
        {
          return true;
        }
      }
      return false;
    }
    
    public PolyTerm multiply(final int index, final int power)
    {
      final PolyTerm copy = new PolyTerm(this);
      final Integer power2 = copy.powers.get(index);
      copy.powers.put(index, power + (power2 == null ? 0 : power2));
      return copy;
    }
    
    public PolyTerm multiply(final PolyTerm right)
    {
      PolyTerm copy = new PolyTerm(this);
      for (final Entry<Integer, Integer> e : right.powers.entrySet())
      {
        copy = copy.multiply(e.getKey(), e.getValue());
      }
      return copy;
    }
    
    @Override
    public String toString()
    {
      final StringBuilder builder = new StringBuilder();
      for (final Entry<Integer, Integer> e : this.powers.entrySet())
      {
        builder.append("(");
        builder.append(e.getKey());
        builder.append("^");
        builder.append(e.getValue());
        builder.append(")");
      }
      return builder.toString();
    }
    
  }
  
  private static PolynomialExpansion generate(final int size, final int... powers)
  {
    final PolynomialExpansion a = new PolynomialExpansion();
    for (int i = 0; i < size; i++)
    {
      final int firstPower = powers[0];
      a.terms.add(new PolyTerm(i, firstPower));
    }
    if (1 == powers.length)
    {
      return a;
    }
    final PolynomialExpansion b = generate(size, Arrays.copyOfRange(powers, 1, powers.length));
    final PolynomialExpansion c = new PolynomialExpansion();
    for (final PolyTerm termA : a.terms)
    {
      for (final PolyTerm termB : b.terms)
      {
        if (!termA.hasCommonElement(termB))
        {
          c.add(termA.multiply(termB));
        }
      }
    }
    return c;
  }
  
  public static PolynomialExpansion generate(final int size, final int[]... powerList)
  {
    PolynomialExpansion root = new PolynomialExpansion();
    for (final int[] powers : powerList)
    {
      final PolynomialExpansion temp = generate(size, powers);
      if (null == temp)
      {
        throw new RuntimeException();
      }
      root = root.add(temp);
    }
    return root;
  }
  
  private final TreeSet<PolyTerm> terms = new TreeSet<PolynomialExpansion.PolyTerm>();
  
  private PolynomialExpansion()
  {
  }
  
  private PolynomialExpansion(final PolynomialExpansion polynomialExpansion)
  {
    this.terms.addAll(polynomialExpansion.terms);
  }
  
  public PolynomialExpansion add(final PolynomialExpansion generate)
  {
    final PolynomialExpansion copy = new PolynomialExpansion(this);
    for (final PolyTerm term : generate.terms)
    {
      copy.terms.add(term);
    }
    return copy;
  }
  
  private void add(final PolyTerm right)
  {
    this.terms.add(right);
  }
  
  public double[] expand(final double[] input)
  {
    final double[] data = new double[this.terms.size()];
    int i = 0;
    for (final PolyTerm term : this.terms)
    {
      data[i++] = term.evaluate(input);
    }
    return data;
  }
  
  public int terms()
  {
    return this.terms.size();
  }
  
  @Override
  public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    for (final PolyTerm term : this.terms)
    {
      if (0 < builder.length())
      {
        builder.append("+");
      }
      builder.append(term.toString());
    }
    return builder.toString();
  }
  
}
