package com.simiacryptus.probabilityModel;

import com.simiacryptus.data.DoubleRange;
import com.simiacryptus.probabilityModel.volume.DoubleVolume;
import org.junit.After;
import org.junit.BeforeClass;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class TestBase {

  final List<Semaphore> finishSemaphores = new ArrayList<Semaphore>();
  
  @BeforeClass
  public static void setupModelTest2()
  {
    // Setup for HTML-based plotting
    System.out.println("Using HTML/JavaScript-based 3D plotting");
  }
  
  @After//Class
  public void teardownModelTest2() throws InterruptedException {
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
  
  final DoubleVolume range        = new DoubleVolume(new DoubleRange(0, 1), new DoubleRange(0, 1), new DoubleRange(0, 1));
  int                        plotSize     = 100000;
  int                        trainingSize = 5000;
  
}