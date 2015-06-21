# volumetry

## Background

Few methods currently exist for approximating a [joint probability distribution](https://en.wikipedia.org/wiki/Joint_probability_distribution) from a sample of data points. Popular methods are [kernel density estimation](https://en.wikipedia.org/wiki/Multivariate_kernel_density_estimation) and, slightly less directly, [gaussian mixture models](https://en.wikipedia.org/wiki/Mixture_model) or [clustering](https://en.wikipedia.org/wiki/Cluster_analysis).

## Introduction

This project explores a density estimation algorithm based on [space partitioning](https://en.wikipedia.org/wiki/Space_partitioning) and the sampled densities of the resulting partitions. The result is a structure similar in design and construction to a [decision tree](https://en.wikipedia.org/wiki/Decision_tree), with a modified entropy rule.

## Running it

Various test cases in [Demo.java](https://github.com/acharneski/volumetry/blob/master/src/test/java/com/simiacryptus/probabilityModel/Demo.java#L35) are designed to be simple to execute and point towards the most relevant parts of code. These test cases will each display a 3d scatterplot, described below:

### [testModelProject](https://github.com/acharneski/volumetry/blob/master/src/test/java/com/simiacryptus/probabilityModel/Demo.java#L45)
Displays a modeled "snake" distribution, which is based on a gaussian kernel extruded along a random spline. Demonstrates both slicing and projection operations, where a multidimensional model is projected, or flattened, along some axis to obtain a [marginal density](https://en.wikipedia.org/wiki/Marginal_distribution)
![Logistic](https://raw.githubusercontent.com/acharneski/volumetry/master/doc/projection.png)

### [testSlicedModel](https://github.com/acharneski/volumetry/blob/master/src/test/java/com/simiacryptus/probabilityModel/Demo.java#L141)
A similar demo, but also demonstrating slices based on range constraints (e.g. x>=5 as opposed to x=5)
![Logistic](https://raw.githubusercontent.com/acharneski/volumetry/master/doc/slices.png)

### [test3dLogisticDistribution](https://github.com/acharneski/volumetry/blob/master/src/test/java/com/simiacryptus/probabilityModel/Demo.java#L130)
This displays the ["3d logistic map"](https://github.com/acharneski/volumetry/blob/master/src/test/java/com/simiacryptus/probabilityModel/distributions/LogisticDistribution.java#L32) testing distribution. This is a particularly good benchmarking distribution since is contains a wide variety of geometric features.
![Logistic](https://raw.githubusercontent.com/acharneski/volumetry/master/doc/logistic.png)

### [testVolumeEntropyModel](https://github.com/acharneski/volumetry/blob/master/src/test/java/com/simiacryptus/probabilityModel/Demo.java#L189)
This demonstrates the effectiveness of this model-building technique applied to the "3d logistic map".
![Logistic](https://raw.githubusercontent.com/acharneski/volumetry/master/doc/logistic_model.png)

### [testSurfaceFiller](https://github.com/acharneski/volumetry/blob/master/src/test/java/com/simiacryptus/probabilityModel/Demo.java#L91)
This demonstrates a unique application of the modeling technique, wherin we wish to find an equipotential surface. This is different from most optimization and numerical solver techniques since it not only has to converge on the solution, it has to fill and uniformly sample all applicable space as well.
![Logistic](https://raw.githubusercontent.com/acharneski/volumetry/master/doc/surface.png)
