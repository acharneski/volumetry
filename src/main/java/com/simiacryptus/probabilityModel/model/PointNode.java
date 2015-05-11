package com.simiacryptus.probabilityModel.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.json.JSONException;

import com.simiacryptus.collections.CompositeCollection;
import com.simiacryptus.collections.ProxyCollection;
import com.simiacryptus.data.VolumeMetric;
import com.simiacryptus.probabilityModel.rules.PartitionRule;
import com.simiacryptus.probabilityModel.visitors.JsonConverter;
import com.simiacryptus.probabilityModel.volume.DoubleVolume;
import com.simiacryptus.probabilityModel.volume.SpacialVolume;

public class PointNode extends NodeBase<PointNode>
{
  
  private final SpacialVolume             range;
  private int                             dataSize   = 0;
  private final ProxyCollection<double[]> dataPoints = new ProxyCollection<double[]>();
  private DoubleVolume pointRange;
  
  public PointNode(final PointNode parent, final SpacialVolume volume)
  {
    super(parent, volume);
    this.range = volume;
    this.pointRange = new DoubleVolume(volume.dimensions());
  }
  
  PointNode(final SpacialVolume range, final DistributionModel<PointNode> tree)
  {
    super(tree, range);
    this.range = range;
    this.pointRange = new DoubleVolume(range.dimensions());
  }
  
  public synchronized void addDataPoint(final double[] point)
  {
    if(pointRange.isUnbounded() || !pointRange.contains(point))
    {
      final DoubleVolume newRange = pointRange.include(point);
      assert(null != newRange);
      pointRange = newRange;
    }
    final PointNode leaf = this.getLeaf(point);
    leaf.dataPoints.add(point);
    leaf.addDataSize(1);
  }
  
  protected synchronized void addDataSize(final int i)
  {
    this.dataSize += i;
    if (null != this.getParent())
    {
      this.getParent().addDataSize(i);
    }
  }
  
  public Collection<double[]> getDataPoints()
  {
    return Collections.unmodifiableCollection(this.dataPoints);
  }
  
  @Override
  public double getWeight()
  {
    return this.dataSize;
  }
  
  private List<PointNode> prepareChildren(final PartitionRule split)
  {
    final SpacialVolume[] volumes = split.getSubVolumes();
    final List<PointNode> newChildren = new ArrayList<PointNode>();
    for (final SpacialVolume volume : volumes)
    {
      if (volume.equals(this.range))
      {
        throw new RuntimeException("Rule results in a child with identical volume");
      }
      final PointNode child = new PointNode(this, volume);
      DoubleVolume newRange = (DoubleVolume) child.range.getBounds().intersect(pointRange);
      if(null == newRange) newRange = DoubleVolume.unbounded(pointRange.dimensions());
      child.pointRange = newRange;
      newChildren.add(child);
    }
    for (final double[] point : this.getDataPoints())
    {
      final int childIndex = split.evaluate(point);
      final PointNode child = newChildren.get(childIndex);
      child.dataPoints.add(point);
    }
    int nonZeroChildren = 0;
    for (final PointNode child : newChildren)
    {
      child.dataSize = child.dataPoints.size();
      if (0 < child.dataSize)
      {
        nonZeroChildren++;
      }
    }
    if (1 == nonZeroChildren)
    {
      // System.err.println("Rule does not result in a data partitioning!");
    }
    return newChildren;
  }
  
  @Override
  public synchronized void setRule(final PartitionRule rule)
  {
    if (null != rule)
    {
      final List<PointNode> prepareChildren = this.prepareChildren(rule);
      if (null != prepareChildren)
      {
        this.setRule(rule, prepareChildren);
      }
    }
    else
    {
      this.clearChildren();
      super.setRule(null);
      this.dataPoints.inner = new ArrayList<double[]>(this.dataPoints.inner);
    }
  }
  
  private void setRule(final PartitionRule rule, final List<PointNode> children)
  {
    this.clearChildren();
    this.addChildren(children);
    super.setRule(rule);
    {
      final List<PointNode> nodeChildren = this.getChildren();
      @SuppressWarnings("unchecked")
      final Collection<double[]>[] inputs = new Collection[nodeChildren.size()];
      for (int i = 0; i < nodeChildren.size(); i++)
      {
        inputs[i] = nodeChildren.get(i).getDataPoints();
      }
      this.dataPoints.inner = new CompositeCollection<double[]>(inputs);
    }
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

  
  public SpacialVolume getRegion()
  {
    final SpacialVolume baseRegion = super.getRegion();
    if(!baseRegion.isUnbounded())
    {
      return baseRegion;
    }
    else
    {
      return baseRegion.intersect(this.pointRange);
    }
  }

  public SpacialVolume getUnboundableRegion()
  {
    return super.getRegion();
  }
  
  protected boolean contains(double[] point)
  {
    return this.getUnboundableRegion().contains(point);
  }

}