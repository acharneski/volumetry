package com.simiacryptus.collections;

import java.util.ArrayList;
import java.util.Collection;

import com.google.common.collect.ForwardingCollection;

public final class ProxyCollection<T> extends ForwardingCollection<T>
{
  public Collection<T> inner = new ArrayList<T>();
  
  @Override
  protected Collection<T> delegate()
  {
    return this.inner;
  }
}