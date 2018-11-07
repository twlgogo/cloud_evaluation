package com.buaa.cloud_evaluation.model;

import com.buaa.cloud_evaluation.util.Serialization;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import lombok.Data;

@Data
public class NodeValueModel {
  private int id;
  private int n;
  @JsonIgnore
  private String matrixStr;
  @JsonIgnore
  private String vectorStr;

  private List<Integer> matrix;
  private List<Double> vector;

  public void setMatrixStr(String matrixStr) {
    this.matrixStr = matrixStr;
    this.matrix = matrixStr != null ? Serialization.stringToIntList(matrixStr) : null;
  }

  public void setVectorStr(String vectorStr) {
    this.vectorStr = vectorStr;
    this.vector = vectorStr != null ? Serialization.stringToDoubleList(vectorStr) : null;
  }

  public void setMatrix(List<Integer> matrix) {
    this.matrix = matrix;
    this.matrixStr = matrix != null ? Serialization.intListToString(matrix) : null;
  }

  public void setVector(List<Double> vector) {
    this.vector = vector;
    this.vectorStr = vector != null ? Serialization.doubleListToString(vector) : null;
  }

  public static final int INVALID_VALUE_ID = -1;
}
