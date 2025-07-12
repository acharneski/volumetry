# Technical Code Tour: Probability Model Library

This is a comprehensive Java library for building and working with probability distribution models using spatial partitioning and tree-based data structures. Let me walk you through the key components and their interactions.

## Core Architecture Overview

The library is built around a hierarchical tree structure where each node represents a region of space with associated data points and probability density. The main components are:

1. **Distribution Models** - Top-level containers for probability trees
2. **Nodes** - Individual tree nodes representing spatial regions
3. **Spatial Volumes** - Geometric representations of space regions
4. **Partition Rules** - Logic for splitting space into subregions
5. **Visitors** - Pattern for traversing and operating on trees

## 1. Distribution Models (`com.simiacryptus.probabilityModel.model`)

### `DistributionModel<T>`
The abstract base class for all probability models. Key features:

```java
public abstract class DistributionModel<T extends NodeBase<T>>
{
  protected volatile T root;

  public ScalarModel evaluate(final BinaryNodeFunction f, final DistributionModel<?> right)
  {
    // Combines two models using a binary function
    final ScalarModel scalarModel = new ScalarModel(null);
    scalarModel.setRoot(evaluate(f, this.getRoot(), right.getRoot()));
    return scalarModel;
  }

  public double getDensity(double[] p)
  {
    final T leaf = getRoot().getLeaf(p);
    final VolumeMetric volume = leaf.getVolumeFraction();
    if(0 == volume.value) return 0;
    return getWeightFraction(leaf) / volume.value;
  }
}
```

**Key Methods:**
- `getDensity(double[] p)` - Returns probability density at a point
- `evaluate(BinaryNodeFunction f, DistributionModel<?> right)` - Combines models
- `slice(DoubleVolume range)` - Extracts a subset of the model
- `project(ProjectionNodeFunction fn, int... dimensions)` - Projects to lower dimensions

### `PointModel`
Concrete implementation for point-based data:

```java
public final class PointModel extends DistributionModel<PointNode>
{
  public void addDataPoint(final double[] point)
  {
    this.getRoot().addDataPoint(point);
  }

  protected PointNode constructRoot()
  {
    return new PointNode(this.range, this);
  }
}
```

## 2. Node Hierarchy

### `NodeBase<T>`
Abstract base for all tree nodes:

```java
public abstract class NodeBase<T extends NodeBase<T>>
{
  private SpacialVolume region;
  private PartitionRule rule = null;
  private final List<T> children = new ArrayList<T>();
  private int nodeCount = 1;

  public final T getLeaf(final double[] point)
  {
    if (!contains(point)) return null;
    if (null == this.getRule()) return (T) this;

    final int index = this.getRule().evaluate(point);
    final T child = this.getChildren().get(index);
    return child.getLeaf(point);
  }
}
```

**Key Features:**
- Maintains spatial region and partition rule
- Tracks child nodes and total node count
- Provides leaf lookup for point queries

### `PointNode`
Stores actual data points:

```java
public class PointNode extends NodeBase<PointNode>
{
  private final ProxyCollection<double[]> dataPoints = new ProxyCollection<double[]>();
  private int dataSize = 0;

  public synchronized void addDataPoint(final double[] point)
  {
    final PointNode leaf = this.getLeaf(point);
    leaf.dataPoints.add(point);
    leaf.addDataSize(1);
  }
}
```

### `ScalarNode`
Represents computed probability values:

```java
public class ScalarNode extends NodeBase<ScalarNode>
{
  private double weight;

  ScalarNode slice(DoubleVolume range)
  {
    SpacialVolume newRegion = range.intersect(previousRegion);
    setRegion(newRegion);
    // Filter and update children based on new region
    // ...
  }
}
```

## 3. Spatial Volumes (`com.simiacryptus.probabilityModel.volume`)

### `SpacialVolume` Interface
Defines geometric regions in space:

```java
public interface SpacialVolume
{
  boolean contains(double[] point);
  int dimensions();
  DoubleVolume getBounds();
  VolumeMetric getVolume();
  SpacialVolume intersect(SpacialVolume right);
  double[] sample();
  Iterable<double[]> points();
}
```

### `DoubleVolume`
Axis-aligned bounding boxes:

```java
public class DoubleVolume extends ArrayList<DoubleRange> implements SpacialVolume
{
  public boolean contains(final double[] point)
  {
    for (int i = 0; i < this.size(); i++)
    {
      if (!this.get(i).contains(point[i])) return false;
    }
    return true;
  }

  public VolumeMetric getVolume()
  {
    double value = 1;
    int dimension = 0;
    for (final DoubleRange range : this)
    {
      if (range.isUnbounded()) continue;
      value *= range.size();
      dimension++;
    }
    return new VolumeMetric(value, dimension);
  }
}
```

### `RuleVolume`
Volumes defined by partition rules:

```java
public class RuleVolume implements SpacialVolume
{
  public final SpacialVolume parentVolume;
  public final PartitionRule rule;
  public final int rulePartition;

  public boolean contains(final double[] point)
  {
    if (!this.parentVolume.contains(point)) return false;
    if (this.rulePartition != this.rule.evaluate(point)) return false;
    return true;
  }
}
```

## 4. Partition Rules (`com.simiacryptus.probabilityModel.rules`)

### `PartitionRule` Interface
Defines how to split space:

```java
public interface PartitionRule
{
  int evaluate(final double[] point);
  int getPartitions();
  SpacialVolume[] getSubVolumes();
}
```

### `MetricRule`
Splits based on metric thresholds:

```java
public class MetricRule implements PartitionRule
{
  public final Metric metric;
  public final double[] splitValue;

  public int evaluate(final double[] point)
  {
    for (int i = 0; i < this.splitValue.length; i++)
    {
      if (this.metric.evaluate(point) < this.splitValue[i])
      {
        return i;
      }
    }
    return this.splitValue.length;
  }
}
```

### Rule Generation
The `MetricRuleGenerator` finds optimal splits:

```java
public class MetricRuleGenerator implements RuleGenerator
{
  private final SplitFitness splitFitness;

  public PartitionRule getRule(PointNode node)
  {
    final TreeSet<RuleCandidate> candidates = new TreeSet<RuleCandidate>();
    for (final Metric metric : this.getCandidateMetrics(node))
    {
      final RuleCandidate candidate = this.getRuleCandidate(node, metric);
      if (null != candidate) candidates.add(candidate);
    }
    return candidates.last().rule; // Best fitness
  }
}
```

## 5. Advanced Features

### PCA-Based Partitioning
The library includes Principal Component Analysis for intelligent partitioning:

```java
public class PCARuleGenerator extends MetricRuleGenerator
{
  public Collection<Metric> getCandidateMetrics(PointNode node)
  {
    final double[][] dataPoints = node.getDataPoints().toArray(new double[][]{});
    final Covariance covariance = new Covariance(dataMatrix, false);
    final EigenDecomposition eigenDecomposition = new EigenDecomposition(covariance.getCovarianceMatrix());

    for(int i=0;i<eigenvalues.length;i++)
    {
      final RealVector eigenvector = eigenDecomposition.getEigenvector(i);
      list.add(new LinearMetric(eigenvector, eigenvalue, centroid));
    }
    return list;
  }
}
```

### Fitness Functions
Multiple fitness functions for evaluating splits:

```java
public final class DataEntropySplitFitness extends LinearSplitFitness
{
  public double getValue(PointNode node, final VolumeDataDensity item)
  {
    if(0 >= item.data) return -Double.MAX_VALUE;
    if(0 >= item.volume) return -Double.MAX_VALUE;
    final double fitness = item.data * MathUtil.log2(item.volume);
    return -fitness; // Minimize entropy
  }
}
```

### Model Sampling
Generate samples from learned distributions:

```java
public final class ModelSampler implements Distribution
{
  public double[] sample(final Random random)
  {
    final NodeBase<?> root = this.model.getRoot();
    final NodeBase<?> leaf = getLeaf(random, root);
    return getPointIterator(leaf).next();
  }

  private static <T extends NodeBase<T>> T getLeaf(final Random random, final NodeBase<T> node)
  {
    if (node.getChildren().size() == 0) return (T) node;

    double fate = random.nextDouble() * node.getWeight();
    for (final Entry<T, Double> e : childrenWeights.entrySet())
    {
      fate -= e.getValue();
      if (fate < 0) return getLeaf(random, e.getKey());
    }
  }
}
```

## 6. Visitor Pattern Implementation

### `NodeVisitor<X, T>`
Traverses tree structures:

```java
public abstract class NodeVisitor<X extends NodeVisitor<X, T>, T extends NodeBase<T>>
{
  public X visit(final T tree, final int maxLevels)
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
}
```

### `ModelPartitioner`
Automatically partitions models:

```java
public class ModelPartitioner extends PoolNodeVisitor<ModelPartitioner, PointNode>
{
  public void visitBegin(final PointNode node)
  {
    if (this.minPointThreshold >= node.getWeight()) return;

    final PartitionRule rule = this.ruleGenerator.getRule(node);
    if (null != rule) node.setRule(rule);
  }
}
```

## 7. Usage Patterns

### Basic Model Creation and Training
```java
// Create model with bounded region
DoubleVolume bounds = new DoubleVolume(
    new DoubleRange(0, 10),
    new DoubleRange(0, 10)
);
PointModel model = new PointModel(bounds);

// Add training data
for(double[] point : trainingData) {
    model.addDataPoint(point);
}

// Partition the model
new ModelPartitioner(new DataEntropySplitFitness())
    .setMinPointThreshold(10)
    .visit(model, Integer.MAX_VALUE);
```

### Density Estimation
```java
// Query density at specific points
double density = model.getDensity(new double[]{5.0, 7.5});

// Sample from the learned distribution
ModelSampler sampler = new ModelSampler(model);
double[][] samples = sampler.sample(1000, new Random());
```

### Model Operations
```java
// Combine two models
ScalarModel combined = model1.evaluate(new BinaryNodeFunction() {
    public double evaluate(double left, double right, VolumeMetric volume) {
        return left * right; // Multiply densities
    }
}, model2);

// Project to lower dimensions
ScalarModel projected = model.project(new ProjectionNodeFunction() {
    public double evaluate(ScalarNode... nodes) {
        return Arrays.stream(nodes).mapToDouble(n -> n.getWeight()).sum();
    }
}, 0); // Project onto first dimension
```

## Key Design Patterns

1. **Composite Pattern** - Tree structure with uniform node interface
2. **Strategy Pattern** - Pluggable partition rules and fitness functions
3. **Visitor Pattern** - Tree traversal and operations
4. **Template Method** - Abstract base classes with concrete implementations
5. **Factory Pattern** - Model and rule generation

## Performance Considerations

- **Lazy Evaluation** - Volumes computed on demand
- **Caching** - Point iterators and volume calculations cached
- **Parallel Processing** - `PoolNodeVisitor` supports multi-threading
- **Memory Management** - Bounded point caches in `RuleVolume`

This library provides a sophisticated framework for learning and working with probability distributions in high-dimensional spaces, with particular strength in adaptive spatial partitioning and hierarchical modeling.
