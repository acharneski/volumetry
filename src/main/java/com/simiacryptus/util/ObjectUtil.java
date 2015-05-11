package com.simiacryptus.util;

public class ObjectUtil
{

  public static String getId(Object obj)
  {
    return obj.getClass().getSimpleName() + "@" + Integer.toHexString(System.identityHashCode(obj));
  }
  
}
