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
    ZIP_CAN_REQUEST("zip-can-request"),
    CAN_SNIFF("can-sniff"),
    ZIP_CAN_SNIFF("zip-can-sniff"),
    DISCONNECTED("disconnected"),
    CAN_FINAL("can-final"),
    ZIP_CAN_FRAMES("zip-can-frames"),
    CAN_FRAMES("can-frames");

    private final String name;
  }
}
