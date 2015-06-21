package com.simiacryptus.probabilityModel.benchmark;

import java.util.ArrayList;
import com.simiacryptus.data.DoubleRange;

import com.simiacryptus.probabilityModel.benchmark.base.TestObject;
import com.simiacryptus.probabilityModel.benchmark.impl.LibDataDistributionSampler;
import com.simiacryptus.probabilityModel.benchmark.impl.LibDataSampler;
import com.simiacryptus.probabilityModel.benchmark.impl.TreeModeler;
import com.simiacryptus.probabilityModel.distributions.Test3dDistributions;
import com.simiacryptus.probabilityModel.rules.fitness.VolumeEntropySplitFitness;
import com.simiacryptus.probabilityModel.rules.pca.PCARuleGenerator;
import com.simiacryptus.probabilityModel.visitors.ModelPartitioner;
import com.simiacryptus.probabilityModel.volume.DoubleVolume;

public class BenchmarkSummaryReport extends CrossProductBenchmark
{
  
  public BenchmarkSummaryReport()
  {
    super();
  }
  
  @Override
  protected ArrayList<TestObject> setupTestObjects()
  {
    final ArrayList<TestObject> objects = new ArrayList<TestObject>();
    final DoubleVolume volume = new DoubleVolume(new DoubleRange(0, 1), new DoubleRange(0, 1), new DoubleRange(0, 1));
    objects.add(new LibDataDistributionSampler(volume, Test3dDistributions.MV2Normal_1));
    objects.add(new LibDataDistributionSampler(volume, Test3dDistributions.MV2Normal_2));
    objects.add(new LibDataSampler(volume, Test3dDistributions.MV3Snake_1));
    objects.add(new LibDataSampler(volume, Test3dDistributions.MV3Logistic));
    objects.add(new TreeModeler(volume, new ModelPartitioner(new VolumeEntropySplitFitness())));
    objects.add(new TreeModeler(volume, new ModelPartitioner(new PCARuleGenerator(new VolumeEntropySplitFitness()))));
    //objects.add(new KDTreeModeler(volume));
    return objects;
  }
  
}