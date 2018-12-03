package com.buaa.cloud_evaluation.tool;

import com.buaa.cloud_evaluation.ahp.AHPCacluator;
import com.buaa.cloud_evaluation.ahp.DeviationCaculator;
import com.buaa.cloud_evaluation.meanshift.Meanshift;
import com.buaa.cloud_evaluation.model.AHPRequest;
import com.buaa.cloud_evaluation.model.AHPResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.stream.Collectors;

public class VectorImporter {

  public static void main(String[] args) throws IOException {
    importVectors(
        new File("vectors.txt"),
        new File("ahp_results.txt"),
        new File("meanshift.txt"),
        "meanshift_group_%d.txt",
        "meanshift_group_progress_%d.txt",
        "meanshift_group_standard_diviation_%d.txt",
        new File("meanshift_result.txt"),
        new File("meanshift_final_result.txt")
    );


//    importAHPResults(
//        new File("ahp_results.txt"),
//        new File("meanshift.txt"),
//        "meanshift_group_%d.txt",
//        "meanshift_group_progress_%d.txt",
//        "meanshift_group_standard_diviation_%d.txt",
//        new File("meanshift_result.txt"),
//        new File("meanshift_final_result.txt"));
  }

  private static List<AHPRequest> readVectorsFile(File file) throws FileNotFoundException {
    Scanner scanner = new Scanner(new FileInputStream(file));

    List<AHPRequest> requests = new ArrayList<>();

    try {
      for (;;) {
        double v1 = scanner.nextDouble();
        double v2 = scanner.nextDouble();
        double v3 = scanner.nextDouble();
        double v4 = scanner.nextDouble();
        double v5 = scanner.nextDouble();
        double v6 = scanner.nextDouble();

        AHPRequest request = new AHPRequest();
        request.setN(4);
        request.setList(Arrays.asList(v1, v2, v3, v4, v5, v6));

        requests.add(request);
      }
    } catch (NoSuchElementException e) {
      // Ignore
    }

    if (requests.isEmpty()) {
      throw new IllegalStateException("No vector");
    }

    return requests;
  }

  private static void writeAHPResults(File file, List<AHPResult> results) throws IOException {
    try (Writer writer = new FileWriter(file)) {
      for (AHPResult result : results) {
        writer.write(Double.toString(result.getCI()));
        for (Double d : result.getResList()) {
          writer.write("\t");
          writer.write(Double.toString(d));
        }
        writer.write("\n");
      }
    }
  }

  private static void writeMeanshiftResults(
      File file,
      String groupFilename,
      List<Meanshift.Result> results
  ) throws IOException {

    int index = 0;

    try (Writer writer = new FileWriter(file)) {
      for (Meanshift.Result result : results) {
        for (Double d : result.center) {
          writer.write(Double.toString(d));
          writer.write("\t");
        }
        writer.write(Integer.toString(result.count));
        writer.write("\n");

        File groupFile = new File(String.format(groupFilename, index++));
        try (Writer groupWriter = new FileWriter(groupFile)) {
          for (List<Double> ds : result.elements) {
            for (Double d : ds) {
              groupWriter.write(Double.toString(d));
              groupWriter.write("\t");
            }
            groupWriter.write("\n");
          }
        }
      }
    }
  }

  private static void writeDoubleList(Writer writer, List<Double> ds) throws IOException {
    writer.write(Double.toString(ds.get(0)));
    for (int i = 1; i < ds.size(); i++) {
      writer.write("\t");
      writer.write(Double.toString(ds.get(i)));
    }
  }

  private static void writeDoubleArray(Writer writer, double[] ds) throws IOException {
    writer.write(Double.toString(ds[0]));
    for (int i = 1; i < ds.length; i++) {
      writer.write("\t");
      writer.write(Double.toString(ds[i]));
    }
  }

  private static void writeMeanshiftProgress(
      String groupProgressFilename,
      String groupStandardDiviationFilename,
      List<Meanshift.Result> results
  ) throws IOException {
    for (int i = 0; i < results.size(); i++) {
      Meanshift.Result result = results.get(i);
      List<List<Double>> progress = new ArrayList<>();

      File groupFile = new File(String.format(groupProgressFilename, i));
      try (Writer groupWriter = new FileWriter(groupFile)) {
        for (int j = 0; j < result.elements.size(); j++) {
          List<Double> weight = AHPCacluator.fixedCommunityWeight(result.elements.subList(0, j + 1));
          progress.add(weight);

          writeDoubleList(groupWriter, weight);
          groupWriter.write("\n");
        }
      }

      File sdFile = new File(String.format(groupStandardDiviationFilename, i));
      try (Writer sdWriter = new FileWriter(sdFile)) {
        for (int j = 0; j < result.elements.size(); j++) {
          List<List<Double>> temp = progress.subList(0, j + 1);
          sdWriter.write(Double.toString(DeviationCaculator.StandardDiviation(temp.stream().map(x -> x.get(0)).collect(Collectors.toList()))));
          sdWriter.write("\t");
          sdWriter.write(Double.toString(DeviationCaculator.StandardDiviation(temp.stream().map(x -> x.get(1)).collect(Collectors.toList()))));
          sdWriter.write("\t");
          sdWriter.write(Double.toString(DeviationCaculator.StandardDiviation(temp.stream().map(x -> x.get(2)).collect(Collectors.toList()))));
          sdWriter.write("\t");
          sdWriter.write(Double.toString(DeviationCaculator.StandardDiviation(temp.stream().map(x -> x.get(3)).collect(Collectors.toList()))));
          sdWriter.write("\n");
        }
      }
    }
  }

  private static void writeMeanshiftResult(
      File file,
      List<Meanshift.Result> results
  ) throws IOException {
    try (Writer writer = new FileWriter(file)) {
      for (Meanshift.Result result : results) {
        writeDoubleList(writer, result.weight);
        writer.write("\n");
      }
    }
  }

  private static void writeMeanshiftFinalResult(
      File file,
      List<Meanshift.Result> results
  ) throws IOException {
    int atLeastCount = results.stream().max(Comparator.comparingInt(i -> i.count)).get().count / 3;
    List<Meanshift.Result> invalidResults = results.stream().filter(x -> x.count > atLeastCount).collect(Collectors.toList());

    int maxCount = invalidResults.stream().mapToInt(x -> x.count).sum();
    double[] fixResult = new double[invalidResults.get(0).weight.size()];
    for (Meanshift.Result invalidResult : invalidResults) {
      for (int i = 0; i < invalidResult.weight.size(); i++) {
        fixResult[i] += invalidResult.weight.get(i) * invalidResult.count / maxCount;
      }
    }

    try (Writer writer = new FileWriter(file)) {
      writeDoubleArray(writer, fixResult);
    }
  }

  private static void importVectors(
      File vectorsFile,
      File ahpResultsFile,
      File meanshiftFile,
      String meanshiftGroupFilename,
      String meanshiftGroupProgressFilename,
      String meanshiftGroupStandardDiviationFilename,
      File meanshiftGroupResultFile,
      File meanshiftFinalResultFile
  ) throws IOException {
    List<AHPRequest> requests = readVectorsFile(vectorsFile);

    List<AHPResult> results = requests.stream().map(AHPCacluator::getAHPResult).collect(Collectors.toList());
    writeAHPResults(ahpResultsFile, results);

    List<Meanshift.Result> meanshiftResults = Meanshift.newInstance(requests).meanshift();
    writeMeanshiftResults(meanshiftFile, meanshiftGroupFilename, meanshiftResults);
    writeMeanshiftProgress(meanshiftGroupProgressFilename, meanshiftGroupStandardDiviationFilename, meanshiftResults);
    writeMeanshiftResult(meanshiftGroupResultFile, meanshiftResults);
    writeMeanshiftFinalResult(meanshiftFinalResultFile, meanshiftResults);
  }

  private static List<AHPResult> readAHPResultsFile(File file) throws FileNotFoundException {
    Scanner scanner = new Scanner(new FileInputStream(file));
    List<AHPResult> results = new ArrayList<>();

    try {
      for (;;) {
        double ci = scanner.nextDouble();
        double v1 = scanner.nextDouble();
        double v2 = scanner.nextDouble();
        double v3 = scanner.nextDouble();
        double v4 = scanner.nextDouble();

        AHPResult result = new AHPResult();
        result.setN(4);
        result.setFitCI(true);
        result.setResList(Arrays.asList(v1, v2, v3, v4));
        result.setCI(ci);

        results.add(result);
      }
    } catch (NoSuchElementException e) {
      // Ignore
    }

    if (results.isEmpty()) {
      throw new IllegalStateException("No AHPResult");
    }

    return results;
  }


  private static void importAHPResults(
      File ahpResultsFile,
      File meanshiftFile,
      String meanshiftGroupFilename,
      String meanshiftGroupProgressFilename,
      String meanshiftGroupStandardDiviationFilename,
      File meanshiftGroupResultFile,
      File meanshiftFinalResultFile
  ) throws IOException {
    List<AHPResult> results = readAHPResultsFile(ahpResultsFile);

    List<Meanshift.Result> meanshiftResults = Meanshift.newInstance2(results).meanshift();
    writeMeanshiftResults(meanshiftFile, meanshiftGroupFilename, meanshiftResults);
    writeMeanshiftProgress(meanshiftGroupProgressFilename, meanshiftGroupStandardDiviationFilename, meanshiftResults);
    writeMeanshiftResult(meanshiftGroupResultFile, meanshiftResults);
    writeMeanshiftFinalResult(meanshiftFinalResultFile, meanshiftResults);
  }
}
