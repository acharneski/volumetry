package com.simiacryptus.probabilityModel.benchmark.base;

public abstract class TestObject
{

  public String getName()
  {
    return getClass().getSimpleName();
  }

  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append(getClass().getSimpleName());
    builder.append("@");
    builder.append(Integer.toHexString(System.identityHashCode(this)));
    return builder.toString();
  }
  
}
