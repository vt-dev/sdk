package com.visualthreat.api.v1;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.visualthreat.api.data.CANFrame;
import lombok.Getter;

public class APICanMessage extends CANFrame {
  @Getter
  @JsonProperty("response")
  private final boolean isResponse;

  @JsonCreator
  public APICanMessage(@JsonProperty("timestamp") final long timestamp,
                       @JsonProperty("id") final int id,
                       @JsonProperty("data") final byte[] data,
                       @JsonProperty("response") final boolean isResponse) {
    super(timestamp, id, data);

    this.isResponse = isResponse;
  }
}
