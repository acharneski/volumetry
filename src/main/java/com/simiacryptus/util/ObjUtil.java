package com.simiacryptus.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ObjUtil
{
  
  public static JSONObject toJson(Object obj) throws JSONException, IllegalArgumentException, IllegalAccessException
  {
    return toJson(obj, new HashSet<ObjId>());
  }
  
  public static class ObjId
  {
    Object obj;
    
    public ObjId(Object obj)
    {
      this.obj = obj;
    }

    @Override
    public int hashCode()
    {
      return System.identityHashCode(obj);
    }
    
    @Override
    public boolean equals(Object obj)
    {
      if(null == obj) return false;
      if(!(obj instanceof ObjId)) return false;
      return (this.obj == ((ObjId)obj).obj);
    }
    
    @Override
    public String toString()
    {
      StringBuilder builder = new StringBuilder();
      builder.append(obj.getClass().getSimpleName());
      builder.append("@");
      builder.append(Integer.toHexString(System.identityHashCode(obj)));
      return builder.toString();
    }
    
  }
  
  public static JSONObject toJson(Object obj, HashSet<ObjId> stack) throws JSONException, IllegalArgumentException, IllegalAccessException
  {
    ObjId id = new ObjId(obj);
    if(!stack.add(id)) return null;
    JSONObject json;
    try
    {
      json = new JSONObject();
      json.put("_id", new ObjId(obj).toString());
      if (List.class.isAssignableFrom(obj.getClass()))
      {
        json.put("elements", toArray((List<?>) obj, stack));
      }
      else if (Map.class.isAssignableFrom(obj.getClass()))
      {
        json.put("entries", toArray(((Map<?,?>) obj).entrySet(), stack));
      }
      for (Field f : obj.getClass().getDeclaredFields())
      {
        if(0 != (f.getModifiers() & Modifier.STATIC)) continue;
        if(0 != (f.getModifiers() & Modifier.PRIVATE)) continue;
        f.setAccessible(true);
        Object value = f.get(obj);
        if (null == value) continue;
        if (f.getType().isPrimitive())
        {
          if (double.class.isAssignableFrom(f.getType()))
          {
            json.put(f.getName(), ((double) (Double) value));
          }
          else if (Double.class.isAssignableFrom(f.getType()))
          {
            json.put(f.getName(), ((double) (Double) value));
          }
          else if (int.class.isAssignableFrom(f.getType()))
          {
            json.put(f.getName(), ((int) (Integer) value));
          }
          else if (Integer.class.isAssignableFrom(f.getType()))
          {
            json.put(f.getName(), ((int) (Integer) value));
          }
          else if (Long.class.isAssignableFrom(f.getType()))
          {
            json.put(f.getName(), ((long) (Long) value));
          }
          else if (long.class.isAssignableFrom(f.getType()))
          {
            json.put(f.getName(), ((long) (Long) value));
          }
          else if (Boolean.class.isAssignableFrom(f.getType()))
          {
            json.put(f.getName(), ((boolean) (Boolean) value));
          }
          else if (boolean.class.isAssignableFrom(f.getType()))
          {
            json.put(f.getName(), ((boolean) (Boolean) value));
          }
          else
          {
            throw new RuntimeException(f.toString());
          }
        }
        else if (String.class.isAssignableFrom(f.getType()))
        {
          json.put(f.getName(), ((String) value));
        }
        else if (value.getClass().isArray())
        {
          if (int[].class.isAssignableFrom(f.getType()))
          {
            json.put(f.getName(), ((int[]) value));
          }
          else if (double[].class.isAssignableFrom(f.getType()))
          {
            json.put(f.getName(), ((double[]) value));
          }
          else if (char[].class.isAssignableFrom(f.getType()))
          {
            json.put(f.getName(), ((char[]) value));
          }
          else
          {
            json.put(f.getName(), toArray((Object[]) value, stack));
          }
        }
        else
        {
          assert (!f.getType().isPrimitive());
          json.put(f.getName(), toJson(value, stack));
        }
      }
    }
    finally
    {
      stack.remove(id);
    }
    return json;
  }
  
  private static JSONArray toArray(Iterable<?> value, HashSet<ObjId> stack) throws IllegalArgumentException, JSONException, IllegalAccessException
  {
    JSONArray array = new JSONArray();
    for (Object v : value)
    {
      if (null == v)
      {
        array.put((Object) null);
      }
      else
      {
        array.put(toJson(v,stack));
      }
    }
    return array;
  }
  
  private static JSONArray toArray(Object[] x, HashSet<ObjId> stack) throws IllegalArgumentException, JSONException, IllegalAccessException
  {
    JSONArray array = new JSONArray();
    for (Object v : x)
    {
      if (null == v)
      {
        array.put((Object) null);
      }
      else
      {
        array.put(toJson(v,stack));
      }
    }
    return array;
  }
  
}
