package com.visualthreat.api.v1;

import com.visualthreat.api.data.CANResponseFilter;
import lombok.Value;

@Value
public class SniffMessage {
  final long interval;
  final CANResponseFilter canResponseFilter;
}
