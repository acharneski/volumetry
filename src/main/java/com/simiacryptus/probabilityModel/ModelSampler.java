package com.simiacryptus.probabilityModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.simiacryptus.data.RealFunction;
import com.simiacryptus.data.VolumeMetric;
import com.simiacryptus.probabilityModel.model.DistributionModel;
import com.simiacryptus.probabilityModel.model.NodeBase;

public final class ModelSampler implements Distribution
{
  
  public final DistributionModel<?>                  model;
  private final Map<NodeBase<?>, Iterator<double[]>> volumeIterators = new HashMap<NodeBase<?>, Iterator<double[]>>();
  
  public ModelSampler(final DistributionModel<?> model)
  {
    this.model = model;
  }
  
  public RealFunction getDensity()
  {
    return new RealFunction() {
      
      @Override
      public double[] evaluate(final double[] value)
      {
        final double[] zero = new double[] { 0 };
        final NodeBase<?> root = ModelSampler.this.model.getRoot();
        final NodeBase<?> leaf = root.getLeaf(value);
        if (null == leaf)
        {
          return zero;
        }
        final double probability = leaf.getWeight() / root.getWeight();
        final VolumeMetric volumeRatio = leaf.getVolume().divide(root.getVolume());
        if (volumeRatio.dimension != 0)
        {
          return zero;
        }
        return new double[] { probability / volumeRatio.value };
      }
      
      @Override
      public int inputDimension()
      {
        return ModelSampler.this.model.getRegion().dimensions();
      }
      
      @Override
      public int outputDimension()
      {
        return 1;
      }
    };
  }
  
  public int getDimension()
  {
    return this.model.getRegion().dimensions();
  }
  
  public double[] sample(final Random random)
  {
    final NodeBase<?> root = ModelSampler.this.model.getRoot();
    double[] point = null;
    while (null == point)
    {
      final NodeBase<?> leaf = getLeaf(random, root);
      point = getPointIterator(leaf).next();
    }
    return point;
  }

  private Iterator<double[]> getPointIterator(final NodeBase<?> leaf)
  {
    Iterator<double[]> iterator = this.volumeIterators.get(leaf);
    if (null == iterator)
    {
      iterator = leaf.getRegion().points().iterator();
      this.volumeIterators.put(leaf, iterator);
    }
    return iterator;
  }
  
  public double[][] sample(int count, Random random)
  {
    final NodeBase<?> root = ModelSampler.this.model.getRoot();
    @SuppressWarnings("unchecked")
    final Map<NodeBase<?>, Integer> map = (Map<NodeBase<?>, Integer>) getLeaf(count, root);
    List<double[]> points = new ArrayList<double[]>();
    for(Entry<NodeBase<?>, Integer> e : map.entrySet())
    {
      final Iterator<double[]> pointIterator = getPointIterator(e.getKey());
      for(int i=0;i<e.getValue();i++)
      {
        points.add(pointIterator.next());
      }
    }
    while(points.size() < count)
    {
      points.add(sample(random));
    }
    return points.toArray(new double[][]{});
  }

  @Override
  public String toString()
  {
    return "ModelDistributionFactory [" + Integer.toHexString(System.identityHashCode(this.model)) + "]";
  }

  @SuppressWarnings("unchecked")
  private static <T extends NodeBase<T>> Map<T,Integer> getLeaf(int count, final NodeBase<T> node)
  {
    final HashMap<T, Integer> map = new HashMap<T, Integer>();
    if (node.getChildren().size() == 0)
    {
      map.put((T)node, count);
    }
    else
    {
      final double modelSize = node.getWeight();
      double density = count / modelSize;
      final Map<T, Double> childrenWeights = childWeightMap(node);
      for (final Entry<T, Double> e : childrenWeights.entrySet())
      {
        map.putAll(getLeaf((int) Math.floor(density * e.getValue()), e.getKey()));
      }
    }
    return map;
  }

  @SuppressWarnings("unchecked")
  private static <T extends NodeBase<T>> T getLeaf(final Random random, final NodeBase<T> node)
  {
    if (node.getChildren().size() == 0)
    {
      return (T) node;
    }
    final double modelSize = node.getWeight();
    double fate = random.nextDouble() * modelSize;
    final Map<T, Double> childrenWeights = childWeightMap(node);
    for (final Entry<T, Double> e : childrenWeights.entrySet())
    {
      fate -= e.getValue();
      if (fate < 0)
      {
        final T leaf = getLeaf(random, e.getKey());
        assert null != leaf;
        return leaf;
      }
    }
    assert false;
    return null;
  }

  private static <T extends NodeBase<T>> ImmutableMap<T, Double> childWeightMap(final NodeBase<T> distributionModelNode)
  {
    final ImmutableMap<T, Double> childrenWeights = Maps.toMap(Collections2.filter(distributionModelNode.getChildren(), new Predicate<T>() {
      @Override
      public boolean apply(@Nullable T input)
      {
        return null != input;
      }
    }), new Function<NodeBase<T>, Double>() {
      @Override
      @Nullable
      public Double apply(@Nullable final NodeBase<T> input)
      {
        return (null == input) ? 0 : input.getWeight();
      }
    });
    return childrenWeights;
  }
  
}