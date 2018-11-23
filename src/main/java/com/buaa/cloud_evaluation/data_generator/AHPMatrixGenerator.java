package com.buaa.cloud_evaluation.data_generator;

import com.buaa.cloud_evaluation.ahp.AHPCacluator;
import com.buaa.cloud_evaluation.model.AHPRequest;
import com.buaa.cloud_evaluation.model.AHPResult;

import java.util.*;
import java.util.stream.Collectors;

public class AHPMatrixGenerator {
  public static AHPRequest []ahpRequests;

  public AHPMatrixGenerator(int size, double initMatrix[]) {
    ahpRequests = new AHPRequest[size];
    for (int i = 0; i < size; i++) {
      ahpRequests[i] = new AHPRequest();
      ahpRequests[i].setN(4);



      List<Double> list =
          Arrays.stream(initMatrix).boxed().collect(Collectors.toList());
      ahpRequests[i].setList(list);
    }
  }

  public void adjustMatrix(double thea, int numofChangeItem) {
    for (int i = 1; i < ahpRequests.length; i++) {
      Random random = new Random(System.currentTimeMillis());
      try {
        Thread.sleep(50);
      }catch (Exception e){
        e.printStackTrace();
      }
      int n = ahpRequests[i].getN();
      for (int j = numofChangeItem; j > 0; j--) {
        int index2Change = random.nextInt(n);
        double item = ahpRequests[i].getList().get(index2Change);
        if (random.nextInt(2) == 1){
          item += thea;
        } else {
          item -= thea;
        }
        ahpRequests[i].getList().set(index2Change, item);
      }
    }
  }

  public void printMatrix() {
    for (int i = 0; i < ahpRequests.length; i++) {
      System.out.println(ahpRequests[i].getList());
    }
  }

  public static void main(String[] args) {
    double []initMatrix = {2,4,1.3,2,0.6,0.3};
    double []initMatrix2 = {2,4,1,2,1,1};
    AHPMatrixGenerator generator = new AHPMatrixGenerator(37,initMatrix2);
    generator.adjustMatrix(1,1);
    generator.printMatrix();
    AHPResult []results = new AHPResult[ahpRequests.length];
    for (int i = 0; i < ahpRequests.length; i++) {
      results[i] = AHPCacluator.getAHPResult(ahpRequests[i]);
      System.out.println(results[i].getResList());
    }
    List<AHPRequest> list = new ArrayList<>();
   // list = Collection
    for (int i = 0; i < ahpRequests.length; i++) {
      list.add(ahpRequests[i]);
    }
    //Arrays.stream(ahpRequests).boxed().collect(Collectors.toList());
    AHPResult result = AHPCacluator.fixAHPWeight(list);
    System.out.println(result.getResList());

  }
}
