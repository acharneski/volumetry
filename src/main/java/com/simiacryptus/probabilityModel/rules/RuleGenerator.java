package com.simiacryptus.probabilityModel.rules;

import com.simiacryptus.probabilityModel.model.PointNode;

public interface RuleGenerator
{

  RuleGenerator setSplitPoints(int partitions);

  int getSplitPoints();

  PartitionRule getRule(PointNode node);
  
}