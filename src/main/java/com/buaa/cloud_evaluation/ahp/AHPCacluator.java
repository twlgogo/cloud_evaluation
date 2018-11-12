package com.buaa.cloud_evaluation.ahp;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AHPCacluator {
  private static double[] RI = {0.00,0.00,0.58,0.90,1.12,1.32,1.41,1.45,1.49,1.52,1.54};
  private static double CR = 0.0;
  private static double lamta = 0.0;

  private static double CI = 1.0;

  public static AHPResult getAHPResult(AHPRequest ahpRequest) {
    AHPResult ahpResult = new AHPResult();
    if (ahpRequest == null) {
      ahpResult.setFitCI(false);
      return ahpResult;
    }
    int n = ahpRequest.getN() ;
    int checkSum = n*(n - 1) /2;
    if (checkSum != ahpRequest.getList().size()) {
      ahpResult.setFitCI(false);
      return ahpResult;
    }
    double[][] ahpMatrix = getAHPMatrix(ahpRequest.getList(),n);
    double[] weight = new double[n];
    getWeight(ahpMatrix,weight,n);
    List<Double> list = new ArrayList<>(weight.length);
    for (double w: weight) {
      list.add(w);
    }
    ahpResult.setResList(list);
    ahpResult.setFitCI(CI < 0.1);
    ahpResult.setCI(CI);
    return ahpResult;
  }

  public static AHPResult fixAHPWeight(List<AHPRequest> historyAHPRequests) {
    List<AHPResult> ahpResults = new ArrayList<>();
    double[] CIs = new double[historyAHPRequests.size()];
    int index = 0;
    for (AHPRequest ahp:historyAHPRequests) {
      AHPResult cur = getAHPResult(ahp);
      ahpResults.add(cur);
      CIs[index++] = 0.1 - cur.getCI();
    }
    Normalization.normal(CIs);
    int n = historyAHPRequests.get(0).getN();
    List<Double> fixedWeight = new ArrayList<>();
    for (int i = 0; i < n ; i++) {
      double cur = 0.0;
      for (int j = 0; j < ahpResults.size(); j++) {
        cur += ahpResults.get(j).getResList().get(i) * CIs[j];
      }
      fixedWeight.add(cur);
    }
    AHPResult result = new AHPResult();
    result.setFitCI(true);
    result.setN(n);
    result.setResList(fixedWeight);
    return result;
  }

  private static double[][] getAHPMatrix (List<Double> list, int N) {
    double[][] resMatirx = new double[N][N];
    int index = 0;
    for (int i = 0; i < N; i++) {
      for (int j = i + 1; j < N ; j++) {
        resMatirx[i][j] = list.get(index++);
      }
    }
    for (int i = 0; i < N; i++) {
      resMatirx[i][i] = 1;
    }

    for (int i = 0; i < N; i++) {
      for (int j = 0; j < i; j++) {
        resMatirx[i][j] = 1/resMatirx[j][i];
      }
    }
    return resMatirx;
  }

  private static double round(double v, int scale) {
    BigDecimal b = new BigDecimal(Double.toString(v));
    BigDecimal one = new BigDecimal("1");
    return b.divide(one,scale,BigDecimal.ROUND_HALF_UP).doubleValue();
  }


  private static void  getWeight(double[][] a, double[] weight, int N) {
    double[] w0 = new double[N];
    for (int i = 0; i < N; i++) {
      w0[i] = 1.0 / N;
    }

    double[] w1 = new double[N];
    double[] w2 = new double[N];
    double sum = 1.0;
    double d = 1.0;
    double delt = 0.00001;

    while (d > delt) {
      d = 0.0;
      sum = 0;
      int index = 0;
      for (int j = 0; j < N; j++) {
        double t = 0.0;
        for (int l = 0; l < N; l++)
          t += a[j][l] * w0[l];
        w1[j] = t;
        sum += w1[j];
      }

      for (int k = 0; k < N; k++) {
        w2[k] = w1[k] / sum;

        d = Math.max(Math.abs(w2[k] - w0[k]), d);

        w0[k] = w2[k];
      }
    }

    // 计算矩阵最大特征值lamta，CI，RI
    lamta = 0.0;
    for (int k = 0; k < N; k++) {
      lamta += w1[k] / (N * w0[k]);
    }
    CI = (lamta - N) / (N - 1);
    if (RI[N - 1] != 0) {
      CR = CI / RI[N - 1];
    }

    lamta = round(lamta, 3);
    CI = Math.abs(round(CI, 3));
    CR =  Math.abs(round(CR, 3));

    for (int i = 0; i < N; i++) {
      w0[i] = round(w0[i], 4);
      w1[i] = round(w1[i], 4);
      w2[i] = round(w2[i], 4);
    }

    System.out.println("lamta=" + lamta);
    System.out.println("CI=" + CI);
    System.out.println("CR=" + CR);

    for (int i = 0; i < N; i++) {
      weight[i] = w2[i];
    }
  }

  public static void main(String[] args) {
    AHPRequest ahpRequest = new AHPRequest();
    List<Double> list = new ArrayList<>();
    list.add(3.0);
    list.add(5.0);
    list.add(2.0);
    ahpRequest.setList(list);
    ahpRequest.setN(3);
    AHPResult ahpResult = getAHPResult(ahpRequest);
    List<Double> reslist = ahpResult.getResList();
    System.out.println(reslist);
  }

}
