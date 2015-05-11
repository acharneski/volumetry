package com.simiacryptus.probabilityModel.rules.metrics;

import com.simiacryptus.data.DoubleRange;
import com.simiacryptus.probabilityModel.model.PointNode;
import com.simiacryptus.probabilityModel.rules.MetricRule;
import com.simiacryptus.probabilityModel.rules.PartitionRule;
import com.simiacryptus.probabilityModel.rules.RuleGenerator;

public class RoundRobinRuleGenerator implements RuleGenerator
{
  private int splitPoints = 1;

  @Override
  public RuleGenerator setSplitPoints(int partitions)
  {
    splitPoints = partitions;
    return this;
  }

  @Override
  public int getSplitPoints()
  {
    return splitPoints;
  }

  @Override
  public PartitionRule getRule(PointNode node)
  {
    double winnerVolume = -Double.MAX_VALUE;
    int dimension = -1;
    for(int i=0;i<node.getRegion().dimensions();i++)
    {
      final double size = node.getRegion().getRange(i).size();
      if(size > winnerVolume)
      {
        winnerVolume = size;
        dimension = i;
      }
    }
    final DoubleRange range = node.getRegion().getRange(dimension);
    double[] values = new double[splitPoints];
    final double size = range.size() / (values.length+1);
    for(int i=0;i<values.length;i++)
    {
      values[i] = range.from + size * (i+1);
    }
    return new MetricRule(node.getUnboundableRegion(), new DimensionMetric(dimension, range), values);
  }
  
}
