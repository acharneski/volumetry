package com.simiacryptus.probabilityModel.benchmark;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.simiacryptus.data.DoubleRange;
import com.simiacryptus.data.DoubleRange;
import com.simiacryptus.data.VolumeMetric;
import com.simiacryptus.lang.MathUtil;
import com.simiacryptus.probabilityModel.benchmark.base.TestObject;
import com.simiacryptus.probabilityModel.benchmark.impl.KDTreeModeler;
import com.simiacryptus.probabilityModel.benchmark.impl.LibDataDistributionSampler;
import com.simiacryptus.probabilityModel.benchmark.impl.LibDataSampler;
import com.simiacryptus.probabilityModel.benchmark.impl.TreeModeler;
import com.simiacryptus.probabilityModel.distributions.Test3dDistributions;
import com.simiacryptus.probabilityModel.model.PointNode;
import com.simiacryptus.probabilityModel.rules.fitness.SplitFitness;
import com.simiacryptus.probabilityModel.rules.fitness.VolumeDataDensity;
import com.simiacryptus.probabilityModel.rules.fitness.VolumeEntropySplitFitness;
import com.simiacryptus.probabilityModel.rules.fitness.VolumeDataDensity.VolumeDataNormalizer;
import com.simiacryptus.probabilityModel.rules.pca.PCARuleGenerator;
import com.simiacryptus.probabilityModel.visitors.ModelPartitioner;
import com.simiacryptus.probabilityModel.volume.DoubleVolume;

public class ExperimentalBenchmark extends CrossProductBenchmark
{

  public static final class ExperimentalEntropy1 implements SplitFitness
  {
    public double getFitness(PointNode node, List<VolumeDataDensity> list)
    {
      final List<VolumeDataDensity> normalizedList = this.process(list);
      try
      {
        double totalValue = 0;
              for (VolumeDataDensity item : normalizedList)
              {
        //        VolumeMetric v = node.getVolumeFraction();
        //        if (0 < v.value)
        //        {
        //          double volume = item.volume * v.value;
        //          double weight = item.data * node.getWeight() / node.getRoot().getWeight();
        //          item = new VolumeDataDensity(volume, weight);
        //        }
                totalValue += this.getCrossCost(item);
              }
              return -totalValue;
      } catch (Throwable e)
      {
        return -Double.MAX_VALUE;
      }
    }

       

    protected double getCrossCost(VolumeDataDensity item)
    {
      if(0 == item.data) return Double.POSITIVE_INFINITY;
      if(0 == item.volume) return Double.POSITIVE_INFINITY;
//      final double fitness = (item.volume * MathUtil.log2(item.data / item.volume));
      final double fitness = (item.volume * MathUtil.log2(item.data));
//      final double fitness = (item.data * MathUtil.log2(item.volume));
      assert !Double.isInfinite(fitness);
      assert !Double.isNaN(fitness);
      return fitness;
    }

    protected List<VolumeDataDensity> process(List<VolumeDataDensity> list)
    {
      list = new VolumeDataNormalizer()
      {
        @Override
        protected VolumeDataDensity processItem(VolumeDataDensity item, int count, double volumeTotal, double dataTotal)
        {
          double dataAdj = 1;
          dataAdj *= item.volume/volumeTotal;
          VolumeDataDensity newValue = new VolumeDataDensity(item.volume, item.data + dataAdj);
          return newValue;
        }

      }.process(list);
      list = new VolumeDataNormalizer().process(list);
      return list;
    }
  }

  public ExperimentalBenchmark()
  {
    super();
    doPlots = true;
    selfCross = false;
  }

  @Override
  protected ArrayList<TestObject> setupTestObjects()
  {
    final ArrayList<TestObject> objects = new ArrayList<TestObject>();
    final DoubleVolume volume = new DoubleVolume(new DoubleRange(0, 1), new DoubleRange(0, 1), new DoubleRange(0, 1));
    objects.add(new LibDataSampler(volume, Test3dDistributions.MV3Logistic));

    ExperimentalEntropy1 entropyFunction = new ExperimentalEntropy1();
    PCARuleGenerator ruleGenerator = new PCARuleGenerator(entropyFunction);
    ModelPartitioner partitioner = new ModelPartitioner(ruleGenerator).setMinPointThreshold(10);
    objects.add(new TreeModeler(volume, partitioner));

    // objects.add(new TreeModeler(volume, new ModelPartitioner(new VolumeEntropySplitFitness())));
    // objects.add(new TreeModeler(volume, new ModelPartitioner(new PCARuleGenerator(new VolumeEntropySplitFitness()))));
    return objects;
  }

}