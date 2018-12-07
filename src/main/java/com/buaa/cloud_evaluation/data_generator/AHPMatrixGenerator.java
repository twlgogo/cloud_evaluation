package com.buaa.cloud_evaluation.data_generator;

import com.buaa.cloud_evaluation.ahp.AHPCacluator;
import com.buaa.cloud_evaluation.model.AHPRequest;
import com.buaa.cloud_evaluation.model.AHPResult;

import java.util.*;
import java.util.stream.Collectors;

public class AHPMatrixGenerator {
  public static AHPRequest []ahpRequests;
  public static double []theas = {0.1,0.3,0.5,0.7,0.9,1.0,1.3};

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

  public void adjustMatrix( int numofChangeItem) {
    for (int i = 1; i < ahpRequests.length; i++) {
      Random random = new Random(System.currentTimeMillis());
      double thea = theas[random.nextInt(theas.length)];
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
      List<Double> list = ahpRequests[i].getList();
      for (double d:list) {
        System.out.print((int)Math.ceil(d)+"\t");
      }
      System.out.println();
    }
  }

  public static void main(String[] args) {
    double []initMatrix = {2,4,1.3,2,0.6,0.3};
    double []initMatrix1 = {2,4,1,2,1,1};
    double []initMatrix2 = {2,4,1.5,2,1,0.5};
    double []initMatrix3 = {6,6,3,1,0.5,0.5};
    double []initMatrix4 = {0.6,3,3,5,5,1};
    double []initMatrix5 = {1,2,0.4,2,0.4,0.2};
    double []initMatrix6 = {4,2,1,0.5,0.3,0.6};
    double []initMatrix7 = {1.5,1.5,1,1,0.6,0.6};
    AHPMatrixGenerator generator = new AHPMatrixGenerator(100,initMatrix);
    generator.adjustMatrix(1);
    generator.printMatrix();
    AHPResult []results = new AHPResult[ahpRequests.length];
    for (int i = 0; i < ahpRequests.length; i++) {
      results[i] = AHPCacluator.getAHPResult(ahpRequests[i]);
      //if (!results[i].isFitCI()) continue;
      List<Double> list = results[i].getResList();
      //String[] arr = (String[])list.toArray(new String[list.size()]);
      for (double str:list) {
        System.out.print("\t"+str);
      }
      System.out.println();
    }
    List<AHPRequest> list = new ArrayList<>();
   // list = Collection
    for (int i = 0; i < ahpRequests.length; i++) {
      list.add(ahpRequests[i]);
    }
    //Arrays.stream(ahpRequests).boxed().collect(Collectors.toList());
    //AHPResult result = AHPCacluator.fixAHPWeight(list);
    //System.out.println(result.getResList());

  }
}
