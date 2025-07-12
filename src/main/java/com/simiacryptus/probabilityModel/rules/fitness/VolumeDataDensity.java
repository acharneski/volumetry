package com.simiacryptus.probabilityModel.rules.fitness;

import java.util.ArrayList;
import java.util.List;

public class VolumeDataDensity
{

  public static class VolumeDataNormalizer extends VolumeDataProcessor
  {
    @Override
    public List<VolumeDataDensity> process(final List<VolumeDataDensity> list)
    {
      double volumeTotal = 0;
      double dataTotal = 0;
      for (final VolumeDataDensity item : list)
      {
        volumeTotal += item.volume;
        dataTotal += item.data;
      }
      final ArrayList<VolumeDataDensity> returnValue = new ArrayList<VolumeDataDensity>(list.size());
      for (final VolumeDataDensity item : list)
      {
        returnValue.add(this.processItem(item, list.size(), volumeTotal, dataTotal));
      }
      return returnValue;
    }
    
    protected VolumeDataDensity processItem(final VolumeDataDensity item, final int count, final double volumeTotal, final double dataTotal)
    {
      return new VolumeDataDensity(item.volume / volumeTotal, item.data / dataTotal);
    }
  }
  
  public static abstract class VolumeDataProcessor
  {
    public abstract List<VolumeDataDensity> process(List<VolumeDataDensity> list);
  }

  public final double volume;
  public final double data;
  
  public VolumeDataDensity(final double volume, final double data)
  {
    super();
    if (volume < 0)
    {
      throw new IllegalArgumentException();
    }
    if (data < 0)
    {
      throw new IllegalArgumentException();
    }
    this.volume = volume;
    this.data = data;
  }
  
  @Override
  public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("VolumeDataDensity [data=");
    builder.append(this.data);
    builder.append(", volume=");
    builder.append(this.volume);
    builder.append("]");
    return builder.toString();
  }
  
}