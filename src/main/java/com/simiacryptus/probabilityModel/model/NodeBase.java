package com.simiacryptus.probabilityModel.model;

import com.simiacryptus.data.VolumeMetric;
import com.simiacryptus.probabilityModel.rules.PartitionRule;
import com.simiacryptus.probabilityModel.volume.SpacialVolume;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class NodeBase<T extends NodeBase<T>>
{
  
  protected DistributionModel<T> tree;
  private T                    parent;
  private SpacialVolume        region;
  private PartitionRule              rule      = null;
  private final List<T>              children  = new ArrayList<T>();
  private int                        nodeCount = 1;
  
  protected NodeBase(final DistributionModel<T> tree, final SpacialVolume range)
  {
    super();
    this.parent = null;
    this.tree = tree;
    this.setRegion(range);
  }
  
  public NodeBase(final T parent, final SpacialVolume range)
  {
    this.tree = (null==parent)?null:parent.tree;
    this.parent = parent;
    this.setRegion(range);
  }
  
  public synchronized String getPath()
  {
    final T parent = getParent();
    if(null == parent) return "";
    final int indexOf = parent.getChildren().indexOf(this);
    if(0 > indexOf) throw new RuntimeException();
    return parent.getPath() + Integer.toHexString(indexOf);
  }
  
  @SuppressWarnings("unchecked")
  protected final synchronized void addChild(final T child)
  {
    assert(this.nodeCount == countNodes());
    if(null != child) 
    {
      child.setParent((T) this);
      this.addNodeCount(child.getNodeCount());
    }
    this.children.add(child);
    assert(this.nodeCount == countNodes());
  }
  
  protected synchronized void addChildren(final List<T> newChildren)
  {
    for(T child : newChildren)
    {
      addChild(child);
    }
  }
  
  protected final synchronized void addNodeCount(final int delta)
  {
    this.nodeCount += delta;
    assert(0 < this.nodeCount);
    if (null != parent)
    {
      parent.addNodeCount(delta);
    }
  }
  
  protected final synchronized void clearChildren()
  {
    for (final T c : this.children)
    {
      if(null != c) this.addNodeCount(-c.getNodeCount());
    }
    this.children.clear();
    assert(this.nodeCount == countNodes());
  }
  
  public final List<T> getChildren()
  {
    return Collections.unmodifiableList(this.children);
  }
  
  @SuppressWarnings("unchecked")
  public final T getLeaf(final double[] point)
  {
    if (!contains(point))
    {
      return null;
    }
    if (null == this.getRule())
    {
      return (T) this;
    }
    else
    {
      final int index = this.getRule().evaluate(point);
      final T child = this.getChildren().get(index);
      if(null == child)
      {
        return null;
      }
      else
      {
        return child.getLeaf(point);
      }
    }
  }
  
  protected boolean contains(double[] point)
  {
    return this.getRegion().contains(point);
  }

  public final int getNodeCount()
  {
    assert(this.nodeCount == countNodes());
    return this.nodeCount;
  }
  
  protected int countNodes()
  {
    int c = 1;
    for(T child : getChildren())
    {
      if(null != child)
      {
        c += child.countNodes();
      }
    }
    return c;
  }

  public T getParent()
  {
    assert(null == this.parent || -1 < this.parent.getChildren().indexOf(this));

    return this.parent;
  }
  
  public SpacialVolume getRegion()
  {
    return this.region;
  }
  
  @SuppressWarnings("unchecked")
  public T getRoot()
  {
    if (null == this.getParent())
    {
      return (T) this;
    }
    return this.getParent().getRoot();
  }
  
  public PartitionRule getRule()
  {
    return this.rule;
  }

  public VolumeMetric getVolume()
  {
    return this.getRegion().getVolume();
  }
  
  public abstract double getWeight();
  
  protected void setRule(final PartitionRule rule)
  {
    this.rule = rule;
    //assert(verifyStructure());
  }

  void setParent(T obj)
  {
    this.parent = obj;
  }

  protected void setRegion(SpacialVolume region)
  {
    this.region = region;
  }

  public VolumeMetric getVolumeFraction()
  {
    VolumeMetric nodeVolume = getRegion().getVolume();
    T volumeParent = getParent();
    while(null != volumeParent)
    {
      T next = volumeParent.getParent();
      VolumeMetric v = null==next?new VolumeMetric(0, 0):next.getVolume();
      if(0 >= v.value || null == next)
      {
        v = volumeParent.getVolume();
        if(0 >= v.value)
        {
          return new VolumeMetric(0, 0);
        }
        else
        {
          return nodeVolume.divide(v);
        }
      }
      volumeParent = volumeParent.getParent();
    }
    return new VolumeMetric(0, 0);
  }

}