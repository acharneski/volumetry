package com.simiacryptus.probabilityModel.rules.fitness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.simiacryptus.probabilityModel.model.PointNode;

public final class IndexFitnessFunction
{
  private final double       totalVolume;
  private final double[]     sortedMetricValues;
  private final double[]     sortedMetricVolumes;
  private final SplitFitness splitFitness;
  
  public IndexFitnessFunction(final SplitFitness splitFitness, final double totalVolume, final double[] sortedMetricValues,
      final double[] sortedMetricVolumes)
  {
    this.splitFitness = splitFitness;
    this.totalVolume = totalVolume;
    this.sortedMetricValues = sortedMetricValues;
    this.sortedMetricVolumes = sortedMetricVolumes;
    for(double x : sortedMetricVolumes)
    {
      if(x < 0) throw new IllegalArgumentException();
      if(Double.isInfinite(x)) throw new IllegalArgumentException();
    }
    for(double x : sortedMetricValues)
    {
      //if(x < 0) throw new IllegalArgumentException();
      if(Double.isInfinite(x)) throw new IllegalArgumentException();
    }
  }
  
  public Double evaluate(PointNode node, final int[] value)
  {
    return this.splitFitness.getFitness(node, this.getVolumeDataDensities(value));
  }
  
  public List<VolumeDataDensity> getVolumeDataDensities(int[] value)
  {
    value = Arrays.copyOf(value, value.length);
    Arrays.sort(value);
    final List<VolumeDataDensity> list = new ArrayList<VolumeDataDensity>();
    for (final int index : value)
    {
      final int prevIndex = 0 == list.size() ? -1 : value[list.size() - 1];
      final double volume = this.sortedMetricVolumes[index] - (0 > prevIndex ? 0. : this.sortedMetricVolumes[prevIndex]);
      final double data = index - (0 > prevIndex ? 0. : prevIndex);
      final VolumeDataDensity item = new VolumeDataDensity(volume, data);
      list.add(item);
    }
    {
      final int prevIndex = 0 == list.size() ? -1 : value[list.size() - 1];
      final double volume = this.totalVolume - (0 > prevIndex ? 0. : this.sortedMetricVolumes[prevIndex]);
      final double data = this.sortedMetricValues.length - (0 > prevIndex ? 0. : prevIndex);
      final VolumeDataDensity item = new VolumeDataDensity(volume, data);
      list.add(item);
    }
    return list;
  }
}