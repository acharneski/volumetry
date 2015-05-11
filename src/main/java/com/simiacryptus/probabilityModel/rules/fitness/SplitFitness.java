package com.simiacryptus.probabilityModel.rules.fitness;

import java.util.List;

import com.simiacryptus.probabilityModel.model.PointNode;

public interface SplitFitness
{

  public abstract double getFitness(PointNode node, List<VolumeDataDensity> list);

}