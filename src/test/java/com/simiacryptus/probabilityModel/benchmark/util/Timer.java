package com.simiacryptus.probabilityModel.benchmark.util;

public class Timer
{
  private long startTime = System.nanoTime();
  
  public double getSeconds()
  {
    long endTime = System.nanoTime();
    double duration = (endTime - startTime) / 1000000000.;
    return duration;
  }
  
  public double getSecondsAndReset()
  {
    final double seconds = getSeconds();
    reset();
    return seconds;
  }
  
  public void reset()
  {
    startTime = System.nanoTime();
  }
  
  long   endTime  = System.nanoTime();
  double duration = (endTime - startTime) / 1000000000.;
  
}