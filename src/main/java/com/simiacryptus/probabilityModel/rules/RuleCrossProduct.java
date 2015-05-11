package com.simiacryptus.probabilityModel.rules;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.simiacryptus.probabilityModel.volume.SpacialVolume;

public final class RuleCrossProduct implements PartitionRule
{
  private final PartitionRule leftRule;
  private final PartitionRule rightRule;

  public RuleCrossProduct(PartitionRule leftRule, PartitionRule rightRule)
  {
    this.leftRule = leftRule;
    this.rightRule = rightRule;
  }

  @Override
  public JSONObject toJson() throws JSONException
  {
    final JSONObject json = new JSONObject();
    json.put("left", leftRule.toJson());
    json.put("right", rightRule.toJson());
    return json;
  }
  
  @Override
  public SpacialVolume[] getSubVolumes()
  {
    final ArrayList<SpacialVolume> list = new ArrayList<SpacialVolume>();
    for(SpacialVolume l : leftRule.getSubVolumes())
    {
      for(SpacialVolume r : leftRule.getSubVolumes())
      {
        list.add(l.intersect(r));
      }
    }
    return list.toArray(new SpacialVolume[]{});
  }
  
  @Override
  public int getPartitions()
  {
    return leftRule.getPartitions() * rightRule.getPartitions();
  }
  
  @Override
  public int evaluate(double[] point)
  {
    return leftRule.evaluate(point) * rightRule.getPartitions() + rightRule.evaluate(point);
  }

  public static PartitionRule create(PartitionRule leftRule, PartitionRule rightRule)
  {
    if(null == leftRule) return rightRule;
    if(null == rightRule) return leftRule;
    return new RuleCrossProduct(leftRule, rightRule);
  }
}