package com.simiacryptus.probabilityModel.benchmark.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import com.simiacryptus.probabilityModel.ModelSampler;
import com.simiacryptus.probabilityModel.benchmark.base.DataDistribution;
import com.simiacryptus.probabilityModel.benchmark.base.DataLearner;
import com.simiacryptus.probabilityModel.benchmark.base.DataSampler;
import com.simiacryptus.probabilityModel.benchmark.base.TestObject;
import com.simiacryptus.probabilityModel.model.PointModel;
import com.simiacryptus.probabilityModel.visitors.ModelPartitioner;
import com.simiacryptus.probabilityModel.volume.DoubleVolume;

public final class TreeModeler extends TestObject implements DataDistribution, DataSampler, DataLearner
{
  private final ModelPartitioner partitioner;
  private PointModel model;
  private final Random random = new Random();
  private final DoubleVolume volume;
  
  public TreeModeler(DoubleVolume volume, ModelPartitioner partitioner)
  {
    this.volume = volume;
    this.partitioner = partitioner;
    this.model = new PointModel(volume);
  }
  
  @Override
  public void train()
  {
    partitioner.visit(model, Integer.MAX_VALUE);
  }
  
  @Override
  public double getDensity(double[] p)
  {
    return model.getDensity(p);
  }
  
  @Override
  public void addPoints(Collection<double[]> points)
  {
    for(double[] point : points)
    {
      model.addDataPoint(point);
    }
  }

  @Override
  public String getName()
  {
    return this.toString();
  }

  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append(getClass().getSimpleName());
    builder.append("[");
    builder.append(this.partitioner);
    builder.append("]");
    return builder.toString();
  }

  @Override
  public Collection<double[]> getPoints(int count)
  {
    return Arrays.asList(new ModelSampler(model).sample(count, random));
  }

  @Override
  public void reset()
  {
    model = new PointModel(model.getRegion().getBounds());
  }

  @Override
  public DoubleVolume getVolume()
  {
    return volume;
  }

}