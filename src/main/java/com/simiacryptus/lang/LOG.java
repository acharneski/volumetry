package com.simiacryptus.lang;

import java.util.Arrays;

public class LOG
{

  private static String toString(double[] point)
  {
    StringBuffer sb = new StringBuffer();
    for(double v : point)
    {
      if(0 < sb.length()) sb.append(", ");
      sb.append(String.format("%.3f", v));
    }
    return "[" + sb.toString() + "]";
  }

  public static void d(String msg, Object... args)
  {
    preprocessArgs(args);
    log(msg, args);
  }

  private static void preprocessArgs(Object... args)
  {
    for(int i=0;i<args.length;i++)
    {
      Class<? extends Object> c = args[i].getClass();
      if(c.isArray())
      {
        if(args[i] instanceof double[])
        {
          args[i] = toString((double[])args[i]);
        }
        else if(args[i] instanceof int[])
        {
          args[i] = Arrays.toString((int[])args[i]);
        }
        else if(args[i] instanceof long[])
        {
          args[i] = Arrays.toString((long[])args[i]);
        }
        else if(args[i] instanceof byte[])
        {
          args[i] = Arrays.toString((byte[])args[i]);
        }
        else
        {
          args[i] = Arrays.toString((Object[])args[i]);
        }
      }
    }
  }

  private static final long startTime = System.nanoTime();

  private static void log(String msg, Object[] args)
  {
    String formatted = String.format(msg, args);
    StackTraceElement caller = Thread.currentThread().getStackTrace()[3];
    double time = (System.nanoTime() - startTime) / 1000000000.;
    String line = String.format("[%.5f] (%s:%s) %s", time, caller.getFileName(), caller.getLineNumber(), formatted.replaceAll("\n", "\n\t"));
    System.out.println(line);
  }
  
}
