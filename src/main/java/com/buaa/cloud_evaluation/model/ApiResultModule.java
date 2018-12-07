package com.buaa.cloud_evaluation.model;

import lombok.Data;

@Data
public class ApiResultModule<VALUE> {
  private boolean success;
  private VALUE value;
  private String error;

  public static <VALUE> ApiResultModule<VALUE> success(VALUE value) {
    ApiResultModule<VALUE> result = new ApiResultModule<>();
    result.setSuccess(true);
    result.setValue(value);
    return result;
  }

  public static <VALUE> ApiResultModule<VALUE> error(String error) {
    ApiResultModule<VALUE> result = new ApiResultModule<>();
    result.setSuccess(false);
    result.setError(error);
    return result;
  }
}
