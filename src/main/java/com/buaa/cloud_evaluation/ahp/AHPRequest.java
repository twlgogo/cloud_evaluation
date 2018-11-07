package com.buaa.cloud_evaluation.ahp;

import java.util.List;
import lombok.Data;

@Data
public class AHPRequest {
  private int n; // matrix n;
  private List<Integer> list;
}
