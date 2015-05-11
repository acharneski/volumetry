package com.simiacryptus.probabilityModel.volume;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.naming.OperationNotSupportedException;

import org.json.JSONException;
import org.json.JSONObject;

import com.simiacryptus.data.DoubleRange;
import com.simiacryptus.data.VolumeMetric;
import com.simiacryptus.lang.NotImplementedException;
import com.simiacryptus.probabilityModel.rules.MetricRule;
import com.simiacryptus.probabilityModel.rules.PartitionRule;
import com.simiacryptus.probabilityModel.rules.metrics.DimensionMetric;
import com.simiacryptus.util.ObjUtil;

public class RuleVolume implements SpacialVolume
{
  
  public final SpacialVolume       parentVolume;
  public final PartitionRule       rule;
  public final int                 rulePartition;
  private final List<double[]>     points         = new ArrayList<double[]>();
  private final Iterator<double[]> parentPoints;
  int                              matchedPoints  = 0;
  int                              totalPoints    = 0;
  private boolean                  isZeroVolume   = false;
  private static final int         cachedPointCap = 50000;
  
  public RuleVolume(final SpacialVolume parentVolume, final PartitionRule rule, final int rulePartition)
  {
    super();
    this.parentVolume = parentVolume;
    this.rule = rule;
    this.rulePartition = rulePartition;
    this.parentPoints = parentVolume.points().iterator();
  }
  
  @Override
  public boolean contains(final double[] point)
  {
    if (!this.parentVolume.contains(point))
    {
      return false;
    }
    if (this.rulePartition != this.rule.evaluate(point))
    {
      return false;
    }
    return true;
  }
  
  @Override
  public int dimensions()
  {
    return this.parentVolume.dimensions();
  }
  
  @Override
  public DoubleVolume getBounds()
  {
    final ArrayList<DoubleRange> ranges = new ArrayList<DoubleRange>();
    for (int i = 0; i < this.dimensions(); i++)
    {
      final DoubleRange range = this.getRange(i);
      if(null == range)
      {
        // TODO: This should never happen??
        throw new NullPointerException();
      }
      ranges.add(range);
    }
    return new DoubleVolume(ranges);
  }
  
  @Override
  public DoubleRange getRange(final int d)
  {
    final DoubleRange range = this.parentVolume.getRange(d);
    if (this.rule instanceof MetricRule)
    {
      final MetricRule metricRule = (MetricRule) this.rule;
      if (metricRule.metric instanceof DimensionMetric)
      {
        final DimensionMetric dimMetric = (DimensionMetric) metricRule.metric;
        if (d == dimMetric.dimension)
        {
          final DoubleRange metricRange = metricRule.getMetricRange(this.rulePartition);
          final DoubleRange intersect = range.intersect(metricRange);
          if(null == intersect)
          {
            // TODO: This should never happen??
            throw new NullPointerException();
          }
          return intersect;
        }
      }
    }
    return range;
  }
  
  public double getSieveFactor()
  {
    double sieveFactor = (double) this.matchedPoints / this.totalPoints;
    if (this.parentVolume instanceof RuleVolume)
    {
      sieveFactor *= ((RuleVolume) this.parentVolume).getSieveFactor();
    }
    return sieveFactor;
  }
  
  @Override
  public VolumeMetric getVolume()
  {
    if (this.isZeroVolume)
    {
      return new VolumeMetric(0, this.dimensions());
    }
    if (this.matchedPoints < 10)
    {
      new Random();
      while (this.matchedPoints < 10)
      {
        this.sample();
        if (this.isZeroVolume)
        {
          return new VolumeMetric(0, this.dimensions());
        }
      }
    }
    return this.parentVolume.getVolume().multiply((double) this.matchedPoints / this.totalPoints);
  }
  
  @Override
  public SpacialVolume intersect(final SpacialVolume right)
  {
    final SpacialVolume newParent = this.parentVolume.intersect(right);
    final RuleVolume ruleVolume = new RuleVolume(newParent, this.rule, this.rulePartition);
    for (final double[] p : this.points)
    {
      if (newParent.contains(p))
      {
        ruleVolume.points.add(p);
      }
    }
    return ruleVolume;
  }
  
  @Override
  public boolean intersects(final SpacialVolume range)
  {
    return this.parentVolume.intersects(range);
  }
  
  @Override
  public Iterable<double[]> points()
  {
    return new Iterable<double[]>() {
      
      @Override
      public Iterator<double[]> iterator()
      {
        return new Iterator<double[]>() {
          int index = 0;
          
          @Override
          public boolean hasNext()
          {
            return !RuleVolume.this.isZeroVolume;
          }
          
          @Override
          public double[] next()
          {
            if (RuleVolume.this.isZeroVolume)
            {
              return null;
            }
            final int i = this.index++;
            if (i >= RuleVolume.this.points.size())
            {
              return RuleVolume.this.sample();
            }
            else
            {
              return RuleVolume.this.points.get(i);
            }
          }
          
          @Override
          public void remove()
          {
            throw new RuntimeException(new OperationNotSupportedException());
          }
        };
      }
    };
  }
  
  @Override
  public double[] sample()
  {
    while (!this.isZeroVolume)
    {
      final double[] sample = this.parentPoints.next();
      if (null == sample)
      {
        this.isZeroVolume = true;
        break;
      }
      this.totalPoints++;
      if (this.rule.evaluate(sample) == this.rulePartition)
      {
        this.matchedPoints++;
        if (this.points.size() < cachedPointCap)
        {
          this.points.add(sample);
        }
        return sample;
      }
      else
      {
        if (0 == this.matchedPoints && 1000 < this.totalPoints)
        {
          this.isZeroVolume = true;
          break;
        }
      }
    }
    return null;
  }
  
  @Override
  public JSONObject toJson() throws JSONException
  {
    final JSONObject json = new JSONObject();
    json.put("parentVolume", this.parentVolume.toJson());
    json.put("rule", this.rule.toJson());
    json.put("rulePartition", this.rulePartition);
    json.put("totalPoints", this.totalPoints);
    json.put("matchedPoints", this.matchedPoints);
    json.put("volume", this.getVolume());
    json.put("bounds", this.getBounds().toJson());
    json.put("sieveFactor", this.getSieveFactor());
    return json;
  }
  
  @Override
  public String toString()
  {
    try
    {
      return ObjUtil.toJson(this).toString(2);
    }
    catch (final Exception e)
    {
      return e.toString();
    }
  }

  @Override
  public boolean isUnbounded()
  {
    return getBounds().isUnbounded();
  }

  @Override
  public SpacialVolume union(SpacialVolume right)
  {
    throw new NotImplementedException("Need to implement an binary-OR rule evaluator");
//    final SpacialVolume newParent = this.parentVolume.union(right);
//    final RuleVolume ruleVolume = new RuleVolume(newParent, this.rule, this.rulePartition);
//    ruleVolume.points.addAll(this.points);
//    return ruleVolume;
  }
  
}