package com.simiacryptus.probabilityModel.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import com.simiacryptus.data.VolumeMetric;
import com.simiacryptus.probabilityModel.visitors.JsonConverter;
import com.simiacryptus.probabilityModel.volume.DoubleVolume;
import com.simiacryptus.probabilityModel.volume.SpacialVolume;

public class ScalarNode extends NodeBase<ScalarNode>
{
  private double weight;
  
  ScalarNode(final DistributionModel<ScalarNode> tree, final NodeBase<?> node)
  {
    super(tree, node.getRegion());
    this.setWeight(node.getWeight());
    this.setRule(node.getRule());
  }
  
  ScalarNode(final SpacialVolume region)
  {
    super((ScalarNode) null, region);
    this.setWeight(0);
    this.setRule(null);
  }
  
  ScalarNode(final ScalarNode parent, final NodeBase<?> node)
  {
    super(parent, node.getRegion());
    this.setWeight(node.getWeight());
    this.setRule(node.getRule());
  }
  
  ScalarNode(final ScalarNode parent, final SpacialVolume region)
  {
    super(parent, region);
  }
  
  void copyChildren(final NodeBase<?> node)
  {
    this.setRule(node.getRule());
    final List<? extends NodeBase<?>> childrenList = node.getChildren();
    for (final NodeBase<?> child : childrenList)
    {
      ScalarNode newChild = null;
      if (null != child)
      {
        newChild = new ScalarNode((ScalarNode) null, child);
        newChild.copyChildren(child);
      }
      this.addChild(newChild);
    }
    //assert verifyStructure();
  }
  
  @Override
  public VolumeMetric getVolume()
  {
    return this.getRegion().getVolume();
  }
  
  @Override
  public double getWeight()
  {
    return this.weight;
  }
  
  public void setWeight(final double weight)
  {
    assert (weight < 1e6);
    assert (weight >= 0);
    this.weight = weight;
  }
  
  @Override
  public String toString()
  {
    try
    {
      return JsonConverter.toJson(this, 0).toString();
    }
    catch (final JSONException e)
    {
      return e.getMessage();
    }
  }
  
  ScalarNode slice(DoubleVolume range)
  {
    //assert verifyStructure();
    SpacialVolume previousRegion = getRegion();
    SpacialVolume newRegion = range.intersect(previousRegion);
    setRegion(newRegion);
    final ArrayList<ScalarNode> filteredChildren = new ArrayList<ScalarNode>();
    int childCount = 0;
    final List<ScalarNode> children = getChildren();
    for (ScalarNode child : children)
    {
      if (null != child && !child.getRegion().intersects(newRegion))
      {
        child = null;
      }
      if (null != child)
      {
        child = child.slice(range);
      }
      if (null != child && previousRegion.dimensions() > child.getRegion().dimensions())
      {
        child = null;
      }
      if (null != child)
      {
        childCount++;
        filteredChildren.add(child);
      }
      else
      {
        filteredChildren.add(null);
      }
    }
    if (1 == childCount)
    {
      for (ScalarNode child : filteredChildren)
      {
        if (null != child)
        {
          //assert child.verifyStructure();
          return child;
        }
      }
      throw new RuntimeException();
    }
    if (0 < childCount)
    {
      clearChildren();
      double newWeight = 0;
      for (ScalarNode child : filteredChildren)
      {
        addChild(child);
        if (null != child)
        {
          newWeight += child.getWeight();
        }
      }
      setWeight(newWeight);
    }
    else
    {
      VolumeMetric prevVolume = previousRegion.getVolume();
      VolumeMetric newVolume = newRegion.intersect(previousRegion).getVolume();
      setWeight(getWeight() * newVolume.divide(prevVolume).value);
    }
    //assert verifyStructure();
    return this;
  }
  
}