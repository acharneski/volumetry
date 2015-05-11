package com.simiacryptus.probabilityModel.visitors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.simiacryptus.data.VolumeMetric;
import com.simiacryptus.probabilityModel.model.DistributionModel;
import com.simiacryptus.probabilityModel.model.NodeBase;
import com.simiacryptus.probabilityModel.model.PointNode;

public final class JsonConverter<T extends NodeBase<T>> extends NodeVisitor<JsonConverter<T>, T>
{
  public static <T extends NodeBase<T>> JSONObject toJson(final DistributionModel<T> model, final int maxLevels) throws JSONException
  {
    final JsonConverter<T> visit = new JsonConverter<T>().setPointSample(1).visit(model, maxLevels);
    final JSONObject jsonObject = visit.jsonCache.get(model.getRoot());
    if(null == jsonObject)
    {
      throw new RuntimeException();
    }
    return jsonObject;
  }
  
  public static <T extends NodeBase<T>> JSONObject toJson(final T node, final int maxLevels) throws JSONException
  {
    return new JsonConverter<T>().visit(node, maxLevels).jsonCache.get(node);
  }
  
  public Map<T, JSONObject> jsonCache   = new HashMap<T, JSONObject>();
  
  private int               pointSample = 0;
  
  protected JSONObject getCountInfo(final T node) throws JSONException
  {
    final T root = node.getRoot();
    final JSONObject countJson = new JSONObject();
    final double dataFraction = node.getWeight() / root.getWeight();
    final double volumeFraction = node.getVolume().divide(root.getVolume()).value;
    countJson.put("data", node.getWeight());
    if(!Double.isInfinite(dataFraction) && !Double.isNaN(dataFraction)) countJson.put("data %", 100. * dataFraction);
    countJson.put("volume", node.getVolume());
    if(node instanceof PointNode)
    {
      countJson.put("volume", ((PointNode)node).getUnboundableRegion());
    }
    if (!Double.isNaN(volumeFraction) && !Double.isInfinite(volumeFraction))
    {
      countJson.put("volume %", 100. * volumeFraction);
    }
    return countJson;
  }
  
  protected JSONObject getNodeJson(final T node) throws JSONException
  {
    final JSONObject json = new JSONObject();
    //assert node.verifyStructure();
    json.put("path", node.getPath());
    json.put("count", this.getCountInfo(node));
    json.put("volume", node.getRegion().toJson());
    json.put("stats", this.getStatsInfo(node));
    if (null != node.getRule())
    {
      final JSONObject ruleJson = node.getRule().toJson();
      ruleJson.put("entropy", this.getSplitEntropy(node));
      json.put("rule", ruleJson);
    }
    if (0 < this.pointSample && node instanceof PointNode)
    {
      json.put("points", this.getPoints((PointNode) node));
    }
    final JSONArray childrenJson = new JSONArray();
    for (final T child : node.getChildren())
    {
      childrenJson.put(this.jsonCache.get(child));
    }
    json.put("children", childrenJson);
    return json;
  }
  
  private JSONArray getPoints(final PointNode node)
  {
    final JSONArray json = new JSONArray();
    int count = 0;
    final ArrayList<double[]> copy = new ArrayList<double[]>(node.getDataPoints());
    Collections.shuffle(copy);
    for (final double[] point : copy)
    {
      if (count++ > this.pointSample)
      {
        break;
      }
      json.put(String.format("%s", Arrays.toString(point)));
    }
    return json;
  }
  
  protected double getSplitEntropy(final NodeBase<T> node)
  {
    final VolumeMetric sumVolume = node.getVolume();
    final double sumProb = node.getWeight();
    final double log2 = Math.log(2);
    double entropy = 0;
    for (final T c : node.getChildren())
    {
      if(null == c) continue;
      final double dataFraction = c.getWeight() / sumProb;
      final double volumeFraction = c.getVolume().divide(sumVolume).value;
      entropy += dataFraction * Math.log(volumeFraction) / log2;
    }
    return entropy;
  }
  
  protected JSONObject getStatsInfo(final T node) throws JSONException
  {
    final JSONObject statsJson = new JSONObject();
    if (null != node.getRule())
    {
      final Double totalEntropy = this.getTotalEntropy(node);
      if (!Double.isNaN(totalEntropy) && !Double.isInfinite(totalEntropy))
      {
        statsJson.put("totalEntropy", totalEntropy);
      }
    }
    statsJson.put("nodes", node.getNodeCount());
    return statsJson;
  }
  
  protected Double getTotalEntropy(final NodeBase<T> node)
  {
    if (null == node.getRule())
    {
      return null;
    }
    double value = this.getSplitEntropy(node);
    for (final T child : node.getChildren())
    {
      if(null == child) continue;
      final Double childEntropy = this.getTotalEntropy(child);
      if (null != childEntropy)
      {
        final double childProbability = child.getWeight() / node.getWeight();
        value += childEntropy * childProbability;
      }
    }
    return value;
  }
  
  public int isShowPoints()
  {
    return this.pointSample;
  }
  
  public JsonConverter<T> setPointSample(final int pointSample)
  {
    this.pointSample = pointSample;
    return this;
  }
  
  @Override
  public void visitBegin(final T node)
  {
  }
  
  @Override
  public void visitEnd(final T node)
  {
    try
    {
      this.jsonCache.put(node, this.getNodeJson(node));
    }
    catch (final JSONException e)
    {
      throw new RuntimeException(e);
    }
  }
}