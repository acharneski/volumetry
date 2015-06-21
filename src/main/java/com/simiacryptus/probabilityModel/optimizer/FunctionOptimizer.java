package com.simiacryptus.probabilityModel.optimizer;

import com.simiacryptus.probabilityModel.rules.fitness.IndexFitnessFunction;

public abstract class FunctionOptimizer<P, R extends Comparable<R>>
{
  public abstract OptimizerResult<P, R> optimize(IndexFitnessFunction function);
}