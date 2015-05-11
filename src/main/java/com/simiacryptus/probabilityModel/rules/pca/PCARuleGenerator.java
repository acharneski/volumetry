package com.simiacryptus.probabilityModel.rules.pca;

import java.util.ArrayList;
import java.util.Collection;



import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.stat.correlation.Covariance;

import com.simiacryptus.probabilityModel.model.PointNode;
import com.simiacryptus.probabilityModel.rules.MetricRuleGenerator;
import com.simiacryptus.probabilityModel.rules.PartitionRule;
import com.simiacryptus.probabilityModel.rules.fitness.SplitFitness;
import com.simiacryptus.probabilityModel.rules.metrics.Metric;
import com.simiacryptus.probabilityModel.volume.SpacialVolume;

public class PCARuleGenerator extends MetricRuleGenerator
{

  public PCARuleGenerator(SplitFitness splitFitness)
  {
    super(splitFitness);
  }

  @Override
  public Collection<Metric> getCandidateMetrics(PointNode node)
  {
    final ArrayList<Metric> list = new ArrayList<Metric>();
    list.addAll(super.getCandidateMetrics(node));
    final double[][] dataPoints = node.getDataPoints().toArray(new double[][]{});
    if(dataPoints.length < 10) return list;
    final RealVector centroid;
    {
      RealVector sum = null;
      for(int i=0;i<dataPoints.length;i++)
      {
        final ArrayRealVector v = new ArrayRealVector(dataPoints[i]);
        sum = (null == sum)?v:sum.add(v);
      }
      centroid = sum.mapDivide(dataPoints.length);
    }
    final RealMatrix dataMatrix = MatrixUtils.createRealMatrix(dataPoints.length, centroid.getDimension());
    for(int i=0;i<dataPoints.length;i++)
    {
      final ArrayRealVector v = new ArrayRealVector(dataPoints[i]);
      dataMatrix.setRowVector(i, v.subtract(centroid));
    }
    final Covariance covariance = new Covariance(dataMatrix, false);
    final EigenDecomposition eigenDecomposition = new EigenDecomposition(covariance.getCovarianceMatrix());
    final double[] eigenvalues = eigenDecomposition.getRealEigenvalues();
    for(int i=0;i<eigenvalues.length;i++)
    {
      final RealVector eigenvector = eigenDecomposition.getEigenvector(i);
      final double eigenvalue = eigenvalues[i];
      list.add(new LinearMetric(eigenvector, eigenvalue, centroid));
    }
    return list;
  }

  @Override
  protected PartitionRule getRule(final PointNode node, final Metric metric, final double[] metricValue)
  {
    return new LinearMetricRule(node.getUnboundableRegion(), metric, metricValue);
  }

  
}
