package com.visualthreat.api.data;

import lombok.Value;

import java.util.Collection;

@Value
public class CANMessage {
  private final Collection<Request> requests;
  private final CANResponseFilter canResponseFilter;
}
