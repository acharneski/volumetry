package com.simiacryptus.probabilityModel.model;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;


import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.simiacryptus.codes.HammingSymbol;

public class NodeUtil
{
  
  public static <T extends NodeBase<T>> TreeSet<HammingSymbol<T>> getLeafSymbols(final NodeBase<T> distributionModelNode)
  {
    final List<HammingSymbol<T>> list = Lists.transform(NodeUtil.getLeaves(distributionModelNode), new Function<T, HammingSymbol<T>>() {
      @Override
      public HammingSymbol<T> apply(final T input)
      {
        return new HammingSymbol<T>((int) input.getWeight(), input);
      }
    });
    final TreeSet<HammingSymbol<T>> set = new TreeSet<HammingSymbol<T>>();
    for (final HammingSymbol<T> item : list)
    {
      if (!set.add(item))
      {
        // set.floor(item);
        assert false;
      }
    }
    assert set.size() == list.size();
    return set;
  }
  
  @SuppressWarnings("unchecked")
  public static <T extends NodeBase<T>> List<T> getLeaves(final NodeBase<T> distributionModelNode)
  {
    final ArrayList<T> list = new ArrayList<T>();
    if(null != distributionModelNode)
    {
      if (null == distributionModelNode.getRule())
      {
        list.add((T) distributionModelNode);
      }
      else
      {
        for (final T child : distributionModelNode.getChildren())
        {
          list.addAll(getLeaves(child));
        }
      }
    }
    return list;
  }
  
}
