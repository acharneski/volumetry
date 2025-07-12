package com.simiacryptus.probabilityModel.rules;

import com.simiacryptus.probabilityModel.model.PointNode;

public interface RuleGenerator
{

  int getSplitPoints();

  PartitionRule getRule(PointNode node);
  
}