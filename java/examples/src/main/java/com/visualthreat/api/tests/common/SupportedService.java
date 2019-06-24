package com.visualthreat.api.tests.common;

import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Arrays;

@EqualsAndHashCode
public class SupportedService implements Serializable {
  private static final long serialVersionUID = -4923362744533641383L;

  public boolean highSecurity;
  public String name;
  public Integer ecuId;
  public Integer responseId;
  public byte supportedService;
  public byte[] subFunction = null;

  @Override
  public String toString() {
    String subFunctionStr = "";
    if (subFunction != null && subFunction.length > 0) {
      StringBuilder sb = new StringBuilder();
      sb.append(" SubFunction=");
      for (int i = 0; i < subFunction.length - 1; i++) {
        sb.append(String.format("0x%X,", subFunction[i]));
      }
      sb.append(String.format("0x%X", subFunction[subFunction.length - 1]));
      subFunctionStr = sb.toString();
    }
    return String.format("0x%X %s 0x%X 0x%X %s ServiceId=0x%X", ecuId,
        (highSecurity) ? "Supported Services with security:" : "Supported Services:",
        ecuId, responseId, name, supportedService) + subFunctionStr;
  }

  public SupportedService() {

  }

  // Copy structure
  public SupportedService(SupportedService in) {
    this.highSecurity = in.highSecurity;
    this.name = in.name;
    this.ecuId = in.ecuId;
    this.responseId = in.responseId;
    this.supportedService = in.supportedService;
    if (in.subFunction != null) {
      this.subFunction = Arrays.copyOf(in.subFunction, in.subFunction.length);
    }
  }
}
