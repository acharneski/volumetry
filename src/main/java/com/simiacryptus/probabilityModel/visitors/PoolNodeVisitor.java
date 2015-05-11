package com.simiacryptus.probabilityModel.visitors;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;

import com.simiacryptus.probabilityModel.model.NodeBase;

public abstract class PoolNodeVisitor<X extends PoolNodeVisitor<X, T>, T extends NodeBase<T>> extends NodeVisitor<X, T>
{
  final ExecutorService pool;
  
  public PoolNodeVisitor()
  {
    this(null);
  }
  
  public PoolNodeVisitor(final ExecutorService pool)
  {
    super();
    this.pool = pool;
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public final X visit(final T tree, final int maxLevels)
  {
    final BlockingQueue<Future<?>> futures = new LinkedBlockingDeque<Future<?>>();
    this.visitPool(tree, maxLevels, futures);
    try
    {
      Future<?> f;
      while (null != (f = futures.poll()))
      {
        f.get();
      }
    }
    catch (final Exception e)
    {
      throw new RuntimeException(e);
    }
    return (X) this;
  }
  
  @Override
  protected void visitBegin(final T node)
  {
  }
  
  @Override
  protected final void visitEnd(final T node)
  {
  }
  
  private void visitPool(final T tree, final int maxLevels, final BlockingQueue<Future<?>> futures)
  {
    this.visitBegin(tree);
    if (0 < maxLevels)
    {
      for (final T child : tree.getChildren())
      {
        if (null == this.pool)
        {
          this.visitPool(child, maxLevels - 1, futures);
        }
        else
        {
          futures.add(this.pool.submit(new Runnable() {
            @Override
            public void run()
            {
              PoolNodeVisitor.this.visitPool(child, maxLevels - 1, futures);
            }
          }));
        }
      }
    }
  }
}