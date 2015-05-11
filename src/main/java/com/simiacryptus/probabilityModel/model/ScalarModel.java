package com.simiacryptus.probabilityModel.model;

public final class ScalarModel extends DistributionModel<ScalarNode>
{
  private final NodeBase<?> node;
  
  ScalarModel(final NodeBase<?> node)
  {
    this.node = node;
  }
  
  @Override
  protected ScalarNode constructRoot()
  {
    return new ScalarNode(this, this.node);
  }

  void setRoot(ScalarNode newRoot)
  {
    newRoot.setParent(null);
    root = newRoot;
  }
}