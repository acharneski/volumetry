package com.simiacryptus.util;

import java.awt.Desktop;
import java.awt.Frame;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.jzy3d.bridge.IFrame;
import org.jzy3d.chart.Chart;
import org.jzy3d.chart.ChartLauncher;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.maths.Rectangle;
import org.jzy3d.plot3d.primitives.AbstractDrawable;
import org.jzy3d.plot3d.primitives.Scatter;
import org.jzy3d.plot3d.rendering.canvas.Quality;

import com.google.common.collect.Multiset;
import com.simiacryptus.lang.LOG;
import com.simiacryptus.probabilityModel.Distribution;
import com.simiacryptus.probabilityModel.distributions.Test3dDistributions;
import com.simiacryptus.probabilityModel.distributions.VolumeSieveDistributionFactory;
import com.simiacryptus.probabilityModel.model.PointModel;
import com.simiacryptus.probabilityModel.volume.DoubleVolume;

public class TestUtil
{
  public static interface Visitor<T>
  {
    void run(T object);
  }
  
  public static final long     startTime     = System.nanoTime();
  
  public static final Random   random        = newRandom();
  
  private static final Color[] defaultColors = new Color[] { Color.GREEN, Color.BLUE, Color.RED, Color.YELLOW, Color.BLACK };
  
  public static <T> void assertEqual(final Multiset<T> expected, final Multiset<T> actual)
  {
    for (final T p : actual.elementSet())
    {
      Assert.assertEquals(expected.count(p), actual.count(p));
    }
    for (final T p : expected.elementSet())
    {
      Assert.assertEquals(expected.count(p), actual.count(p));
    }
  }
  
  public static double[] compareDensity(final DoubleVolume range, final int sample, Distribution pointSouce, Distribution densityFunction)
  {
    if (pointSouce instanceof Test3dDistributions)
    {
      pointSouce = new VolumeSieveDistributionFactory(pointSouce, range);
    }
    if (densityFunction instanceof Test3dDistributions)
    {
      densityFunction = new VolumeSieveDistributionFactory(densityFunction, range);
    }
    final double[] coDensity = getCoDensity(pointSouce, densityFunction, sample, range);
    if (!densityFunction.equals(pointSouce))
    {
      final double[] selfDensity = getCoDensity(densityFunction, densityFunction, sample, range);
      for (int i = 0; i < coDensity.length; i++)
      {
        if (0 == selfDensity[i])
        {
          continue;
        }
        if (Double.isInfinite(selfDensity[i]))
        {
          continue;
        }
        if (Double.isNaN(selfDensity[i]))
        {
          continue;
        }
        coDensity[i] /= selfDensity[i];
      }
    }
    final Object[] args = { pointSouce, densityFunction, coDensity[0], coDensity[1] };
    LOG.d("Points from %s in %s: %.5f (n*n), %.5f (n*log[n])", args);
    return coDensity;
  }
  
  public static void compareDensityMatrix(final DoubleVolume range, final int sample, final Distribution... sources)
  {
    for (final Distribution source : sources)
    {
      compareDensity(range, sample, source, source);
    }
    for (int i = 0; i < sources.length; i++)
    {
      for (int j = 0; j < sources.length; j++)
      {
        if (i == j)
        {
          continue;
        }
        compareDensity(range, sample, sources[i], sources[j]);
      }
    }
  }
  
  public static void fillModel(final PointModel model, final Distribution referenceDistribution, final int dataPoints)
  {
    for (int i = 0; i < dataPoints; i++)
    {
      final double[] dataPoint = referenceDistribution.sample(random);
      if (!model.getRegion().contains(dataPoint))
      {
        i--;
        continue;
      }
      model.addDataPoint(dataPoint);
    }
    LOG.d("Filled model with %s data points from %s", dataPoints, referenceDistribution);
  }
  
  public static void fillModel(final PointModel model, final PointModel sourceModel)
  {
    for (final double[] dataPoint : sourceModel.getDataPoints())
    {
      if (!model.getRegion().contains(dataPoint))
      {
        continue;
      }
      model.addDataPoint(dataPoint);
    }
    LOG.d("Filled model with %s data points from %s", sourceModel.getWeight(), sourceModel);
  }
  
  private static double[] getCoDensity(Distribution pointSouce, Distribution densityFunction, final int sample, final DoubleVolume range)
  {
    if (pointSouce instanceof Test3dDistributions)
    {
      pointSouce = new VolumeSieveDistributionFactory(pointSouce, range);
    }
    if (densityFunction instanceof Test3dDistributions)
    {
      densityFunction = new VolumeSieveDistributionFactory(densityFunction, range);
    }
    double totalN2 = 0;
    double totalNLogN = 0;
    double count = 0;
    for (int i = 0; i < sample; i++)
    {
      final double[] dataPoint = pointSouce.sample(random);
      final double density = densityFunction.getDensity().evaluate(dataPoint)[0];
      totalN2 += density;
      totalNLogN += Math.log(density);
      count++;
    }
    return new double[] { totalN2 / count, totalNLogN / count };
  }
  
  public static Scatter getScatterChart(final int size, final DoubleVolume range, final Distribution... distributions)
  {
    final Random random = new Random(TestUtil.random.nextInt());
    final Map<Distribution, Color> dists = new HashMap<Distribution, Color>();
    for (Distribution d : distributions)
    {
      if (d instanceof Test3dDistributions)
      {
        d = new VolumeSieveDistributionFactory(d, range);
      }
      final Color color = dists.size() < defaultColors.length ? defaultColors[dists.size()] : new Color(
          random.nextFloat(),
          random.nextFloat(),
          random.nextFloat());
      dists.put(d, color);
    }
    
    
    Coord3d[] coordinates = new Coord3d[size];
    Arrays.fill(coordinates, new Coord3d());
    Color[] colors = new Color[size];
    Arrays.fill(colors, Color.BLACK);
    final Scatter scatter = new Scatter(coordinates, colors);
    scatter.setWidth(2);
    scatter.setColor(Color.BLACK);
    
    final AtomicInteger count = new AtomicInteger(0);
    for (int thread = 0; thread < 4; thread++)
    {
      new Thread(new Runnable() {
        
        @Override
        public void run()
        {
          while (count.get() < size)
          {
            for (final Entry<Distribution, Color> e : dists.entrySet())
            {
              int index = count.getAndIncrement();
              if (index >= size)
              {
                break;
              }
              final double[] dataPoint = e.getKey().sample(random);
              coordinates[index] = new Coord3d(dataPoint[0], dataPoint[1], 3 > dataPoint.length ? 0 : dataPoint[2]);
              if(index%100==0) scatter.updateBounds();
            }
          }
          scatter.updateBounds();
        }
      }).start();
    }
    return scatter;
  }
  
  private static Random newRandom()
  {
    final long nanoTime = System.nanoTime();
    final long seed = (nanoTime >> 32) + (nanoTime << 32);
    LOG.d("Initialized global random seed as 0x%s", Long.toHexString(seed));
    return new Random(seed);
  }
  
  public static void openJson(final JSONObject json) throws IOException, FileNotFoundException, JSONException
  {
    openJson(UUID.randomUUID().toString(), json);
  }
  
  public static void openJson(final String filename, final JSONObject json) throws IOException, FileNotFoundException, JSONException
  {
    final File tempFile = File.createTempFile(filename, ".json");
    final PrintStream out = new PrintStream(tempFile);
    out.print(json.toString(2));
    out.close();
    Desktop.getDesktop().open(tempFile);
  }
  
  public static synchronized Semaphore show(final AbstractDrawable... drawables)
  {
    return show("test", drawables);
  }
  
  public static Semaphore show(final String title, final AbstractDrawable... drawables)
  {
    final Chart chart = AWTChartComponentFactory.chart(Quality.Advanced, "newt");
    for (final AbstractDrawable drawable : drawables)
    {
      chart.getScene().add(drawable);
    }
    ChartLauncher.configureControllers(chart, title, true, false);
    chart.render();
    final IFrame chartFrame = chart.getFactory().newFrame(chart, new Rectangle(200, 200, 600, 600), title);
    final Frame frame = (java.awt.Frame) chartFrame;
    final Semaphore onClosed = new Semaphore(0);
    frame.addWindowListener(new java.awt.event.WindowAdapter() {
      
      @Override
      public void windowClosing(final WindowEvent e)
      {
        onClosed.release();
        super.windowClosing(e);
      }
      
    });
    return onClosed;
  }
  
  public static void visitFiles(final File root, final TestUtil.Visitor<File> visitor)
  {
    for (final File file : root.listFiles())
    {
      if (file.isDirectory())
      {
        visitFiles(file, visitor);
      }
      else
      {
        visitor.run(file);
      }
    }
  }
  
}
