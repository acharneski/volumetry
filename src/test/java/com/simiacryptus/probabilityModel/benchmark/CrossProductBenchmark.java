package com.simiacryptus.probabilityModel.benchmark;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import com.simiacryptus.lang.LOG;
import com.simiacryptus.probabilityModel.benchmark.base.DataLearner;
import com.simiacryptus.probabilityModel.benchmark.base.DataSampler;
import com.simiacryptus.probabilityModel.benchmark.base.TestObject;
import com.simiacryptus.util.HtmlOutput;

public abstract class CrossProductBenchmark extends Benchmark
{

  protected boolean selfCross = true;
  private final List<TestObject> testObjects = Collections.unmodifiableList(setupTestObjects());
  public CrossProductBenchmark()
  {
    super();
  }

  protected abstract ArrayList<TestObject> setupTestObjects();

  private File getOutputFile()
  {
    try
    {
      return File.createTempFile(getClass().getSimpleName() + "_", ".html");
    } catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  @Test
  public final void statistics() throws JSONException, IOException
  {
    final HtmlOutput doc = new HtmlOutput(getOutputFile());
    final String method = Thread.currentThread().getStackTrace()[1].getMethodName();
    doc.printHeader1("Test: %s", method);
    final JSONObject results = new JSONObject();
    for (TestObject model : testObjects)
    {
      if(!(model instanceof DataLearner)) continue;
      LOG.d("Model: %s", model);
      doc.printHeader2("Model: %s", model);
      final JSONObject modelResults = new JSONObject();
      for (TestObject dataset : testObjects)
      {
        if(!(dataset instanceof DataSampler)) continue;
        if (model == dataset && !selfCross) continue;
        LOG.d("Data: %s", dataset);
        doc.printHeader3("Data: %s", dataset);
        try
        {
          final JSONObject testResult = train(doc, (DataLearner) model, (DataSampler) dataset);
          if (null != testResult && testResult.length() > 0)
          {
            doc.printTag(new String[] { "pre" }, testResult.toString(2));
            modelResults.put(dataset.getName(), testResult);
          }
        } catch (Exception e)
        {
          LOG.d("Error testing %s vs %s", model, dataset);
          e.printStackTrace(System.out);
        }
      }
      doc.printHeader3("Model Summary");
      doc.printTag(new String[] { "pre" }, modelResults.toString(2));
      results.put(model.getName(), modelResults);
    }
    doc.printHeader2("Data Summary");
    doc.printTag(new String[] { "pre" }, results.toString(2));
    doc.close();
    Desktop.getDesktop().open(doc.getFile());
  }


}