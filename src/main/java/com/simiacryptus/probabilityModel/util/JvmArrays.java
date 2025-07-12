package com.simiacryptus.probabilityModel.util;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Modifications of JVM Arrays methods.
 * TODO: Review license and either contribute, replace, or reimplement from scratch
 * 
 * @author Monster
 */
public class JvmArrays
{
  /**
   * Tuning parameter: list size at or below which insertion sort will be
   * used in preference to mergesort or quicksort.
   */
  private static final int INSERTIONSORT_THRESHOLD = 7;

  public static double[] lookup(final int[] indexes, final double[] values)
  {
    final double[] reordered = new double[indexes.length];
    for (int i = 0; i < indexes.length; i++)
    {
      reordered[i] = values[indexes[i]];
    }
    return reordered;
  }
  
  /**
   * Src is the source array that starts at index 0
   * Dest is the (possibly larger) array destination with a possible offset
   * low is the index in dest to start sorting
   * high is the end index in dest to end sorting
   * off is the offset into src corresponding to low in dest
   */
  private static void mergeSort(final int[] src, final int[] dest, int low, int high, final int off, final Comparator<Integer> c)
  {
    final int length = high - low;
    
    // Insertion sort on smallest arrays
    if (length < INSERTIONSORT_THRESHOLD)
    {
      for (int i = low; i < high; i++)
      {
        for (int j = i; j > low && c.compare(dest[j - 1], dest[j]) > 0; j--)
        {
          swap(dest, j, j - 1);
        }
      }
      return;
    }
    
    // Recursively sort halves of dest into src
    final int destLow = low;
    final int destHigh = high;
    low += off;
    high += off;
    final int mid = low + high >>> 1;
    mergeSort(dest, src, low, mid, -off, c);
    mergeSort(dest, src, mid, high, -off, c);
    
    // If list is already sorted, just copy from src to dest. This is an
    // optimization that results in faster sorts for nearly ordered lists.
    if (c.compare(src[mid - 1], src[mid]) <= 0)
    {
      System.arraycopy(src, low, dest, destLow, length);
      return;
    }
    
    // Merge sorted halves (now in src) into dest
    for (int i = destLow, p = low, q = mid; i < destHigh; i++)
    {
      if (q >= high || p < mid && c.compare(src[p], src[q]) <= 0)
      {
        dest[i] = src[p++];
      }
      else
      {
        dest[i] = src[q++];
      }
    }
  }
  
  public static void sort(final int[] a, final Comparator<Integer> c)
  {
    mergeSort(Arrays.copyOf(a, a.length), a, 0, a.length, 0, c);
  }
  
  /**
   * Swaps x[a] with x[b].
   */
  private static void swap(final int[] x, final int a, final int b)
  {
    final int t = x[a];
    x[a] = x[b];
    x[b] = t;
  }
  
}
