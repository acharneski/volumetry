package com.simiacryptus.probabilityModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.junit.After;
import org.junit.BeforeClass;
import org.jzy3d.chart.ChartLauncher;
import org.jzy3d.chart.Settings;
import org.jzy3d.utils.LoggerUtils;

import com.simiacryptus.data.DoubleRange;
import com.simiacryptus.probabilityModel.volume.DoubleVolume;

public class TestBase {
  
  List<Semaphore> finishSemaphores = new ArrayList<Semaphore>();
  
  @BeforeClass
  public static void setupModelTest2()
  {
    LoggerUtils.minimal();
    Settings.getInstance().setHardwareAccelerated(true);
    ChartLauncher.instructions();
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
  
  final DoubleVolume range        = new DoubleVolume(new DoubleRange(0, 1), new DoubleRange(0, 1), new DoubleRange(0, 1));
  int                        plotSize     = 100000;
  int                        trainingSize = 5000;
  
}
