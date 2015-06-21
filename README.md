# volumetry

## Background

Few methods currently exist for approximating a [joint probability distribution](https://en.wikipedia.org/wiki/Joint_probability_distribution) from a sample of data points. Popular methods are [kernel density estimation](https://en.wikipedia.org/wiki/Multivariate_kernel_density_estimation) and, slightly less directly, [gaussian mixture models](https://en.wikipedia.org/wiki/Mixture_model) or [clustering](https://en.wikipedia.org/wiki/Cluster_analysis).

## Introduction

This project explores a density estimation algorithm based on [space partitioning](https://en.wikipedia.org/wiki/Space_partitioning) and the sampled densities of the resulting partitions. The result is a structure similar in design and construction to a [decision tree](https://en.wikipedia.org/wiki/Decision_tree), with a modified entropy rule.

## Running it

Various test cases in [Demo.java](https://github.com/acharneski/volumetry/blob/master/src/test/java/com/simiacryptus/probabilityModel/Demo.java#L35) are designed to be simple to execute and point towards the most relevant parts of code. These test cases will each display a 3d scatterplot, described below:

### [testModelProject](https://github.com/acharneski/volumetry/blob/master/src/test/java/com/simiacryptus/probabilityModel/Demo.java#L45)
![Logistic](https://raw.githubusercontent.com/acharneski/volumetry/master/doc/projection.png)

### [testSurfaceFiller](https://github.com/acharneski/volumetry/blob/master/src/test/java/com/simiacryptus/probabilityModel/Demo.java#L91)
![Logistic](https://raw.githubusercontent.com/acharneski/volumetry/master/doc/surface.png)

### [test3dLogisticDistribution](https://github.com/acharneski/volumetry/blob/master/src/test/java/com/simiacryptus/probabilityModel/Demo.java#L130)
![Logistic](https://raw.githubusercontent.com/acharneski/volumetry/master/doc/logistic.png)


### [testSlicedModel](https://github.com/acharneski/volumetry/blob/master/src/test/java/com/simiacryptus/probabilityModel/Demo.java#L141)
![Logistic](https://raw.githubusercontent.com/acharneski/volumetry/master/doc/slices.png)

### [testVolumeEntropyModel](https://github.com/acharneski/volumetry/blob/master/src/test/java/com/simiacryptus/probabilityModel/Demo.java#L189)
![Logistic](https://raw.githubusercontent.com/acharneski/volumetry/master/doc/logistic_model.png)

