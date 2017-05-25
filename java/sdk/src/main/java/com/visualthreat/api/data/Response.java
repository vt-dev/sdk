package com.visualthreat.api.data;

import com.visualthreat.api.v1.AsyncIterator;
import lombok.Value;

@Value
public class Response {
  private final CANFrame request;
  private final AsyncIterator<CANFrame> responses;
}
