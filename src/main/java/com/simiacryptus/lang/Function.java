package com.simiacryptus.lang;

public interface Function<P, R>
{
  public R evaluate(P value);
}