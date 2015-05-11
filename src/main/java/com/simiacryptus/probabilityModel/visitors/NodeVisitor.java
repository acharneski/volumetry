package com.simiacryptus.probabilityModel.visitors;

import com.simiacryptus.probabilityModel.model.DistributionModel;
import com.simiacryptus.probabilityModel.model.NodeBase;

public abstract class NodeVisitor<X extends NodeVisitor<X, T>, T extends NodeBase<T>>
{
  public X visit(final DistributionModel<T> tree, final int maxLevels)
  {
    return this.visit(tree.getRoot(), maxLevels);
  }
  
  @SuppressWarnings("unchecked")
  public
  X visit(final T tree, final int maxLevels)
  {
    if(null != tree)
    {
      this.visitBegin(tree);
      if (0 < maxLevels)
      {
        for (final T child : tree.getChildren())
        {
          this.visit(child, maxLevels - 1);
        }
      }
      this.visitEnd(tree);
    }
    return (X) this;
  }
  
  protected void visitBegin(final T node)
  {
  }
  
  protected void visitEnd(final T node)
  {
  }
}