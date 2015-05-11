package com.simiacryptus.collections;

import java.security.InvalidParameterException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import com.simiacryptus.lang.NotImplementedException;

public final class CountTree<T> implements Iterable<CountTree<T>.CountTreeNode>
{
  public final class CountTreeNode
  {
    protected final CountTreeNode parent;
    protected CountTreeNode       lower;
    protected CountTreeNode       upper;
    public final T                key;
    private final AtomicInteger   nodeTotal = new AtomicInteger(0);
    private final AtomicInteger   nodeCount = new AtomicInteger(1);
    private final AtomicInteger   treeTotal = new AtomicInteger(0);
    
    public CountTreeNode(final T value)
    {
      super();
      this.key = value;
      this.parent = null;
    }
    
    public CountTreeNode(final T value, final CountTreeNode parent)
    {
      super();
      this.key = value;
      this.parent = parent;
    }
    
    public int add(final T key)
    {
      return this.add(key, 1);
    }
    
    public int add(final T key, final int value)
    {
      final int compare = CountTree.this.comparator.compare(this.key, key);
      this.treeTotal.addAndGet(value);
      if (compare == 0)
      {
        return this.nodeTotal.addAndGet(value);
      }
      else if (compare > 0)
      {
        if (null == this.lower)
        {
          this.lower = new CountTreeNode(key, this);
          this.incrementNodeCount();
        }
        return this.lower.add(key);
      }
      else
      {
        if (null == this.upper)
        {
          this.upper = new CountTreeNode(key, this);
          this.incrementNodeCount();
        }
        return this.upper.add(key);
      }
    }
    
    public CountTreeNode get(int index)
    {
      if (0 > index) { throw new InvalidParameterException(); }
      if (this.size() <= index) { throw new InvalidParameterException(); }
      if (null != this.lower)
      {
        final int lowerSize = this.lower.size();
        if (index < lowerSize) { return this.lower.get(index); }
        index -= lowerSize;
      }
      if (0 == index) { return this; }
      if (null != this.upper) { return this.upper.get(index - 1); }
      throw new IllegalStateException();
    }
    
    public int getNodeCount()
    {
      return this.nodeCount.get();
    }
    
    public int getNodeTotal()
    {
      return this.nodeTotal.get();
    }
    
    public int getTreeTotal()
    {
      return this.treeTotal.get();
    }
    
    protected void incrementNodeCount()
    {
      this.nodeCount.incrementAndGet();
      if (null != this.parent)
      {
        this.parent.incrementNodeCount();
      }
    }
    
    /**
     * Return node with the greatest key which is smaller than or equal to the
     * given key
     * 
     * @param value
     * @return
     */
    public CountTreeNode maxNode(final T value)
    {
      final int compare = null == value ? -1 : CountTree.this.comparator
          .compare(this.key, value);
      if (compare == 0)
      {
        return this;
      }
      else if (compare < 0)
      {
        return null == this.upper ? this : this.upper.maxNode(value);
      }
      else
      {
        return null == this.lower ? null : this.lower.maxNode(value);
      }
    }
    
    /**
     * Return node with the smallest key which is greater to or equal to the
     * given key
     * 
     * @param value
     * @return
     */
    public CountTreeNode minNode(final T value)
    {
      final int compare = null == value ? 1 : CountTree.this.comparator
          .compare(this.key, value);
      if (compare == 0)
      {
        return this;
      }
      else if (compare < 0)
      {
        return null == this.upper ? null : this.upper.minNode(value);
      }
      else
      {
        return null == this.lower ? this : this.lower.minNode(value);
      }
    }
    
    public CountTreeNode next()
    {
      if (null != this.upper) { return this.upper.minNode(null); }
      CountTreeNode t = this;
      CountTreeNode p = this.parent;
      while (null != p && p.upper == t)
      {
        t = p;
        p = p.parent;
      }
      // p is the closest parent where this node is on the lower side of the tree
      // node is the highest node of tree p.lower
      return p;
    }
    
    public int nodeFrom()
    {
      if (null == this.lower) { return this.treeFrom(); }
      return this.lower.treeTo();
    }
    
    public int nodeTo()
    {
      return this.nodeFrom() + this.nodeTotal.get();
    }
    
    public int size()
    {
      return this.nodeCount.get();
    }
    
    protected int treeFrom()
    {
      CountTreeNode t = this;
      CountTreeNode p = this.parent;
      while (null != p && p.lower == t)
      {
        t = p;
        p = p.parent;
      }
      // p is the immediate parent where this node is on the upper side of
      // node is the lowest node of tree p.upper
      if (null == p) { return 0; }
      return p.treeFrom() + p.nodeTotal.get()
          + (null == p.lower ? 0 : p.lower.treeTotal.get());
    }
    
    protected int treeTo()
    {
      return this.treeFrom() + this.treeTotal.get();
    }
  }
  
  private final class NodeIterator implements
      Iterator<CountTree<T>.CountTreeNode>
  {
    CountTreeNode node;
    
    @Override
    public boolean hasNext()
    {
      if (null == this.node && null == CountTree.this.root) { return false; }
      if (null != this.node && null == this.node.next()) { return false; }
      return true;
    }
    
    @Override
    public CountTreeNode next()
    {
      if (null == this.node)
      {
        if (null != CountTree.this.root)
        {
          this.node = CountTree.this.root.minNode(null);
        }
      }
      else
      {
        this.node = this.node.next();
      }
      return this.node;
    }
    
    @Override
    @SuppressWarnings("deprecation")
    public void remove()
    {
      throw new NotImplementedException();
    }
  }
  
  private CountTreeNode       root;
  private final Comparator<T> comparator;
  
  public CountTree()
  {
    super();
    this.comparator = new Comparator<T>() {
      @Override
      @SuppressWarnings("unchecked")
      public int compare(final T o1, final T o2)
      {
        return ((Comparable<T>) o1).compareTo(o2);
      }
    };
  }
  
  public CountTree(final Comparator<T> comparator)
  {
    super();
    this.comparator = comparator;
  }
  
  public int add(final T key)
  {
    return this.add(key, 1);
  }
  
  public int add(final T key, final int value)
  {
    if (null == this.root)
    {
      this.root = new CountTreeNode(key);
    }
    return this.root.add(key, value);
  }
  
  public CountTreeNode get(final int index)
  {
    if (null == this.root) { return null; }
    return this.root.get(index);
  }
  
  @Override
  public Iterator<CountTreeNode> iterator()
  {
    return new NodeIterator();
  }
  
  /**
   * Return node with the greatest key which is smaller than or equal to the
   * given key
   * 
   * @param value
   * @return
   */
  public CountTreeNode maxNode(final T value)
  {
    if (null == this.root) { return null; }
    return this.root.maxNode(value);
  }
  
  /**
   * Return node with the smallest key which is greater to or equal to the
   * given key
   * 
   * @param value
   * @return
   */
  public CountTreeNode minNode(final T value)
  {
    if (null == this.root) { return null; }
    return this.root.minNode(value);
  }
  
  public int size()
  {
    if (null == this.root) { return 0; }
    return this.root.size();
  }
  
  public int sum()
  {
    if (null == this.root) { return 0; }
    return this.root.getTreeTotal();
  }
  
}