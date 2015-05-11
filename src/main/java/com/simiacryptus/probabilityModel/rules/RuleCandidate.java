package com.simiacryptus.probabilityModel.rules;

import java.util.List;

import com.simiacryptus.probabilityModel.rules.fitness.VolumeDataDensity;
import com.simiacryptus.util.ObjUtil;

public final class RuleCandidate implements Comparable<RuleCandidate>
{
  public final PartitionRule     rule;
  public final double            fitness;
  public List<VolumeDataDensity> detail;
  
  public RuleCandidate(final PartitionRule rule, final double fitness)
  {
    super();
    this.rule = rule;
    this.fitness = fitness;
  }
  
  @Override
  public int compareTo(final RuleCandidate o)
  {
    int result = Double.valueOf(this.fitness).compareTo(o.fitness);
    if (0 == result)
    {
      result = Integer.valueOf(System.identityHashCode(this.rule)).compareTo(System.identityHashCode(0));
    }
    return result;
  }
  
  @Override
  public String toString()
  {
    try
    {
      return ObjUtil.toJson(this).toString(2);
    }
    catch (final Exception e)
    {
      return e.toString();
    }
  }
  
}