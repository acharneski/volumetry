package com.simiacryptus.probabilityModel.optimizer;

public class OptimizerResult<P, R>
{
  public final P parameter;
  public final R result;
  
  public OptimizerResult(final P paramter, final R result)
  {
    super();
    this.parameter = paramter;
    this.result = result;
  }
}