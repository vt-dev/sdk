package com.visualthreat.api.v1;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
class WsMessage {
  private final long id;
  private final Type type;
  private final String message;

  @Getter
  @RequiredArgsConstructor
  public enum Type {
    PING("ping"),
    CAN_CANCEL("can-cancel"),
    CAN_REQUEST("can-request"),
    CAN_SNIFF("can-sniff"),
    DISCONNECTED("disconnected"),
    CAN_FINAL("can-final"),
    CAN_FRAMES("can-frames");

    private final String name;
  }
}
