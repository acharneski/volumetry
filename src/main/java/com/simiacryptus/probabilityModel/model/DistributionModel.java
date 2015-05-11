package com.simiacryptus.probabilityModel.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.simiacryptus.data.DoubleRange;
import com.simiacryptus.data.VolumeMetric;
import com.simiacryptus.probabilityModel.rules.MetricRule;
import com.simiacryptus.probabilityModel.rules.PartitionRule;
import com.simiacryptus.probabilityModel.rules.RuleCrossProduct;
import com.simiacryptus.probabilityModel.rules.metrics.DimensionMetric;
import com.simiacryptus.probabilityModel.visitors.NodeVisitor;
import com.simiacryptus.probabilityModel.volume.DoubleVolume;
import com.simiacryptus.probabilityModel.volume.SpacialVolume;

public abstract class DistributionModel<T extends NodeBase<T>>
{
  public static final class AccumulateWeights extends NodeVisitor<AccumulateWeights, ScalarNode>
  {
    
    @Override
    protected void visitEnd(ScalarNode node)
    {
      final List<ScalarNode> children = node.getChildren();
      if (null != children && 0 < children.size())
      {
        double weight = 0;
        for (ScalarNode c : children)
        {
          if (null != c) weight += c.getWeight();
        }
        node.setWeight(weight);
      }
      super.visitEnd(node);
    }
    
  }
  
  public interface BinaryNodeFunction
  {
    double evaluate(double left, double right, VolumeMetric volume);
  }
  
  public interface ProjectionNodeFunction
  {
    double evaluate(ScalarNode... nodes);
  }
  
  static ScalarNode extractProjectionTree(final HashSet<Integer> dimensions, final NodeBase<?> subtree)
  {
    DoubleVolume region = new DoubleVolume(subtree.getRegion().getBounds());
    for (int d : dimensions)
    {
      region.set(d, DoubleRange.UNBOUNDED);
    }
    if (0 == subtree.getChildren().size())
    {
      return new ScalarNode(region);
    }
    final MetricRule rule = (MetricRule) subtree.getRule();
    final int dimension = ((DimensionMetric) rule.metric).dimension;
    ScalarNode projectedNode;
    if (dimensions.contains(dimension))
    {
      projectedNode = null;
      for (NodeBase<?> child : subtree.getChildren())
      {
        final ScalarNode childProjection = extractProjectionTree(dimensions, child);
        if (null == projectedNode)
        {
          projectedNode = childProjection;
        }
        else
        {
          projectedNode = evaluate(new BinaryNodeFunction() {
            @Override
            public double evaluate(double left, double right, VolumeMetric volume)
            {
              return 0;
            }
          }, projectedNode, childProjection, null);
        }
      }
    }
    else
    {
      projectedNode = new ScalarNode(region);
      projectedNode.setRule(rule);
      for (NodeBase<?> child : subtree.getChildren())
      {
        final ScalarNode childProjection = extractProjectionTree(dimensions, child);
        projectedNode.addChild(childProjection);
      }
    }
    return projectedNode;
  }
  
  static ScalarNode evaluate(final BinaryNodeFunction f, final NodeBase<?> left, final NodeBase<?> right, final ScalarNode parent)
  {
    final SpacialVolume region = left.getRegion().union(right.getRegion());
    final ScalarNode resultNode = new ScalarNode((ScalarNode)null, region);
    if (left.getChildren().size() == 0 && right.getChildren().size() == 0)
    {
      resultNode.setWeight(f.evaluate(left.getWeight(), right.getWeight(), region.getVolume()));
      return resultNode;
    }
    else
    {
      double newWeight = 0;
      final List<NodeBase<?>> rightNodes = new ArrayList<NodeBase<?>>();
      final List<NodeBase<?>> leftNodes = new ArrayList<NodeBase<?>>();
      PartitionRule leftRule;
      PartitionRule rightRule;
      if (left.getChildren().size() == 0)
      {
        rightNodes.addAll(right.getChildren());
        leftNodes.add(left);
        leftRule = null;
        rightRule = right.getRule();
      }
      else if (right.getChildren().size() == 0)
      {
        rightNodes.add(right);
        leftNodes.addAll(left.getChildren());
        leftRule = left.getRule();
        rightRule = null;
      }
      else
      {
        rightNodes.addAll(right.getChildren());
        leftNodes.addAll(left.getChildren());
        leftRule = left.getRule();
        rightRule = right.getRule();
      }
      resultNode.setRule(RuleCrossProduct.create(leftRule, rightRule));
      for (NodeBase<?> leftNode : leftNodes)
      {
        for (NodeBase<?> rightNode : rightNodes)
        {
          if (null == leftNode || null == rightNode)
          {
            resultNode.addChild(null);
            continue;
          }
          final SpacialVolume intersect = leftNode.getRegion().intersect(rightNode.getRegion());
          if (null == intersect)
          {
            resultNode.addChild(null);
            continue;
          }
          ScalarNode leftSubnode = new ScalarNode((ScalarNode)null, leftNode);
          leftSubnode.copyChildren(leftNode);
          leftSubnode = leftSubnode.slice(intersect.getBounds());
          ScalarNode rightSubnode = new ScalarNode((ScalarNode)null, rightNode);
          rightSubnode.copyChildren(rightNode);
          rightSubnode = rightSubnode.slice(intersect.getBounds());
          ScalarNode resultSubnode = evaluate(f, leftSubnode, rightSubnode, resultNode);
          resultNode.addChild(resultSubnode);
          newWeight += resultSubnode.getWeight();
        }
      }
      resultNode.setWeight(newWeight);
      return resultNode;
    }
  }
  
  protected volatile T root;
  
  public DistributionModel()
  {
    super();
  }
  
  protected abstract T constructRoot();
  
  public ScalarModel copy()
  {
    final T root = this.getRoot();
    final ScalarModel scalarModel = new ScalarModel(root);
    scalarModel.getRoot().copyChildren(root);
    return scalarModel;
  }
  
  public ScalarModel evaluate(final BinaryNodeFunction f, final DistributionModel<?> right)
  {
    final ScalarModel scalarModel = new ScalarModel(null);
    scalarModel.setRoot(evaluate(f, this.getRoot(), right.getRoot(), null));
    return scalarModel;
  }
  
  public int getNodeCount()
  {
    return this.getRoot().getNodeCount();
  }
  
  public SpacialVolume getRegion()
  {
    return this.getRoot().getRegion();
  }
  
  public final T getRoot()
  {
    if (null == this.root)
    {
      synchronized (this)
      {
        if (null == this.root)
        {
          this.root = this.constructRoot();
        }
      }
    }
    return this.root;
  }
  
  public double getWeight()
  {
    return this.getRoot().getWeight();
  }
  
  public ScalarModel slice(final DoubleVolume range)
  {
    final ScalarModel scalarModel = copy();
    final ScalarNode root = scalarModel.getRoot();
    ScalarNode newRoot = root.slice(range);
    scalarModel.setRoot(newRoot);
    return scalarModel;
  }
  
  public ScalarModel project(final ProjectionNodeFunction fn, final int... dimensions)
  {
    final HashSet<Integer> set = new HashSet<Integer>();
    for (int d : dimensions)
      set.add(d);
    final ScalarNode projectionTree = extractProjectionTree(set, getRoot());
    for (ScalarNode leaf : NodeUtil.getLeaves(projectionTree))
    {
      final ScalarNode[] leaves = NodeUtil.getLeaves(slice(leaf.getRegion().getBounds()).getRoot()).toArray(new ScalarNode[] {});
      leaf.setWeight(fn.evaluate(leaves));
    }
    new AccumulateWeights().visit(projectionTree, Integer.MAX_VALUE);
    final ScalarModel projectedModel = new ScalarModel(null);
    projectedModel.setRoot(projectionTree);
    return projectedModel;
  }

  public double getDensity(double[] p)
  {
    final T leaf = getRoot().getLeaf(p);
    final VolumeMetric volume = leaf.getVolumeFraction();
    if(0 == volume.value) return 0;
//    if(volume.dimension < getRegion().dimensions()) return 0;
    return getWeightFraction(leaf) / volume.value;
  }

  private double getWeightFraction(final T leaf)
  {
    return leaf.getWeight() / getRoot().getWeight();
  }
  
}