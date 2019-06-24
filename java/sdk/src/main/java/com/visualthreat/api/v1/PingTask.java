package com.visualthreat.api.v1;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.websocket.Session;
import java.util.TimerTask;

import static com.visualthreat.api.v1.Utils.encodeJson;

@RequiredArgsConstructor
@Slf4j
public class PingTask extends TimerTask {
  private static final WsMessage PING = new WsMessage(0, WsMessage.Type.PING, "");
  private static final String PING_MESSAGE = encodeJson(PING);
  private final Session wsSession;

  @Override
  public void run() {
    wsSession.getAsyncRemote().sendText(PING_MESSAGE);
  }
}
