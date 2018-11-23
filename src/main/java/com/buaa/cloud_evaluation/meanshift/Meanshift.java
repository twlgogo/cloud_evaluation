package com.buaa.cloud_evaluation.meanshift;

import com.buaa.cloud_evaluation.ahp.AHPCacluator;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.lang.Nullable;

public class Meanshift {

  private int dimension;
  List<Double> cis;
  private List<double[]> weights;
  private double[] minBound;
  private double[] maxBound;
  private double interval;
  private double radius;
  private double bias;
  private double diff;

  public static Meanshift newInstance(File file) throws FileNotFoundException {
    // FIXME hardcode dimension
    int dimension = 4;

    List<Double> cis = new ArrayList<>();
    List<double[]> weights = new ArrayList<>();

    Scanner scanner = new Scanner(new FileInputStream(file));
    try {
      for (;;) {
        double ci = scanner.nextDouble();
        double[] weight = new double[dimension];
        for (int i = 0; i < dimension; i++) {
          weight[i] = scanner.nextDouble();
        }
        cis.add(ci);
        weights.add(weight);
      }
    } catch (NoSuchElementException e) {
      // Ignore
    }

    return new Meanshift(dimension, cis, weights);
  }

  public Meanshift(
      int dimension,
      List<Double> cis,
      List<double[]> weights
  ) {
    this(
        dimension,
        cis,
        weights,
        new double[] {0, 0, 0, 0},
        new double[] {1, 1, 1, 1},
        0.1,
        0.1,
        0.01,
        0.2);
  }

  public Meanshift(
      int dimension,
      List<Double> cis,
      List<double[]> weights,
      double[] minBound,
      double[] maxBound,
      double interval,
      double radius,
      double bias,
      double diff
  ) {
    // Check dimension
    for (double[] weight : weights) {
      if (weight.length != dimension) {
        throw new IllegalStateException("Dimension is " + dimension + ", but it's " + weight.length);
      }
    }
    if (minBound.length != dimension) {
      throw new IllegalStateException("Dimension is " + dimension + ", but it's " + minBound.length);
    }
    if (maxBound.length != dimension) {
      throw new IllegalStateException("Dimension is " + dimension + ", but it's " + maxBound.length);
    }
    if (interval <= 0) {
      throw new IllegalStateException("interval <= 0");
    }
    if (radius <= 0) {
      throw new IllegalStateException("radius <= 0");
    }
    if (bias <= 0) {
      throw new IllegalStateException("bias <= 0");
    }
    if (diff <= 0) {
      throw new IllegalStateException("diff <= 0");
    }

    this.dimension = dimension;
    this.cis = cis;
    this.weights = weights;
    this.minBound = minBound;
    this.maxBound = maxBound;
    this.interval = interval;
    this.radius = radius;
    this.bias = bias;
    this.diff = diff;
  }

  public List<Result> meanshift() {
    List<double[]> samples = genSamples();
    List<double[]> results = new ArrayList<>();

    for (double[] sample : samples) {
      for (;;) {
        double[] oldSample = sample;
        sample = step(sample);
        if (sample == null || distance(oldSample, sample) < bias) {
          break;
        }
      }
      if (sample != null) {
        results.add(sample);
      }
    }

    List<double[]> clusterWeights = new ArrayList<>();
    for (double[] result : results) {
      for (int j = 0; j < clusterWeights.size(); j++) {
        double[] weight = clusterWeights.get(j);
        if (distance(result, weight) <= diff) {
          clusterWeights.set(j, average(result, weight));
          result = null;
          break;
        }
      }
      if (result != null) {
        clusterWeights.add(result);
      }
    }

    List<List<Integer>> indexs = new ArrayList<>();
    for (int i = 0; i < clusterWeights.size(); i++) {
      indexs.add(new ArrayList<>());
    }
    for (int i = 0; i < weights.size(); i++) {
      double[] weight = weights.get(i);
      int j = IntStream.range(0, clusterWeights.size())
          .reduce((a, b) -> distance(clusterWeights.get(a), weight) > distance(clusterWeights.get(b), weight) ? b : a)
          .getAsInt();
      indexs.get(j).add(i);
    }

    List<Result> results1 = new ArrayList<>();

    for (List<Integer> index : indexs) {
      List<List<Double>> requests = new ArrayList<>();
      for (int i : index) {
        List<Double> request = new ArrayList<>();
        request.add(cis.get(i));
        request.addAll(Arrays.stream(this.weights.get(i)).boxed().collect(Collectors.toList()));
        requests.add(request);
      }

      Result result = new Result();
      result.count = index.size();
      result.weight = AHPCacluator.fixedCommunityWeight(requests);
      results1.add(result);
    }

    return results1;
  }

  @Nullable
  private double[] step(double[] point) {
    double[] offset = new double[dimension];
    int count = 0;

    for (double[] vector : weights) {
      if (distance(vector, point) > radius) {
        continue;
      }
      count += 1;
      for (int j = 0; j < dimension; j++) {
        offset[j] += vector[j] - point[j];
      }
    }

    if (count == 0) {
      return null;
    }

    double[] result = Arrays.copyOf(point, point.length);
    for (int i = 0; i < dimension; i++) {
      result[i] += offset[i] / count;
    }

    return result;
  }

  private double[] average(double[] p1, double[] p2) {
    double[] result = new double[p1.length];
    for (int i = 0; i < p1.length; i++) {
      result[i] = (p1[i] + p2[i]) / 2;
    }
    return result;
  }

  private double distance(double[] p1, double[] p2) {
    double distance = 0;
    for (int i = 0; i < p1.length; i++) {
      distance += (p1[i] - p2[i]) * (p1[i] - p2[i]);
    }
    return Math.sqrt(distance);
  }

  private List<double[]> genSamples() {
    List<double[]> samples = new ArrayList<>();
    double[] sample = new double[dimension];
    appendSample(samples, sample, 0);
    return samples;
  }

  private void appendSample(List<double[]> samples, double[] sample, int index) {
    double min = minBound[index];
    double max = maxBound[index];
    int step = (int) Math.ceil((max - min) / interval) + 1;

    for (int i = 0; i < step; i++) {
      double value = min + interval * i;
      sample[index] = value;

      if (index + 1 == dimension) {
        samples.add(Arrays.copyOf(sample, sample.length));
      } else {
        appendSample(samples, sample, index + 1);
      }
    }
  }

  public static class Result {
    public int count;
    public List<Double> weight;
  }
}
