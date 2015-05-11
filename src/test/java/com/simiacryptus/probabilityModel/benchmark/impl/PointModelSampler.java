package com.simiacryptus.probabilityModel.benchmark.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.simiacryptus.data.DoubleRange;
import com.simiacryptus.lang.LOG;
import com.simiacryptus.probabilityModel.ModelSampler;
import com.simiacryptus.probabilityModel.benchmark.base.DataDistribution;
import com.simiacryptus.probabilityModel.benchmark.base.DataSampler;
import com.simiacryptus.probabilityModel.model.DistributionModel.ProjectionNodeFunction;
import com.simiacryptus.probabilityModel.model.PointModel;
import com.simiacryptus.probabilityModel.model.ScalarNode;
import com.simiacryptus.probabilityModel.rules.metrics.RoundRobinRuleGenerator;
import com.simiacryptus.probabilityModel.visitors.ModelPartitioner;
import com.simiacryptus.probabilityModel.volume.DoubleVolume;

public class PointModelSampler implements DataSampler
{
  private final PointModel       responseModel;
  private final DataDistribution sampler;
  private final DoubleVolume     volume;
  final Random                   random = new Random();
  
  public PointModelSampler(DataDistribution sampler)
  {
    this.sampler = sampler;
    this.volume = sampler.getVolume();
    final DoubleVolume responseVolume = new DoubleVolume(volume);
    responseVolume.add(new DoubleRange(0, Double.MAX_VALUE));
    this.responseModel = new PointModel(responseVolume);
  }
  
  @Override
  public DoubleVolume getVolume()
  {
    return volume;
  }
  
  public void learn(int epochSize, int epochCount)
  {
    final int densityDimension = volume.dimensions();
    for (int epoch = 0; epoch < epochCount; epoch++)
    {
      LOG.d("Epoch #%s", epoch);
      ModelSampler densityModel = getExplorationModel();
      LOG.d("Density model: %s nodes", densityModel.model.getNodeCount());
      for (double[] point : densityModel.sample(epochSize, random))
      {
        double[] parameterPoint = Arrays.copyOf(point, densityDimension);
        double[] modelPoint = Arrays.copyOf(point, densityDimension + 1);
        modelPoint[densityDimension] = sampler.getDensity(parameterPoint);
        responseModel.addDataPoint(modelPoint);
      }
    }
  }

  private ModelSampler getExplorationModel()
  {
    new ModelPartitioner(new RoundRobinRuleGenerator())
      .setMinPointThreshold(5)
      .setRewriteRules(false)
      .visit(this.responseModel, Integer.MAX_VALUE);
    final int dimension = volume.dimensions();
    return new ModelSampler(this.responseModel.project(new ProjectionNodeFunction() {
      
      @Override
      public double evaluate(ScalarNode... nodes)
      {
        double count=0;
        double sum=0;
        double sumSq=0;
        if(nodes.length == 1) return 1;
        for(ScalarNode n : nodes)
        {
          DoubleRange r = n.getRegion().getBounds().getRange(dimension);
          for(double i=r.from;i<=r.to;i+=r.size()/10)
          {
            final double weight = n.getWeight();
            if(Double.isInfinite(weight)) continue;
            count += weight;
            sum += weight * i;
            sumSq += weight * i * i;
          }
        }
        final double mean = sum / count;
        final double stdDev = Math.sqrt((sumSq / count) - mean*mean);
        if(0 >= stdDev || stdDev > 1e6 || Double.isNaN(stdDev) || Double.isInfinite(stdDev))
        {
          return 1;
        }
        return stdDev;
      }
    }, dimension));
  }

  private ModelSampler getDensityModel()
  {
    new ModelPartitioner(new RoundRobinRuleGenerator())
      .setMinPointThreshold(5)
      .setRewriteRules(false)
      .visit(this.responseModel, Integer.MAX_VALUE);
    final int dimension = volume.dimensions();
    return new ModelSampler(this.responseModel.project(new ProjectionNodeFunction() {
      
      @Override
      public double evaluate(ScalarNode... nodes)
      {
        double count=0;
        double sum=0;
        double sumSq=0;
        if(nodes.length == 1) return 1;
        for(ScalarNode n : nodes)
        {
          DoubleRange r = n.getRegion().getBounds().getRange(dimension);
          for(double i=r.from;i<=r.to;i+=r.size()/10)
          {
            final double weight = n.getWeight();
            count += weight;
            sum += weight * i;
            sumSq += weight * i * i;
          }
        }
        final double mean = sum / count;
        final double stdDev = Math.sqrt((sumSq / count) - mean*mean);
        if(0 >= stdDev || stdDev > 1e6 || Double.isNaN(stdDev) || Double.isInfinite(stdDev))
        {
          System.out.println("");
        }
        return mean;
      }
    }, dimension));
  }
  
  @Override
  public Collection<double[]> getPoints(int count)
  {
    final List<double[]> list = new ArrayList<double[]>(count);
    for (double[] p : getDensityModel().sample(count, random))
    {
      list.add(Arrays.copyOf(p, volume.dimensions()));
    }
    return Collections.unmodifiableList(list);
  }
  
}
