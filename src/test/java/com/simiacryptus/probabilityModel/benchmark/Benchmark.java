package com.simiacryptus.probabilityModel.benchmark;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.jzy3d.plot3d.primitives.Scatter;

import com.simiacryptus.probabilityModel.Distribution;
import com.simiacryptus.probabilityModel.benchmark.base.DataDistribution;
import com.simiacryptus.probabilityModel.benchmark.base.DataLearner;
import com.simiacryptus.probabilityModel.benchmark.base.DataSampler;
import com.simiacryptus.probabilityModel.benchmark.impl.DataSamplerDistributionWrapper;
import com.simiacryptus.probabilityModel.benchmark.statistics.DensityComparison;
import com.simiacryptus.probabilityModel.benchmark.statistics.SampleDistanceComparison;
import com.simiacryptus.probabilityModel.benchmark.statistics.SamplerDistributionEncodingEntropy;
import com.simiacryptus.probabilityModel.benchmark.util.Timer;
import com.simiacryptus.probabilityModel.volume.SpacialVolume;
import com.simiacryptus.util.HtmlOutput;
import com.simiacryptus.util.TestUtil;

public class Benchmark
{

  protected final static List<Semaphore> finishSemaphores = new ArrayList<Semaphore>();

  @AfterClass
  public static void teardownClass() throws IOException
  {
    try
    {
      for (final Semaphore s : finishSemaphores)
      {
        s.acquire();
      }
    } catch (final InterruptedException e)
    {
    }
  }

  protected boolean doPlots = false;
  protected int trainingPoints = 10000;
  protected int statsSize = 10000;

  public Benchmark()
  {
    super();
  }

  @After
  public void afterTest()
  {
  }

  protected final JSONObject train(HtmlOutput doc, DataLearner learner, DataSampler data) throws JSONException
  {
    if (null == learner) throw new IllegalArgumentException();
    if (null == data) throw new IllegalArgumentException();

    final JSONObject json = new JSONObject();
    final Timer timer = new Timer();
  
    
    learner.reset();
    Collection<double[]> trainPoints = data.getPoints(trainingPoints);
    learner.addPoints(trainPoints);
    learner.train();
  
    json.put("train/duration", timer.getSecondsAndReset());
    
    {
      JSONObject j = new JSONObject();
      j.put("value", new SampleDistanceComparison(data, data, statsSize).jsonValue());
      j.put("duration", timer.getSecondsAndReset());
      json.put("data vs data", j);
    }

    if(null != doc) {
      final JSONObject jsonPts = new JSONObject();
      jsonPts.put("training", trainPoints);
      jsonPts.put("model", learner.getPoints(trainingPoints));
      doc.addSupplementalJson("points", jsonPts);
    }
  
    if (data instanceof DataDistribution)
    {
      {
        JSONObject j = new JSONObject();
        j.put("value", new DensityComparison(((DataDistribution) data), learner).jsonValue());
        j.put("duration", timer.getSecondsAndReset());
        json.put("trained density vs data density", j);
      }
      {
        JSONObject j = new JSONObject();
        j.put("bits per point", new SamplerDistributionEncodingEntropy(learner, ((DataDistribution) data), statsSize).jsonValue());
        j.put("duration", timer.getSecondsAndReset());
        json.put("trained samples vs data density", j);
      }
    }

    {
      JSONObject j = new JSONObject();
      j.put("entropy", new SamplerDistributionEncodingEntropy(data, learner, statsSize).jsonValue());
      j.put("duration", timer.getSecondsAndReset());
      json.put("trained density vs data", j);
    }

    {
      JSONObject j = new JSONObject();
      j.put("entropy", new SamplerDistributionEncodingEntropy(learner, learner, statsSize).jsonValue());
      j.put("duration", timer.getSecondsAndReset());
      json.put("trained density vs trained samples", j);
    }
    
    {
      JSONObject j = new JSONObject();
      j.put("value", new SampleDistanceComparison(data, learner, statsSize).jsonValue());
      j.put("duration", timer.getSecondsAndReset());
      json.put("trained samples vs data", j);
    }
    
    {
      JSONObject j = new JSONObject();
      j.put("value", new SampleDistanceComparison(learner, learner, statsSize).jsonValue());
      j.put("duration", timer.getSecondsAndReset());
      json.put("trained samples vs trained samples", j);
    }
  
    if (doPlots)
    {
      final List<Distribution> toPlot = new ArrayList<Distribution>();
      toPlot.add(new DataSamplerDistributionWrapper(learner));
      toPlot.add(new DataSamplerDistributionWrapper(data));
      SpacialVolume volume = learner.getVolume().union(data.getVolume());
      String title = learner.toString() + " & " + data.toString();
      final Scatter chart = TestUtil.getScatterChart(trainingPoints, volume.getBounds(), toPlot.toArray(new Distribution[] {}));
      finishSemaphores.add(TestUtil.show(title, chart));
    }
  
    return json;
  }

}