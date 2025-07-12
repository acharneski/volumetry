package com.simiacryptus.probabilityModel;

import com.simiacryptus.data.DoubleRange;
import com.simiacryptus.data.VolumeMetric;
import com.simiacryptus.lang.LOG;
import com.simiacryptus.probabilityModel.distributions.SampledDistribution;
import com.simiacryptus.probabilityModel.distributions.Test3dDistributions;
import com.simiacryptus.probabilityModel.model.DistributionModel;
import com.simiacryptus.probabilityModel.model.DistributionModel.BinaryNodeFunction;
import com.simiacryptus.probabilityModel.model.DistributionModel.ProjectionNodeFunction;
import com.simiacryptus.probabilityModel.model.PointModel;
import com.simiacryptus.probabilityModel.model.ScalarModel;
import com.simiacryptus.probabilityModel.model.ScalarNode;
import com.simiacryptus.probabilityModel.rules.*;
import com.simiacryptus.probabilityModel.rules.fitness.DataEntropySplitFitness;
import com.simiacryptus.probabilityModel.rules.fitness.HybridEntropySplitFitness;
import com.simiacryptus.probabilityModel.rules.fitness.SplitFitness;
import com.simiacryptus.probabilityModel.rules.fitness.VolumeEntropySplitFitness;
import com.simiacryptus.probabilityModel.rules.metrics.Metric;
import com.simiacryptus.probabilityModel.rules.metrics.MetricSurface;
import com.simiacryptus.probabilityModel.visitors.ModelPartitioner;
import com.simiacryptus.probabilityModel.volume.DoubleVolume;
import com.simiacryptus.util.TestUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class ModelOperationsDevTest
{

  final List<Semaphore> finishSemaphores = new ArrayList<Semaphore>();
  
  @BeforeClass
  public static void setupModelTest2()
  {
    // Setup for HTML-based plotting
    System.out.println("Using HTML/JavaScript-based 3D plotting");
  }
  
  @After//Class
  public void teardownModelTest2()
  {
    try
    {
      ArrayList<Semaphore> clone;
      synchronized (finishSemaphores) {
        clone = new ArrayList<>(finishSemaphores);
        finishSemaphores.clear();
      }
      for (final Semaphore s : clone)
      {
        s.acquire();
      }
    }
    catch (final InterruptedException e)
    {
    }
  }
  
  private final DoubleVolume range        = new DoubleVolume(new DoubleRange(0, 1), new DoubleRange(0, 1), new DoubleRange(0, 1));
  int                        plotSize     = 100000;
  int                        trainingSize = 5000;
  
  @Test
  public void testDataEntropyModel() throws Exception
  {
    TestUtil.random.setSeed(0xa1b9027d0000dbb3l);
    final Test3dDistributions referenceDistribution = Test3dDistributions.MV3Snake_1;
    final Object[] args = { Thread.currentThread().getStackTrace()[1].getMethodName() };
    LOG.d("Started %s\n", args);
    
    final PointModel model = new PointModel(this.range);
    TestUtil.fillModel(model, referenceDistribution, this.trainingSize);
    new ModelPartitioner(new DataEntropySplitFitness()).visit(model, Integer.MAX_VALUE);
    // model.visit(new TrimTree());
    LOG.d("Initialized model; %s nodes", model.getNodeCount());
    
    final ModelSampler modelDistribution = new ModelSampler(model);
    // finishSemaphores.add(TestUtil.show(TestUtil.getScatterChart(model, plotSize)));
    finishSemaphores.add(TestUtil.show(TestUtil.getScatterChart(this.plotSize, this.range, referenceDistribution, modelDistribution)));
    
    TestUtil.compareDensityMatrix(this.range, 10000, modelDistribution, referenceDistribution, Test3dDistributions.MV2Normal_M34);
  }
  
  @Test
  public void testHybridEntropyModel() throws Exception
  {
    TestUtil.random.setSeed(0xa1b9027d0000dbb3l);
    final Test3dDistributions referenceDistribution = Test3dDistributions.MV3Snake_1;
    final Object[] args = { Thread.currentThread().getStackTrace()[1].getMethodName() };
    
    LOG.d("Started %s\n", args);
    
    final PointModel model = new PointModel(this.range);
    TestUtil.fillModel(model, referenceDistribution, this.trainingSize);
    new ModelPartitioner(new HybridEntropySplitFitness()).visit(model, Integer.MAX_VALUE);
    final Object[] args1 = { model.getNodeCount() };
    // model.visitUp(new TrimTree());
    LOG.d("Initialized model; %s nodes", args1);
    
    // TestUtil.openJson(model.visitUp(new JsonConverter().setPointSample(10), 0, 3).jsonCache.get(model));
    
    final ModelSampler modelDistribution = new ModelSampler(model);
    // finishSemaphores.add(TestUtil.show(TestUtil.getScatterChart(model, plotSize)));
    finishSemaphores.add(TestUtil.show(TestUtil.getScatterChart(this.plotSize, this.range, referenceDistribution, modelDistribution)));
    
    // TestUtil.openJson(((JsonConverter<?>)model.visitUp(new JsonConverter().setPointSample(10), 0, 3)).jsonCache.get(model));
    TestUtil.compareDensityMatrix(this.range, 10000, modelDistribution, referenceDistribution, Test3dDistributions.MV2Normal_M34);
    
  }

  @Test
  @Ignore
  public void testKDTreeExperiment() throws Exception
  {
    MetricRuleGenerator.DEBUG = 0;
    // TestUtil.random.setSeed(0xa1b9027d0000dbb3l);
    TestUtil.random.setSeed(0x7156e9e600024290l);
    final Test3dDistributions referenceDistribution = Test3dDistributions.MV3Logistic;
    final Object[] args = { Thread.currentThread().getStackTrace()[1].getMethodName() };
    LOG.d("Started %s\n", args);
    
    final SplitFitness splitFitness = new VolumeEntropySplitFitness();
    final List<Distribution> toPlot = new ArrayList<Distribution>();
    toPlot.add(referenceDistribution);
    
    {
      final PointModel pointModel = new PointModel(this.range);
      TestUtil.fillModel(pointModel, referenceDistribution, this.trainingSize);
      
      final double[][] dataPoints = pointModel.getDataPoints().toArray(new double[][] {});
      final KDTreeRuleGenerator ruleGenerator = new KDTreeRuleGenerator(splitFitness, dataPoints);
      
      final PartitionRule rule = ruleGenerator.getRuleCandidate(pointModel.getRoot(), ruleGenerator.getKDTreeMetric()).rule;
      final MetricRuleBoundary ruleBoundary = new MetricRuleBoundary((MetricRule) rule, 0);
      ruleBoundary.add(dataPoints);
      
//      final ArrayList<double[]> l = new ArrayList<double[]>();
//      final Iterator<double[]> iterator = new UniformPointSource(pointModel.getRegion().getBounds()).iterator();
//      for(int i=0;i<dataPoints.length;i++) l.add(iterator.next());
//      ruleBoundary.add(l.toArray(new double[][]{}));
//      final double[][] boundaryPoints = ruleBoundary.findBorderPoints(0.01, 1000);
//      toPlot.add(new SampledDistribution(boundaryPoints));
      
      new ModelPartitioner(ruleGenerator).visit(pointModel, Integer.MAX_VALUE);
      final Object[] args1 = { pointModel.getNodeCount() };
      LOG.d("Initialized model; %s nodes", args1);
      toPlot.add(new ModelSampler(pointModel));
    }
    finishSemaphores.add(TestUtil.show("Encog", TestUtil.getScatterChart(this.plotSize, this.range, toPlot.toArray(new Distribution[]{}))));
    
  }

  @Test
  public void testModelSum() throws Exception
  {
    LOG.d("Started %s\n", Thread.currentThread().getStackTrace()[1].getMethodName());
    MetricRuleGenerator.DEBUG = 0;
    trainingSize = 5000;
    //TestUtil.random.setSeed(0x7156e9e600024290l);
    
    final PointModel model1 = new PointModel(this.range);
    TestUtil.fillModel(model1, Test3dDistributions.MV2Normal_1, this.trainingSize);
    new ModelPartitioner(new VolumeEntropySplitFitness()).visit(model1, Integer.MAX_VALUE);
    LOG.d("Initialized model; %s nodes", model1.getNodeCount());
    
    final PointModel model2 = new PointModel(this.range);
    TestUtil.fillModel(model2, Test3dDistributions.MV2Normal_2, this.trainingSize);
    new ModelPartitioner(new VolumeEntropySplitFitness()).visit(model2, Integer.MAX_VALUE);
    LOG.d("Initialized model; %s nodes", model2.getNodeCount());

    final List<Distribution> toPlot = new ArrayList<Distribution>();
    toPlot.add(new ModelSampler(model1));
    toPlot.add(new ModelSampler(model2));
    toPlot.add(new ModelSampler(model1.evaluate(new BinaryNodeFunction() {

      @Override
      public double evaluate(double left, double right, VolumeMetric volume)
      {
        return left + right;
      }
    }, model2)));

    final TestUtil.ScatterPlot chart = TestUtil.getScatterChart(this.plotSize, this.range, toPlot.toArray(new Distribution[]{}));
    finishSemaphores.add(TestUtil.show("Test Model Sum", chart));

  }

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

    final TestUtil.ScatterPlot chart = TestUtil.getScatterChart(this.plotSize, this.range, toPlot.toArray(new Distribution[]{}));
    finishSemaphores.add(TestUtil.show("Test Model Sum", chart));

  }

  @Test
  public void testSurfaceFiller() throws Exception
  {
    MetricRuleGenerator.DEBUG = 0;
    // TestUtil.random.setSeed(0xa1b9027d0000dbb3l);
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
    final TestUtil.ScatterPlot chart = TestUtil.getScatterChart(this.plotSize, this.range, toPlot.toArray(new Distribution[]{}));
    finishSemaphores.add(TestUtil.show("Test Surface", chart));
    
  }
  
  @Test
  @Ignore
  public void testKDTreeModel() throws Exception
  {
    TestUtil.random.setSeed(0xa1b9027d0000dbb3l);
    final Test3dDistributions referenceDistribution = Test3dDistributions.MV3Snake_1;
    final Object[] args = { Thread.currentThread().getStackTrace()[1].getMethodName() };
    LOG.d("Started %s\n", args);
    
    final SplitFitness splitFitness = new VolumeEntropySplitFitness();
    final List<Distribution> toPlot = new ArrayList<Distribution>();
    toPlot.add(referenceDistribution);
    final PointModel cannonicalModel;
    {
      cannonicalModel = new PointModel(this.range);
      TestUtil.fillModel(cannonicalModel, referenceDistribution, this.trainingSize);
      final MetricRuleGenerator ruleGenerator = new MetricRuleGenerator(splitFitness);
      new ModelPartitioner(ruleGenerator).visit(cannonicalModel, Integer.MAX_VALUE);
      LOG.d("Initialized model; %s nodes", cannonicalModel.getNodeCount());
      toPlot.add(new ModelSampler(cannonicalModel));
      // finishSemaphores.add(TestUtil.show("Linear", scatterChart));
    }
    
    {
      final PointModel model = new PointModel(this.range);
      TestUtil.fillModel(model, cannonicalModel);
      final double[][] dataPoints = model.getDataPoints().toArray(new double[][] {});
      final MetricRuleGenerator ruleGenerator = new KDTreeRuleGenerator(splitFitness, dataPoints);
      new ModelPartitioner(ruleGenerator).visit(model, Integer.MAX_VALUE);
      final Object[] args1 = { model.getNodeCount() };
      LOG.d("Initialized model; %s nodes", args1);
      toPlot.add(new ModelSampler(model));
    }
    final TestUtil.ScatterPlot chart = TestUtil.getScatterChart(this.plotSize, this.range, toPlot.toArray(new Distribution[]{}));
    finishSemaphores.add(TestUtil.show("Encog", chart));
    
  }
  
  @Test
  public void testReferenceComparisons() throws Exception
  {
    final Object[] args = { Thread.currentThread().getStackTrace()[1].getMethodName() };
    LOG.d("Started %s\n", args);
    TestUtil.compareDensityMatrix(this.range, 10000, Test3dDistributions.values());
    finishSemaphores.add(TestUtil.show(TestUtil.getScatterChart(this.plotSize, this.range, Test3dDistributions.values())));
  }
  
  @Test
  public void testDistributionTemp() throws Exception
  {
    final Object[] args = { Thread.currentThread().getStackTrace()[1].getMethodName() };
    LOG.d("Started %s\n", args);
    finishSemaphores.add(TestUtil.show(TestUtil.getScatterChart(this.plotSize, this.range, Test3dDistributions.MV3Logistic)));
  }
  
  @Test
  public void testSlicedModel() throws Exception
  {
    TestUtil.random.setSeed(0xa1b9027d0000dbb3l);
    final Test3dDistributions referenceDist = Test3dDistributions.MV3Snake_1;
    final Object[] args = { Thread.currentThread().getStackTrace()[1].getMethodName() };
    LOG.d("Started %s\n", args);
    final ArrayList<DistributionModel<?>> models = new ArrayList<DistributionModel<?>>();
    final ArrayList<Distribution> modelDistributions = new ArrayList<Distribution>();
    
    final PointModel model0 = new PointModel(this.range);
    TestUtil.fillModel(model0, referenceDist, this.trainingSize);
    new ModelPartitioner(new VolumeEntropySplitFitness()).visit(model0, Integer.MAX_VALUE);
    // model.visitUp(new TrimTree());
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
  
  @Test
  public void testVolumeEntropyModel() throws Exception
  {
    TestUtil.random.setSeed(0xa1b9027d0000dbb3l);
    final Test3dDistributions referenceDistribution = Test3dDistributions.MV3Logistic;
    LOG.d("Started %s\n", Thread.currentThread().getStackTrace()[1].getMethodName());
    
    this.trainingSize = 15000;
    this.plotSize = 15000;

    final PointModel model = new PointModel(this.range);
    TestUtil.fillModel(model, referenceDistribution, this.trainingSize);
    new ModelPartitioner(new VolumeEntropySplitFitness()).visit(model, Integer.MAX_VALUE);
    // model.visitUp(new TrimTree());
    LOG.d("Initialized model; %s nodes", model.getNodeCount());
    
    // TestUtil.openJson(model.visitUp(new JsonConverter().setPointSample(10),0, 3).jsonCache.get(model));
    
    final ModelSampler modelDistribution = new ModelSampler(model);
    // finishSemaphores.add(TestUtil.show(TestUtil.getScatterChart(model,
    // plotSize)));
    finishSemaphores.add(TestUtil.show(TestUtil.getScatterChart(this.plotSize, this.range, referenceDistribution, modelDistribution)));
    
    // TestUtil.openJson(((JsonConverter<?>)model.visitUp(new
    // JsonConverter().setPointSample(10), 0, 3)).jsonCache.get(model));
    TestUtil.compareDensityMatrix(this.range, this.plotSize, modelDistribution, referenceDistribution, Test3dDistributions.MV2Normal_M34);
  }
  
}