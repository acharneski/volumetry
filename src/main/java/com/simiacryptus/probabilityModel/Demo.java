package com.simiacryptus.probabilityModel;


import com.simiacryptus.data.DoubleRange;
import com.simiacryptus.lang.LOG;
import com.simiacryptus.probabilityModel.distributions.SampledDistribution;
import com.simiacryptus.probabilityModel.distributions.Test3dDistributions;
import com.simiacryptus.probabilityModel.model.DistributionModel;
import com.simiacryptus.probabilityModel.model.DistributionModel.ProjectionNodeFunction;
import com.simiacryptus.probabilityModel.model.PointModel;
import com.simiacryptus.probabilityModel.model.ScalarModel;
import com.simiacryptus.probabilityModel.model.ScalarNode;
import com.simiacryptus.probabilityModel.rules.MetricRule;
import com.simiacryptus.probabilityModel.rules.MetricRuleGenerator;
import com.simiacryptus.probabilityModel.rules.fitness.VolumeEntropySplitFitness;
import com.simiacryptus.probabilityModel.rules.metrics.Metric;
import com.simiacryptus.probabilityModel.rules.metrics.MetricSurface;
import com.simiacryptus.probabilityModel.visitors.ModelPartitioner;
import com.simiacryptus.probabilityModel.volume.DoubleVolume;
import com.simiacryptus.util.TestUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains test cases intended to demonstrate main points of the project
 * @author Andrew Charneski
 *
 */
public class Demo extends TestBase
{

  /**
   * Demonstrates slice and projection operations on a test distribution (snake)
   * This test will render a 3d "snake" distribution (Green) and the same distribution projected against a YZ plane (Red).
   *
   * @throws Exception
   */
  @Test
  public void testModelProject() throws Exception
  {
    LOG.d("Started %s\n", Thread.currentThread().getStackTrace()[1].getMethodName());
    MetricRuleGenerator.DEBUG = 0;
    this.trainingSize = 5000;
    this.plotSize = 50000;
    TestUtil.random.setSeed(0x54690d4800020cd4l);
    
    final PointModel model1 = new PointModel(this.range);
    TestUtil.fillModel(model1, Test3dDistributions.MV3Snake_1, this.trainingSize);
    new ModelPartitioner(new VolumeEntropySplitFitness()).visit(model1, Integer.MAX_VALUE);
    LOG.d("Initialized model; %s nodes", model1.getNodeCount());
    
    final List<Distribution> toPlot = new ArrayList<Distribution>();
    toPlot.add(new ModelSampler(model1));
    {
      final DoubleVolume slice = new DoubleVolume(range);
      slice.set(1, new DoubleRange(0.3, 0.3));
      toPlot.add(new ModelSampler(model1.slice(slice)));
    }

    {
      final DoubleVolume slice = new DoubleVolume(range);
      slice.set(1, new DoubleRange(0.5, 0.5));
      toPlot.add(new ModelSampler(model1.project(new ProjectionNodeFunction() {
        
        @Override
        public double evaluate(ScalarNode... nodes)

        {
          double sum = 0;
          for(ScalarNode n : nodes) sum += n.getWeight();
          return sum;
        }
      }, 1).slice(slice)));
    }

    finishSemaphores.add(TestUtil.show("Test Model Sum", TestUtil.getScatterChart(this.plotSize, this.range, toPlot.toArray(new Distribution[]{}))));

  }

  /**
   * Test operation where a space-filling monte carlo technique is used to approximate an equipotential surface. 
   * @throws Exception
   */
  @Test
  public void testSurfaceFiller() throws Exception
  {
    MetricRuleGenerator.DEBUG = 0;
    TestUtil.random.setSeed(0x7156e9e600024290l);

    double value = 1;
    Metric metric = new Metric() {
      
      @Override
      public JSONObject toJson() throws JSONException
      {
        return null;
      }
      
      @Override
      public double evaluate(double[] point)
      {
        double sum = 0;
        for(double v : point) sum += v * v;
        return Math.sqrt(sum);
      }
    };
    double tolerance = 0.01;
    final MetricSurface metricSurface = new MetricSurface(new MetricRule(range, metric, value), value, tolerance);

    final List<Distribution> toPlot = new ArrayList<Distribution>();
    final double[][] borderPoints = metricSurface.findBorderPoints(10000);
    toPlot.add(new SampledDistribution(borderPoints));
    LOG.d("Generated %s points within %s%% of target", borderPoints.length, tolerance * 100);
    finishSemaphores.add(TestUtil.show("Test Surface", TestUtil.getScatterChart(this.plotSize, this.range, toPlot.toArray(new Distribution[]{}))));
    
  }
  
  /**
   * Display the "3d logistic map" testing distribution
   * @throws Exception
   */
  @Test
  public void test3dLogisticDistribution() throws Exception
  {
    LOG.d("Started %s\n", Thread.currentThread().getStackTrace()[1].getMethodName());
    finishSemaphores.add(TestUtil.show(TestUtil.getScatterChart(this.plotSize, this.range, Test3dDistributions.MV3Logistic)));
  }
    
  /**
   * Demonstrate several slices of a model, including equality (e.g. x=5) and range (e.g. y<=4) constraints
   * @throws Exception
   */
  @Test
  public void testSlicedModel() throws Exception
  {
    TestUtil.random.setSeed(0x9c71530d0002a0e1l);
    final Test3dDistributions referenceDist = Test3dDistributions.MV3Snake_1;
    LOG.d("Started %s\n", Thread.currentThread().getStackTrace()[1].getMethodName());
    final ArrayList<DistributionModel<?>> models = new ArrayList<DistributionModel<?>>();
    final ArrayList<Distribution> modelDistributions = new ArrayList<Distribution>();
    
    final PointModel model0 = new PointModel(this.range);
    TestUtil.fillModel(model0, referenceDist, this.trainingSize);
    new ModelPartitioner(new VolumeEntropySplitFitness()).visit(model0, Integer.MAX_VALUE);
    final ModelSampler modelDistribution0 = new ModelSampler(model0);
    LOG.d("Initialized model; %s nodes, density function %s", model0.getNodeCount(), modelDistribution0);
    models.add(model0);
    modelDistributions.add(modelDistribution0);
    
    final ArrayList<DoubleVolume> slices = new ArrayList<DoubleVolume>();
    slices.add(new DoubleVolume(new DoubleRange(0., 0.5), new DoubleRange(0, 1), new DoubleRange(0, 1)));
    slices.add(new DoubleVolume(new DoubleRange(0, 1), new DoubleRange(0.3, 0.3), new DoubleRange(0, 1)));
    slices.add(new DoubleVolume(new DoubleRange(0, 1), new DoubleRange(0, 1), new DoubleRange(0.4, 0.4001)));
    for (final DoubleVolume slice : slices)
    {
      final ScalarModel model = model0.slice(slice);
      models.add(model);
      final ModelSampler modelDistribution = new ModelSampler(model);
      modelDistributions.add(modelDistribution);
      final Object[] args2 = { model.getNodeCount(), modelDistribution, slice };
      LOG.d("Sliced model; %s nodes, density function %s: %s", args2);
    }
    
    finishSemaphores.add(TestUtil.show(TestUtil.getScatterChart(this.plotSize, this.range,
        modelDistributions.toArray(new ModelSampler[] {}))));
    modelDistributions.add(referenceDist);
    modelDistributions.add(Test3dDistributions.MV2Normal_M34);
    TestUtil.compareDensityMatrix(this.range, 10000, modelDistributions.toArray(new Distribution[] {}));
    for (final DistributionModel<?> model : models)
    {
      if (model instanceof PointModel)
      {
      }
    }
  }
  
  /**
   * Models the 3d logistic distribution (green) and displays points generated from that model (blue)
   * @throws Exception
   */
  @Test
  public void testVolumeEntropyModel() throws Exception
  {
    TestUtil.random.setSeed(0xa1b9027d0000dbb3l);
    final Test3dDistributions referenceDistribution = Test3dDistributions.MV3Logistic;
    LOG.d("Started %s\n", Thread.currentThread().getStackTrace()[1].getMethodName());
    this.trainingSize = 15000;
    this.plotSize = 30000;
    final PointModel model = new PointModel(this.range);
    TestUtil.fillModel(model, referenceDistribution, this.trainingSize);
    new ModelPartitioner(new VolumeEntropySplitFitness()).visit(model, Integer.MAX_VALUE);
    LOG.d("Initialized model; %s nodes", model.getNodeCount());
    final ModelSampler modelDistribution = new ModelSampler(model);
    finishSemaphores.add(TestUtil.show(TestUtil.getScatterChart(this.plotSize, this.range, referenceDistribution, modelDistribution)));
    TestUtil.compareDensityMatrix(this.range, this.plotSize, modelDistribution, referenceDistribution, Test3dDistributions.MV2Normal_M34);
  }
  
}