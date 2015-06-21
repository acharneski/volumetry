package com.simiacryptus.probabilityModel.optimizer;

import java.util.Arrays;
import java.util.Iterator;

import com.simiacryptus.probabilityModel.model.PointNode;
import com.simiacryptus.probabilityModel.rules.fitness.IndexFitnessFunction;

public final class IndexScanOptimizer
{
  
  public final class IndexIterable implements Iterable<int[]>
  {
    @Override
    public Iterator<int[]> iterator()
    {
      return new IndexIterator();
    }
  }
  
  public final class IndexIterator implements Iterator<int[]>
  {
    private int[] currentValue;
    
    public IndexIterator()
    {
      this.currentValue = null;
    }
    
    @Override
    public boolean hasNext()
    {
      if (null == this.currentValue)
      {
        return true;
      }
      if (this.currentValue[0] < IndexScanOptimizer.this.maxIndex - 1)
      {
        return true;
      }
      for (int i = 1; i < IndexScanOptimizer.this.dimensions; i++)
      {
        if (this.currentValue[i] < this.currentValue[i - 1] - 1)
        {
          return true;
        }
      }
      return false;
    }
    
    @Override
    public int[] next()
    {
      if (null == this.currentValue)
      {
        this.currentValue = new int[IndexScanOptimizer.this.dimensions];
        for (int i = 0; i < IndexScanOptimizer.this.dimensions; i++)
        {
          this.currentValue[i] = IndexScanOptimizer.this.dimensions - (i + 1);
        }
      }
      else
      {
        for (int i = IndexScanOptimizer.this.dimensions - 1; i >= 0; i--)
        {
          int indexValue = this.currentValue[i];
          indexValue++;
          if (0 < i)
          {
            if (indexValue >= this.currentValue[i - 1])
            {
              this.currentValue[i] = 0;
            }
            else
            {
              this.currentValue[i] = indexValue;
              break;
            }
          }
          else
          {
            if (indexValue >= IndexScanOptimizer.this.maxIndex)
            {
              throw new IllegalStateException();
            }
            else
            {
              this.currentValue[i] = indexValue;
            }
          }
        }
      }
      return Arrays.copyOf(this.currentValue, this.currentValue.length);
    }
    
    @Override
    public void remove()
    {
      throw new UnsupportedOperationException();
    }
  }
  
  private int dimensions;
  private int maxIndex;
  
  public IndexScanOptimizer(final int dimensions, final int maxIndex)
  {
    this.dimensions = dimensions;
    this.maxIndex = maxIndex;
  }
  
  private Iterable<int[]> getParameterIterator()
  {
    return new IndexIterable();
  }
  
  public OptimizerResult<int[], Double> optimize(PointNode node, final IndexFitnessFunction function)
  {
    double maxFitness = -Double.MAX_VALUE;
    int[] maxParameter = null;
    for (final int[] parameter : this.getParameterIterator())
    {
      final double f = function.evaluate(node, parameter);
      if (f > maxFitness)
      {
        maxFitness = f;
        maxParameter = parameter;
      }
    }
    if(null == maxParameter) 
    {
      throw new RuntimeException();
    }
    return new OptimizerResult<int[], Double>(maxParameter, maxFitness);
  }
}