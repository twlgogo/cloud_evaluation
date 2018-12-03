package com.buaa.cloud_evaluation.ahp;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class DeviationCaculator {

    //方差s^2=[(x1-x)^2 +...(xn-x)^2]/n
    public static double Variance(double[] x) {
        int m = x.length;
        double sum = 0;
        for (int i = 0; i < m; i++) {//求和
            sum += x[i];
        }
        double dAve = sum / m;//求平均值
        double dVar = 0;
        for (int i = 0; i < m; i++) {//求方差
            dVar += (x[i] - dAve) * (x[i] - dAve);
        }
        return dVar / m;
    }

    //标准差σ=sqrt(s^2)
    public static double StandardDiviation(double[] x) {
        int m = x.length;
        double sum = 0;
        for (int i = 0; i < m; i++) {//求和
            sum += x[i];
        }
        double dAve = sum / m;//求平均值
        double dVar = 0;
        for (int i = 0; i < m; i++) {//求方差
            dVar += (x[i] - dAve) * (x[i] - dAve);
        }
        return Math.sqrt(dVar / m);
    }

    public static void main(String[] args) {
        Double[] data;
        try {
            File file = new File("/Users/tanweiliang/Desktop/qunluo1Var.txt");
            Scanner sc = new Scanner(file);
            List<Double> list = new LinkedList<>();
            while (sc.hasNextLine()) {
                list.add(Double.parseDouble(sc.nextLine()));
            }
            data = new Double[list.size()];
            for (int i = 0; i < data.length; i++) {
                data[i] = list.get(i);
            }
            for (int i = 1; i <= data.length; i++) {
                double[] request = new double[i];
                for (int j = 0; j < i; j++) {
                    request[j] = data[j];
                }

                System.out.println(StandardDiviation(request));
                //request = (double[])Arrays.copyOf(data,i);
                //System.out.println(Arrays.toString(request));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
