package com.buaa.cloud_evaluation.model;

import com.buaa.cloud_evaluation.service.ApiService;
import com.buaa.cloud_evaluation.util.Serialization;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class NodeModel {
  private int id;
  private String name;
  private int type;
  private int parent;
  @JsonIgnore
  private String historyValueIds;
  @JsonIgnore
  private Integer currentValueId;
  private Integer source;

  private List<NodeValueModel> historyValues = new ArrayList<>();
  private NodeValueModel currentValue;

  public void fillNodeValues(ApiService service) {
    List<Integer> ids = Serialization.stringToIntList(historyValueIds);
    historyValues.clear();
    for (Integer id : ids) {
      historyValues.add(service.getNodeValue(id));
    }

    if (currentValueId == NodeValueModel.INVALID_VALUE_ID) {
      currentValue = null;
    } else {
      currentValue = service.getNodeValue(currentValueId);
    }
  }

  public RelationNodeModel wrap() {
    RelationNodeModel rNode = new RelationNodeModel();
    rNode.setNode(this);
    rNode.setChildren(new ArrayList<>());
    return rNode;
  }

  public static final int TYPE_ELEMENT = 0;
  public static final int TYPE_CRITERIA = 1;

  public static final int INVALID_PARENT = -1;
  public static final int INVALID_SOURCE = -1;
}
