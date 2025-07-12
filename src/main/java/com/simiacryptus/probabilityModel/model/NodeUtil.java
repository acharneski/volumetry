package com.simiacryptus.probabilityModel.model;

import java.util.ArrayList;
import java.util.List;

public class NodeUtil
{

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
