package com.buaa.cloud_evaluation.model;

import lombok.Data;
import org.springframework.stereotype.Component;


@Data
public class IndexModel {
  private int indexId;
  private String indexName;
  private float indexScore;
  private int indexParent;

}
