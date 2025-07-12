package com.simiacryptus.util;

import com.simiacryptus.lang.LOG;
import com.simiacryptus.probabilityModel.Distribution;
import com.simiacryptus.probabilityModel.distributions.Test3dDistributions;
import com.simiacryptus.probabilityModel.distributions.VolumeSieveDistributionFactory;
import com.simiacryptus.probabilityModel.model.PointModel;
import com.simiacryptus.probabilityModel.volume.DoubleVolume;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class TestUtil
{

  public static final Random   random        = newRandom();

    private static final String[] defaultColors = new String[]{"#00FF00", "#0000FF", "#FF0000", "#FFFF00", "#000000"};

  public static double[] compareDensity(final DoubleVolume range, final int sample, Distribution pointSouce, Distribution densityFunction)
  {
      if (sample <= 0) {
          throw new IllegalArgumentException("Sample size must be positive");
      }
      // Check if density function is implemented
      try {
          double[] testPoint = new double[3]; // Assuming 3D
          densityFunction.getDensity().evaluate(testPoint);
      } catch (com.simiacryptus.lang.NotImplementedException e) {
          LOG.d("Density function not implemented for %s, returning zero density", densityFunction.getClass().getSimpleName());
          return new double[]{0.0, 0.0};
      }


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
          if (0 == selfDensity[i] || Math.abs(selfDensity[i]) < 1e-10)
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

  public static void fillModel(final PointModel model, final Distribution referenceDistribution, final int dataPoints)
  {
      if (model == null || referenceDistribution == null) {
          throw new IllegalArgumentException("Model and distribution cannot be null");
      }
      if (dataPoints <= 0) {
          throw new IllegalArgumentException("Data points must be positive");
      }

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
      if (model == null || sourceModel == null) {
          throw new IllegalArgumentException("Models cannot be null");
      }

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
  
  private static double[] getCoDensity(Distribution pointSouce, Distribution densityFunction, final int sample, final DoubleVolume range)
  {
      if (pointSouce == null || densityFunction == null) {
          throw new IllegalArgumentException("Distributions cannot be null");
      }

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
        double density;
        try {
            density = densityFunction.getDensity().evaluate(dataPoint)[0];
        } catch (com.simiacryptus.lang.NotImplementedException e) {
            LOG.d("Density function not implemented for %s, skipping density evaluation", densityFunction.getClass().getSimpleName());
            continue;
        }
        if (Double.isNaN(density) || Double.isInfinite(density)) {
            continue;
        }
      totalN2 += density;
        if (density > 0) {
            totalNLogN += Math.log(density);
        }
      count++;
    }
      if (count == 0) {
          return new double[]{0, 0};
      }
    return new double[] { totalN2 / count, totalNLogN / count };
  }
  
    public static ScatterPlot[] getScatterCharts(final int size, final DoubleVolume range, final Distribution... distributions) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive");
        }
        if (distributions == null || distributions.length == 0) {
            throw new IllegalArgumentException("At least one distribution must be provided");
        }

        final Random random = new Random(TestUtil.random.nextInt());
        ScatterPlot[] plots = new ScatterPlot[distributions.length];
        String[] datasetNames = new String[distributions.length];
        String[] datasetDescriptions = new String[distributions.length];

        for (int distIndex = 0; distIndex < distributions.length; distIndex++) {
            Distribution d = distributions[distIndex];
            datasetNames[distIndex] = d.getClass().getSimpleName();
            datasetDescriptions[distIndex] = "Distribution: " + d.getClass().getName();

            if (d instanceof Test3dDistributions) {
                d = new VolumeSieveDistributionFactory(d, range);
            }

            final String color = distIndex < defaultColors.length ? defaultColors[distIndex] : String.format("#%02X%02X%02X", (int) (random.nextFloat() * 255), (int) (random.nextFloat() * 255), (int) (random.nextFloat() * 255));

            Point3D[] points = new Point3D[size];
            for (int i = 0; i < size; i++) {
                final double[] dataPoint = d.sample(random);
                points[i] = new Point3D(dataPoint[0], dataPoint[1], 3 > dataPoint.length ? 0 : dataPoint[2], color);
            }

            plots[distIndex] = new ScatterPlot(points, "Distribution " + distIndex,
                    new String[]{datasetNames[distIndex]}, new String[]{datasetDescriptions[distIndex]});
        }

        return plots;
    }
  
    public static ScatterPlot getScatterChart(final int size, final DoubleVolume range, final Distribution... distributions)
  {
      if (size <= 0) {
          throw new IllegalArgumentException("Size must be positive");
      }
      if (distributions == null || distributions.length == 0) {
          throw new IllegalArgumentException("At least one distribution must be provided");
      }
      // For single distribution, use the optimized single plot generation
      if (distributions.length == 1) {
          ScatterPlot[] plots = getScatterCharts(size, range, distributions);
          return plots[0];
      }
      // For multiple distributions, we need to maintain separate datasets

      final Random random = new Random(TestUtil.random.nextInt());
      final Map<Distribution, Integer> distIndexMap = new HashMap<Distribution, Integer>();
      final Map<Distribution, String> distColorMap = new HashMap<Distribution, String>();
      final String[] datasetNames = new String[distributions.length];
      final String[] datasetDescriptions = new String[distributions.length];

      int distIndex = 0;
    for (Distribution d : distributions)
    {
        datasetNames[distIndex] = d.getClass().getSimpleName();
        datasetDescriptions[distIndex] = "Distribution: " + d.getClass().getName();

        if (d instanceof Test3dDistributions)
      {
        d = new VolumeSieveDistributionFactory(d, range);
      }
        final String color = distIndex < defaultColors.length ? defaultColors[distIndex] : String.format("#%02X%02X%02X", (int) (random.nextFloat() * 255), (int) (random.nextFloat() * 255), (int) (random.nextFloat() * 255));
        distIndexMap.put(d, distIndex);
        distColorMap.put(d, color);
        distIndex++;
    }


      // Create a list to hold points with their dataset indices
      class IndexedPoint {
          final Point3D point;
          final int datasetIndex;

          IndexedPoint(Point3D point, int datasetIndex) {
              this.point = point;
              this.datasetIndex = datasetIndex;
          }
      }

      final java.util.List<IndexedPoint> indexedPoints = new java.util.ArrayList<>(size);

    final AtomicInteger count = new AtomicInteger(0);
      final Object lock = new Object();

      for (int thread = 0; thread < 4; thread++)
    {
      new Thread(new Runnable() {

        @Override
        public void run()
        {
          while (count.get() < size)
          {
              for (final Entry<Distribution, String> e : distColorMap.entrySet())
            {
              int index = count.getAndIncrement();
              if (index >= size)
              {
                break;
              }
              final double[] dataPoint = e.getKey().sample(random);
                Point3D point = new Point3D(dataPoint[0], dataPoint[1], 3 > dataPoint.length ? 0 : dataPoint[2], e.getValue());
                synchronized (lock) {
                    indexedPoints.add(new IndexedPoint(point, distIndexMap.get(e.getKey())));
                }
            }
          }
        }
      }).start();
    }

      // Wait for all threads to complete
      while (count.get() < size) {
          try {
              Thread.sleep(10);
          } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
              break;
          }
      }
      // Convert to array maintaining dataset information
      Point3D[] points = new Point3D[indexedPoints.size()];
      for (int i = 0; i < indexedPoints.size(); i++) {
          points[i] = indexedPoints.get(i).point;
      }
      // Create a custom ScatterPlot that preserves dataset indices
      return new ScatterPlot(points, "3D Scatter Plot", datasetNames, datasetDescriptions) {
          @Override
          public int getDatasetIndex(int pointIndex) {
              if (pointIndex >= 0 && pointIndex < indexedPoints.size()) {
                  return indexedPoints.get(pointIndex).datasetIndex;
              }
              return 0;
          }

          @Override
          public int getDatasetCount() {
              return distributions.length;
          }
      };
  }

    private static Random newRandom()
  {
    final long nanoTime = System.nanoTime();
    final long seed = (nanoTime >> 32) + (nanoTime << 32);
    LOG.d("Initialized global random seed as 0x%s", Long.toHexString(seed));
    return new Random(seed);
  }

    public static synchronized Semaphore show(final ScatterPlot... plots) throws IOException
  {
      return show("test", plots);
  }

    public static Semaphore show(final String title, final ScatterPlot... plots) throws IOException
  {


      final Path tempFile = Files.createTempFile(title.replaceAll("[^a-zA-Z0-9]", "_"), ".html");
      generateHtmlPlot(tempFile, title, plots);
      Desktop.getDesktop().open(tempFile.toFile());
      return new Semaphore(1);
  }

    private static void generateHtmlPlot(Path file, String title, ScatterPlot... plots) throws IOException {
        String template = loadTemplate();
        StringBuilder plotData = new StringBuilder();
        StringBuilder datasetInfo = new StringBuilder();

        // Add diagnostic comment
        plotData.append("        // === PLOT DATA GENERATION DIAGNOSTIC ===\n");
        plotData.append("        // Total plots: ").append(plots.length).append("\n");
        // Generate dataset information
        datasetInfo.append("        // === DATASET INFORMATION ===\n");
        datasetInfo.append("        window.datasetInfo = {\n");
        for (ScatterPlot plot : plots) {
            for (int i = 0; i < plot.getDatasetCount(); i++) {
                datasetInfo.append("          ").append(i).append(": {\n");
                datasetInfo.append("            name: '").append(escapeJavaScript(plot.getDatasetName(i))).append("',\n");
                String description = plot.getDatasetDescription(i);
                if (description != null) {
                    datasetInfo.append("            description: '").append(escapeJavaScript(description)).append("',\n");
                }
                datasetInfo.append("            pointCount: 0\n"); // Will be updated during plot generation
                datasetInfo.append("          },\n");
            }
        }
        datasetInfo.append("        };\n");
        datasetInfo.append("        // === END DATASET INFORMATION ===\n\n");

        // Generate plot data for each scatter plot
        for (ScatterPlot plot : plots) {
            int plotIndex = java.util.Arrays.asList(plots).indexOf(plot);
            plotData.append("        // Plot: ").append(plot.getTitle()).append("\n");
            plotData.append("        // Plot index: ").append(plotIndex).append("\n");
            plotData.append("        // Point count: ").append(plot.getPoints().length).append("\n");


            plotData.append("        // Dataset count: ").append(plot.getDatasetCount()).append("\n");

            // Check if this plot has multiple datasets
            if (plot.getDatasetCount() > 1) {
                // Group points by dataset
                java.util.Map<Integer, java.util.List<Integer>> datasetPoints = new java.util.HashMap<>();
                Point3D[] points = plot.getPoints();
                for (int i = 0; i < points.length; i++) {
                    int datasetIndex = plot.getDatasetIndex(i);
                    datasetPoints.computeIfAbsent(datasetIndex, k -> new java.util.ArrayList<>()).add(i);
                }

                plotData.append("        // Creating separate geometries for each dataset\n");

                // Create separate geometry for each dataset
                for (java.util.Map.Entry<Integer, java.util.List<Integer>> entry : datasetPoints.entrySet()) {
                    int datasetIndex = entry.getKey();
                    java.util.List<Integer> indices = entry.getValue();
                    plotData.append("        // Update dataset point count\n");
                    plotData.append("        if (window.datasetInfo[").append(datasetIndex).append("]) {\n");
                    plotData.append("          window.datasetInfo[").append(datasetIndex).append("].pointCount += ").append(indices.size()).append(";\n");
                    plotData.append("        }\n");


                    plotData.append("        // Dataset ").append(datasetIndex).append(" with ").append(indices.size()).append(" points\n");
                    plotData.append("        const geometry").append(plotIndex).append("_").append(datasetIndex).append(" = new THREE.BufferGeometry();\n");
                    plotData.append("        const positions").append(plotIndex).append("_").append(datasetIndex).append(" = [];\n");
                    plotData.append("        const colors").append(plotIndex).append("_").append(datasetIndex).append(" = [];\n");

                    for (int idx : indices) {
                        Point3D point = points[idx];
                        if (point != null) {
                            plotData.append("        positions").append(plotIndex).append("_").append(datasetIndex).append(".push(").append(point.x).append(", ").append(point.y).append(", ").append(point.z).append(");\n");
                            String hexColor = point.color.substring(1); // Remove #
                            int r = Integer.parseInt(hexColor.substring(0, 2), 16);
                            int g = Integer.parseInt(hexColor.substring(2, 4), 16);
                            int b = Integer.parseInt(hexColor.substring(4, 6), 16);
                            plotData.append("        colors").append(plotIndex).append("_").append(datasetIndex).append(".push(").append(r / 255.0).append(", ").append(g / 255.0).append(", ").append(b / 255.0).append(");\n");
                        }
                    }

                    plotData.append("        geometry").append(plotIndex).append("_").append(datasetIndex).append(".setAttribute('position', new THREE.Float32BufferAttribute(positions").append(plotIndex).append("_").append(datasetIndex).append(", 3));\n");
                    plotData.append("        geometry").append(plotIndex).append("_").append(datasetIndex).append(".setAttribute('color', new THREE.Float32BufferAttribute(colors").append(plotIndex).append("_").append(datasetIndex).append(", 3));\n");
                    plotData.append("        const material").append(plotIndex).append("_").append(datasetIndex).append(" = new THREE.PointsMaterial({ size: 1.0, vertexColors: true, sizeAttenuation: false });\n");
                    plotData.append("        const points").append(plotIndex).append("_").append(datasetIndex).append(" = new THREE.Points(geometry").append(plotIndex).append("_").append(datasetIndex).append(", material").append(plotIndex).append("_").append(datasetIndex).append(");\n");
                    plotData.append("        scene.add(points").append(plotIndex).append("_").append(datasetIndex).append(");\n");
                    plotData.append("        window.registerPlotObject(points").append(plotIndex).append("_").append(datasetIndex).append(", ").append(datasetIndex).append("); // Registering with dataset index: ").append(datasetIndex).append("\n\n");
                }
            } else {
                // Single dataset - use original logic
                plotData.append("        // Update dataset point count\n");
                plotData.append("        if (window.datasetInfo[").append(plotIndex).append("]) {\n");
                plotData.append("          window.datasetInfo[").append(plotIndex).append("].pointCount += ").append(plot.getPoints().length).append(";\n");
                plotData.append("        }\n");

                plotData.append("        const geometry").append(plotIndex).append(" = new THREE.BufferGeometry();\n");
                plotData.append("        const positions").append(plotIndex).append(" = [];\n");
                plotData.append("        const colors").append(plotIndex).append(" = [];\n");
                // Track unique colors in this plot
                java.util.Set<String> uniqueColors = new java.util.HashSet<>();

                for (Point3D point : plot.getPoints()) {
                    if (point != null) {
                        plotData.append("        positions").append(plotIndex).append(".push(").append(point.x).append(", ").append(point.y).append(", ").append(point.z).append(");\n");
                        // Use individual point colors - they will be overridden by color schemes when needed
                        String hexColor = point.color.substring(1); // Remove #
                        int r = Integer.parseInt(hexColor.substring(0, 2), 16);
                        int g = Integer.parseInt(hexColor.substring(2, 4), 16);
                        int b = Integer.parseInt(hexColor.substring(4, 6), 16);
                        plotData.append("        colors").append(plotIndex).append(".push(").append(r / 255.0).append(", ").append(g / 255.0).append(", ").append(b / 255.0).append(");\n");
                        uniqueColors.add(point.color);
                    }
                }
                plotData.append("        // Unique colors in this plot: ").append(uniqueColors.size()).append(" - ").append(uniqueColors).append("\n");

                plotData.append("        geometry").append(plotIndex).append(".setAttribute('position', new THREE.Float32BufferAttribute(positions").append(plotIndex).append(", 3));\n");
                plotData.append("        geometry").append(plotIndex).append(".setAttribute('color', new THREE.Float32BufferAttribute(colors").append(plotIndex).append(", 3));\n");
                plotData.append("        const material").append(plotIndex).append(" = new THREE.PointsMaterial({ size: 1.0, vertexColors: true, sizeAttenuation: false });\n");
                plotData.append("        const points").append(plotIndex).append(" = new THREE.Points(geometry").append(plotIndex).append(", material").append(plotIndex).append(");\n");
                plotData.append("        scene.add(points").append(plotIndex).append(");\n");
                plotData.append("        window.registerPlotObject(points").append(plotIndex).append(", ").append(plotIndex).append("); // Registering with dataset index: ").append(plotIndex).append("\n\n");
            }
        }
        plotData.append("        // === END PLOT DATA GENERATION ===\n\n");

        // Replace placeholders in template
        String html = template.replace("{{TITLE}}", escapeHtml(title))
                .replace("{{DATASET_INFO}}", datasetInfo.toString())
                .replace("{{PLOT_DATA}}", plotData.toString());

        // Write the final HTML to file
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(file))) {
            writer.write(html);
        }
    }

    private static String loadTemplate() throws IOException {
        try (InputStream is = TestUtil.class.getResourceAsStream("/scatter-plot-template.html")) {
            if (is == null) {
                throw new IOException("Template file not found: /scatter-plot-template.html");
      }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#x27;");
    }

    private static String escapeJavaScript(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    public static class Point3D {
        public final double x, y, z;
        public final String color;

        public Point3D(double x, double y, double z, String color) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.color = color;
        }
    }

    public static class ScatterPlot {
        private final Point3D[] points;
        private final String title;
        private final String[] datasetNames;
        private final String[] datasetDescriptions;

        public ScatterPlot(Point3D[] points, String title) {
            this(points, title, null, null);
        }

        public ScatterPlot(Point3D[] points, String title, String[] datasetNames, String[] datasetDescriptions) {
            this.points = points;
            this.title = title;
            this.datasetNames = datasetNames;
            this.datasetDescriptions = datasetDescriptions;
    }

        public Point3D[] getPoints() {
            return points;
        }

        public String getTitle() {
            return title;
        }

        public String[] getDatasetNames() {
            return datasetNames;
        }

        public String[] getDatasetDescriptions() {
            return datasetDescriptions;
        }

        public String getDatasetName(int datasetIndex) {
            if (datasetNames != null && datasetIndex >= 0 && datasetIndex < datasetNames.length) {
                return datasetNames[datasetIndex];
            }
            return "Dataset " + datasetIndex;
        }

        public String getDatasetDescription(int datasetIndex) {
            if (datasetDescriptions != null && datasetIndex >= 0 && datasetIndex < datasetDescriptions.length) {
                return datasetDescriptions[datasetIndex];
            }
            return null;
        }

        // Override these methods in subclasses to provide dataset information
        public int getDatasetIndex(int pointIndex) {
            // Default implementation - all points belong to dataset 0
            return 0;
        }

        public int getDatasetCount() {
            // Default implementation - single dataset
            return 1;
    }
  }


}