# Probability Model Framework - Code Tour and User Guide

## Overview

This is a comprehensive Java framework for probabilistic modeling, density estimation, and statistical analysis. The framework provides tools for building tree-based probability models, performing model operations, and benchmarking different modeling approaches.

## Core Architecture

### Base Interfaces and Abstractions

**[DataDistribution](src/test/java/com/simiacryptus/probabilityModel/benchmark/base/DataDistribution.java)** - Core interface defining probability distributions with density evaluation and volume operations.

**[DataSampler](src/test/java/com/simiacryptus/probabilityModel/benchmark/base/DataSampler.java)** - Interface for generating sample points from distributions.

**[DataLearner](src/test/java/com/simiacryptus/probabilityModel/benchmark/base/DataLearner.java)** - Extends both DataSampler and DataDistribution to provide learning capabilities from training data.

**[TestObject](src/test/java/com/simiacryptus/probabilityModel/benchmark/base/TestObject.java)** - Base class providing common functionality for test objects with naming and string representation.

## Model Implementations

### Tree-Based Modeling

**[TreeModeler](src/test/java/com/simiacryptus/probabilityModel/benchmark/impl/TreeModeler.java)** - Primary implementation using hierarchical tree partitioning for probability modeling. Uses ModelPartitioner to recursively split the space based on data density.

**[KDTreeModeler](src/test/java/com/simiacryptus/probabilityModel/benchmark/impl/KDTreeModeler.java)** - K-dimensional tree implementation for nearest-neighbor density estimation. Calculates density based on distance to k-nearest neighbors.

### Sampling Implementations

**[SieveSampler](src/test/java/com/simiacryptus/probabilityModel/benchmark/impl/SieveSampler.java)** - Rejection sampling implementation that uses accept/reject based on density ratios.

**[LibDataSampler](src/test/java/com/simiacryptus/probabilityModel/benchmark/impl/LibDataSampler.java)** - Wrapper for external distribution libraries, filtering samples to fit within specified volumes.

**[LibDataDistributionSampler](src/test/java/com/simiacryptus/probabilityModel/benchmark/impl/LibDataDistributionSampler.java)** - Combined sampler and distribution wrapper for library distributions.

**[PointModelSampler](src/test/java/com/simiacryptus/probabilityModel/benchmark/impl/PointModelSampler.java)** - Advanced sampler that learns density models iteratively using exploration and exploitation strategies.

### Utility Wrappers

**[DataSamplerDistributionWrapper](src/test/java/com/simiacryptus/probabilityModel/benchmark/impl/DataSamplerDistributionWrapper.java)** - Adapts DataSampler interface to work with Distribution interface.

## Statistical Analysis Tools

### Comparison Metrics

**[DensityComparison](src/test/java/com/simiacryptus/probabilityModel/benchmark/statistics/DensityComparison.java)** - Compares probability density functions using multiple normalization schemes and distance metrics.

**[SampleDistanceComparison](src/test/java/com/simiacryptus/probabilityModel/benchmark/statistics/SampleDistanceComparison.java)** - Measures distances between sample sets using optimal matching algorithms.

**[SamplerDistributionEncodingEntropy](src/test/java/com/simiacryptus/probabilityModel/benchmark/statistics/SamplerDistributionEncodingEntropy.java)** - Calculates encoding entropy for measuring model quality and information content.

## Utility Classes

### Mathematical Operations

**[DoubleArray](src/test/java/com/simiacryptus/probabilityModel/benchmark/util/DoubleArray.java)** - Dynamic array with built-in statistical calculations (mean, standard deviation, normalization).

**[DoubleArrayMath](src/test/java/com/simiacryptus/probabilityModel/benchmark/util/DoubleArrayMath.java)** - Extended mathematical operations on double arrays including element-wise operations and transformations.

**[Distribution1d](src/test/java/com/simiacryptus/probabilityModel/benchmark/util/Distribution1d.java)** - One-dimensional distribution analysis with sorting and quantile operations.

**[Timer](src/test/java/com/simiacryptus/probabilityModel/benchmark/util/Timer.java)** - Performance timing utility for benchmarking operations.

## Benchmarking Framework

### Core Benchmarking

**[Benchmark](src/test/java/com/simiacryptus/probabilityModel/benchmark/Benchmark.java)** - Base class for running comprehensive model training and evaluation experiments.

**[CrossProductBenchmark](src/test/java/com/simiacryptus/probabilityModel/benchmark/CrossProductBenchmark.java)** - Abstract base for running cross-product experiments across multiple test objects.

### Specific Benchmark Implementations

**[BenchmarkSummaryReport](src/test/java/com/simiacryptus/probabilityModel/benchmark/BenchmarkSummaryReport.java)** - Comprehensive benchmark comparing multiple modeling approaches on standard test distributions.

**[ExperimentalBenchmark](src/test/java/com/simiacryptus/probabilityModel/benchmark/ExperimentalBenchmark.java)** - Experimental benchmark testing novel entropy-based fitness functions.

## Test Distributions

### Distribution Implementations

**[DistributionBase](src/test/java/com/simiacryptus/probabilityModel/distributions/DistributionBase.java)** - Abstract base class providing common distribution functionality.

**[DistributionAdapter](src/test/java/com/simiacryptus/probabilityModel/distributions/DistributionAdapter.java)** - Adapts Apache Commons Math distributions to framework interface.

**[MVWrapper](src/test/java/com/simiacryptus/probabilityModel/distributions/MVWrapper.java)** - Wrapper for multivariate distributions.

### Specific Distributions

**[Test3dDistributions](src/test/java/com/simiacryptus/probabilityModel/distributions/Test3dDistributions.java)** - Enumeration of standard 3D test distributions including Gaussian mixtures, snake distributions, and logistic maps.

**[SnakeDistribution](src/test/java/com/simiacryptus/probabilityModel/distributions/SnakeDistribution.java)** - Parametric curve distribution with Gaussian noise for testing non-linear modeling.

**[LogisticDistribution](src/test/java/com/simiacryptus/probabilityModel/distributions/LogisticDistribution.java)** - Chaotic logistic map distribution for testing complex dynamics.

**[SampledDistribution](src/test/java/com/simiacryptus/probabilityModel/distributions/SampledDistribution.java)** - Distribution based on discrete sample points.

### Distribution Utilities

**[DistributionUtil](src/test/java/com/simiacryptus/probabilityModel/distributions/DistributionUtil.java)** - Utility functions for creating random Gaussian distributions.

**[RemappedDistribution](src/test/java/com/simiacryptus/probabilityModel/distributions/RemappedDistribution.java)** - Applies coordinate transformations to existing distributions.

**[VolumeSieveDistributionFactory](src/test/java/com/simiacryptus/probabilityModel/distributions/VolumeSieveDistributionFactory.java)** - Constrains distributions to specific spatial volumes.

## Testing and Validation

### Unit Tests

**[LinearBoundingVolumeTest](src/test/java/com/simiacryptus/probabilityModel/unit/LinearBoundingVolumeTest.java)** - Tests linear bounding volume operations including slicing and sampling.

### Integration Tests

**[Demo](src/test/java/com/simiacryptus/probabilityModel/Demo.java)** - Demonstration tests showcasing main framework capabilities including model projection, surface filling, and slicing operations.

**[ModelOperationsDevTest](src/test/java/com/simiacryptus/probabilityModel/ModelOperationsDevTest.java)** - Development tests for model operations including entropy-based modeling, KD-tree experiments, and model arithmetic.

**[TestBase](src/test/java/com/simiacryptus/probabilityModel/TestBase.java)** - Base class for tests providing common setup and visualization utilities.

## Utilities and Support

**[TestUtil](src/test/java/com/simiacryptus/util/TestUtil.java)** - Comprehensive testing utilities including 3D visualization, density comparison, and statistical analysis tools.

**[HtmlOutput](src/test/java/com/simiacryptus/util/HtmlOutput.java)** - HTML report generation for test results and benchmarks.

## User Guide

### Getting Started

1. **Basic Modeling**: Start with [TreeModeler](src/test/java/com/simiacryptus/probabilityModel/benchmark/impl/TreeModeler.java) for general-purpose probability modeling
2. **Benchmarking**: Use [BenchmarkSummaryReport](src/test/java/com/simiacryptus/probabilityModel/benchmark/BenchmarkSummaryReport.java) to compare different approaches
3. **Custom Distributions**: Extend [DistributionBase](src/test/java/com/simiacryptus/probabilityModel/distributions/DistributionBase.java) for custom probability distributions

### Common Workflows

1. **Model Training**: Create a model → Add training data → Apply partitioning → Evaluate performance
2. **Model Comparison**: Use statistical comparison tools to evaluate model quality
3. **Visualization**: Leverage 3D plotting utilities for visual analysis

### Key Features

- **Hierarchical Modeling**: Tree-based space partitioning for complex distributions
- **Multiple Sampling Methods**: Rejection sampling, nearest-neighbor, and model-based sampling
- **Comprehensive Benchmarking**: Statistical comparison tools and performance metrics
- **3D Visualization**: Built-in plotting for visual analysis
- **Extensible Architecture**: Clean interfaces for adding new models and distributions

The framework is designed for researchers and practitioners working with probability modeling, density estimation, and statistical learning problems.