package com.simiacryptus.codes;

public class HammingSymbol<T>
{
  
  public final T   key;
  public final int count;
  
  public HammingSymbol(final int count, final T key)
  {
    this.count = count;
    this.key = key;
  }
  
}