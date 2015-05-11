package com.simiacryptus.probabilityModel.visitors;

import java.util.concurrent.Executors;

import com.simiacryptus.probabilityModel.model.PointNode;
import com.simiacryptus.probabilityModel.rules.PartitionRule;
import com.simiacryptus.probabilityModel.rules.MetricRuleGenerator;
import com.simiacryptus.probabilityModel.rules.RuleGenerator;
import com.simiacryptus.probabilityModel.rules.fitness.HybridEntropySplitFitness;
import com.simiacryptus.probabilityModel.rules.fitness.SplitFitness;
import com.simiacryptus.probabilityModel.volume.SpacialVolume;

public class ModelPartitioner extends PoolNodeVisitor<ModelPartitioner, PointNode>
{
  private static final boolean multithreaded     = false;
  private final RuleGenerator  ruleGenerator;
  
  private int                  minPointThreshold = 5;
  private boolean              rewriteRules      = true;
  
  public ModelPartitioner()
  {
    this(new HybridEntropySplitFitness());
  }
  
  public ModelPartitioner(final RuleGenerator ruleGenerator)
  {
    super(multithreaded ? Executors.newFixedThreadPool(16) : null);
    this.ruleGenerator = ruleGenerator;
  }
  
  public ModelPartitioner(final SplitFitness splitFitness)
  {
    this(new MetricRuleGenerator(splitFitness));
    
  }
  
  @Override
  protected void finalize() throws Throwable
  {
    if (null != this.pool)
    {
      this.pool.shutdown();
    }
    super.finalize();
  }
  
  public int getMinPointThreshold()
  {
    return this.minPointThreshold;
  }
  
  public boolean isRewriteRules()
  {
    return this.rewriteRules;
  }
  
  public ModelPartitioner setMinPointThreshold(final int minPointThreshold)
  {
    this.minPointThreshold = minPointThreshold;
    return this;
  }
  
  public ModelPartitioner setPartitions(final int partitions)
  {
    this.ruleGenerator.setSplitPoints(partitions);
    return this;
  }
  
  public ModelPartitioner setRewriteRules(final boolean rewriteRules)
  {
    this.rewriteRules = rewriteRules;
    return this;
  }
  
  @Override
  public void visitBegin(final PointNode node)
  {
    if (!this.rewriteRules && null != node.getRule())
    {
      return;
    }
    if (this.minPointThreshold >= node.getWeight())
    {
      return;
    }
    final PartitionRule rule = this.ruleGenerator.getRule(node);
    if (null == rule)
    {
      return;
    }
    for (final SpacialVolume child : rule.getSubVolumes())
    {
      if(checkVolume(node, child))
      {
        return;
      }
    }
    node.setRule(rule);
  }

  private boolean checkVolume(final PointNode node, final SpacialVolume child)
  {
    @SuppressWarnings("unused")
    final SpacialVolume originalChild = child; // Debug purposes
    final SpacialVolume nodeRegion = node.getRegion();
    SpacialVolume intersect = child.intersect(nodeRegion);
    final boolean checkVoume;
    if(null == intersect)
    {
      checkVoume = true;
    }
    // TODO: Why is this here??
    else if (nodeRegion.dimensions() > intersect.dimensions())
    {
      checkVoume = true;
    }
    else if (nodeRegion.getVolume().dimension > intersect.getVolume().dimension)
    {
      checkVoume = true;
    }
    else
    {
      checkVoume = false;
    }
    return checkVoume;
  }

  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append(getClass().getSimpleName());
    builder.append("[");
    builder.append(ruleGenerator);
    builder.append("]");
    return builder.toString();
  }
  
  
}