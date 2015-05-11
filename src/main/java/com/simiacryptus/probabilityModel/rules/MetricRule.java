package com.simiacryptus.probabilityModel.rules;

import java.security.InvalidParameterException;
import java.util.Arrays;

import org.json.JSONException;
import org.json.JSONObject;

import com.simiacryptus.data.DoubleRange;
import com.simiacryptus.probabilityModel.rules.metrics.DimensionMetric;
import com.simiacryptus.probabilityModel.rules.metrics.Metric;
import com.simiacryptus.probabilityModel.volume.DoubleVolume;
import com.simiacryptus.probabilityModel.volume.RuleVolume;
import com.simiacryptus.probabilityModel.volume.SpacialVolume;

public class MetricRule implements PartitionRule
{
  public final Metric           metric;
  public final double[]         splitValue;
  private final SpacialVolume[] subVolumes;
  public final SpacialVolume    range;
  
  public MetricRule(final SpacialVolume range, final Metric metric, final double... splitValue)
  {
    this.metric = metric;
    this.range = range;
    this.splitValue = Arrays.copyOf(splitValue, splitValue.length);
    Arrays.sort(this.splitValue);
    this.subVolumes = new SpacialVolume[splitValue.length + 1];
    for (int i = 0; i <= this.splitValue.length; i++)
    {
      this.subVolumes[i] = this.getSubVolume(i);
    }
  }
  
  @Override
  public boolean equals(final Object obj)
  {
    if (this == obj)
    {
      return true;
    }
    if (obj == null)
    {
      return false;
    }
    if (this.getClass() != obj.getClass())
    {
      return false;
    }
    final MetricRule other = (MetricRule) obj;
    if (!this.metric.equals(other.metric))
    {
      return false;
    }
    if (this.splitValue.length != other.splitValue.length)
    {
      return false;
    }
    for (int i = 0; i < this.splitValue.length; i++)
    {
      if (Double.doubleToLongBits(this.splitValue[i]) != Double.doubleToLongBits(other.splitValue[i]))
      {
        return false;
      }
    }
    return true;
  }
  
  @Override
  public int evaluate(final double[] fromIndex)
  {
    for (int i = 0; i < this.splitValue.length; i++)
    {
      if (this.metric.evaluate(fromIndex) < this.splitValue[i])
      {
        return i;
      }
    }
    return this.splitValue.length;
  }
  
  public DoubleRange getMetricRange(final int rulePartition)
  {
    if (rulePartition < 0)
    {
      throw new IllegalArgumentException();
    }
    if (rulePartition > this.splitValue.length)
    {
      throw new IllegalArgumentException();
    }
    if (rulePartition == 0)
    {
      return new DoubleRange(-Double.MAX_VALUE, this.splitValue[rulePartition]);
    }
    if (rulePartition == this.splitValue.length)
    {
      return new DoubleRange(this.splitValue[rulePartition - 1], Double.MAX_VALUE);
    }
    return new DoubleRange(this.splitValue[rulePartition - 1], this.splitValue[rulePartition]);
  }
  
  @Override
  public int getPartitions()
  {
    return this.subVolumes.length;
  }
  
  protected SpacialVolume getSubVolume(final int n)
  {
    final SpacialVolume intersectionVolume;
    // TODO: Untangle unidimensional and nonlinear rule logic to not share the code paths with instanceof logic
    if (this.metric instanceof DimensionMetric && range instanceof DoubleVolume)
    {
      final DimensionMetric dimensionMetric = (DimensionMetric) this.metric;
      final DoubleVolume doubleVolume = DoubleVolume.unbounded(range.dimensions());
      doubleVolume.set(dimensionMetric.dimension, getMetricRange(n));
      intersectionVolume = doubleVolume;
    }
    else
    {
      intersectionVolume = new RuleVolume(range.getBounds(), this, n);
    }
    // After assembling the rectangular bounds, do an intersection to pick up any nonlinear boundaries.
    return range.intersect(intersectionVolume);
  }
  
  @Override
  public SpacialVolume[] getSubVolumes()
  {
    return this.subVolumes;
  }
  
  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + this.metric.hashCode();
    for (final double element : this.splitValue)
    {
      long temp;
      temp = Double.doubleToLongBits(element);
      result = prime * result + (int) (temp ^ temp >>> 32);
    }
    return result;
  }
  
  @Override
  public JSONObject toJson() throws JSONException
  {
    final JSONObject json = new JSONObject();
    json.put("metric", this.metric.toJson());
    json.put("value", this.splitValue);
    return json;
  }
  
  @Override
  public String toString()
  {
    try
    {
      return this.toJson().toString();
    }
    catch (final JSONException e)
    {
      return e.getMessage();
    }
  }
  
}