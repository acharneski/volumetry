package com.simiacryptus.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONObject;

import com.google.common.collect.Lists;

public class HtmlOutput
{

  private final PrintWriter out;
  private final AtomicBoolean isClosed = new AtomicBoolean(true);
  private final File file;

  public HtmlOutput(File file)
  {
    try
    {
      this.file = file;
      file.mkdirs();
      this.out = new PrintWriter(new FileWriter(file));
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
    print("<html>");
    print("<head>");
    printTag(new String[]{"title"},"Model Tree Test Output");
    print("</head>");
    print("<body>");
  }

  @Override
  protected void finalize() throws Throwable
  {
    close();
    super.finalize();
  }

  public void close() throws IOException
  {
    if(isClosed.getAndSet(false))
    {
      print("</body></html>");
      this.out.close();
    }
  }

  public void printHeader1(String msg, Object... args)
  {
    printTag(new String[]{"h1"},msg, args);
  }

  public void printHeader2(String msg, Object... args)
  {
    printTag(new String[]{"h2"},msg, args);
  }

  public void printHeader3(String msg, Object... args)
  {
    printTag(new String[]{"h3"},msg, args);
  }

  public void printTag(String[] tags, String msg, Object... args)
  {
    final StringBuffer sb = new StringBuffer();
    for(String tag : tags)
    {
      sb.append("<");
      sb.append(tag);
      sb.append(">");
    }
    sb.append(msg);
    for(String tag : Lists.reverse(Lists.newArrayList(tags)))
    {
      sb.append("</");
      sb.append(tag);
      sb.append(">");
    }
    print(sb.toString(), args);
  }

  public void print(String msg, Object... args)
  {
    out.println(String.format(msg, args));
    out.flush();
  }

  public void flush()
  {
    out.flush();
  }

  public void addSupplementalJson(String label, JSONObject json)
  {
    try
    {
      final String name = UUID.randomUUID().toString() + ".json";
      final FileWriter out = new FileWriter(new File(file.getParent(), name));
      out.write(json.toString(2));
      out.close();
      print("<a href='%s' target='_blank'>%s</a>", name, label);
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  public File getFile()
  {
    return file;
  }
  
  
  
}
