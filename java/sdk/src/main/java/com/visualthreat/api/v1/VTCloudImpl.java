package com.visualthreat.api.v1;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.visualthreat.api.VTCloud;
import com.visualthreat.api.data.*;
import com.visualthreat.api.exception.APIException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.websocket.Session;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static com.visualthreat.api.v1.Utils.encodeJson;
import static com.visualthreat.api.v1.Utils.json;

@Slf4j
@RequiredArgsConstructor
public class VTCloudImpl implements VTCloud {
  private static final long PING_INTERVAL = 30000L;
  private static final CANFrame SNIFF_FRAME = null;

  private final Timer timer = new Timer();
  private final Map<Long, AsyncIterator<Response>> iterators = new ConcurrentHashMap<>();
  private final PingTask pingTask;
  private final Session wsSession;

  private AtomicLong id = new AtomicLong();
  private CANFrame requestFrame = SNIFF_FRAME;
  private AsyncIterator<CANFrame> frames = new AsyncIterator<>();


  VTCloudImpl(final Session wsSession) {
    this.wsSession = wsSession;
    pingTask = new PingTask(wsSession);
    timer.schedule(pingTask, PING_INTERVAL, PING_INTERVAL);
    wsSession.addMessageHandler(String.class, this::messageHandler);
  }

  @Override
  public Iterator<Response> sendCANFrames(final Collection<Request> requests,
                                          final CANResponseFilter canResponseFilter) throws APIException {
    final long id = this.id.incrementAndGet();
    final WsMessage message = buildCANMessage(id, requests, canResponseFilter);
    return sendWsMessage(id, message);
  }

  @Override
  public Iterator<CANFrame> sniff(final long interval,
                                  final CANResponseFilter canResponseFilter) {
    final long id = this.id.incrementAndGet();
    final WsMessage message = buildSniffMessage(id, interval, canResponseFilter);
    return new FlattenCANIterator(sendWsMessage(id, message));
  }

  @Override
  public void cancelRequest() {
    iterators.values().forEach(this::finish);
    iterators.clear();
    sendWsMessageNow(new WsMessage(id.incrementAndGet(), WsMessage.Type.CAN_CANCEL, ""));
  }

  @Override
  public void close() {
    try {
      pingTask.cancel();
      timer.cancel();
      wsSession.close();
    } catch (final IOException e) {
      log.warn("Can't close cloud connection", e);
    }
  }

  private void messageHandler(final String msg) {
    try {
      final WsMessage message = json.readValue(msg, WsMessage.class);
      switch (message.getType()) {
        case CAN_FRAMES:
          final Collection<APICanMessage> canFrames = Arrays.asList(
              json.readValue(message.getMessage(), APICanMessage[].class));
          final AsyncIterator<Response> async = iterators.get(message.getId());
          if (async != null) {
            for (final APICanMessage canFrame : canFrames) {
              if (!canFrame.isResponse()) {
                // add frames
                finish(async);
                // cleanup
                requestFrame = canFrame;
              } else {
                frames.getQueue().add(canFrame);
              }
            }
          }
          break;
        case CAN_FINAL:
          final AsyncIterator<Response> iter = iterators.get(message.getId());
          if (iter != null) {
            finish(iter);
            iter.stop();
          }
          break;
        case DISCONNECTED:
          close();
      }
    } catch (final IOException e) {
      log.warn("Incorrect message received: " + msg, e);
    }
  }

  private void finish(final AsyncIterator<Response> async) {
    if (requestFrame != SNIFF_FRAME || !frames.getQueue().isEmpty()) {
      frames.stop();
      async.getQueue().add(new Response(requestFrame, frames));
      frames = new AsyncIterator<>();
    }
  }

  private Iterator<Response> sendWsMessage(final long id, final WsMessage wsMessage)
      throws APIException {
    try {
      final String message = json.writeValueAsString(wsMessage);
      sendTextMessage(message);
      final AsyncIterator<Response> async = new AsyncIterator<>();
      iterators.put(id, async);
      return async;
    } catch (final JsonProcessingException e) {
      throw new APIException(e);
    }
  }

  private WsMessage buildCANMessage(final long id,
                                    final Collection<Request> requests,
                                    final CANResponseFilter canResponseFilter) {
    final String message = encodeJson(new CANMessage(requests, canResponseFilter));
    return new WsMessage(id, WsMessage.Type.CAN_REQUEST, message);
  }

  private WsMessage buildSniffMessage(final long id,
                                      final long interval,
                                      final CANResponseFilter canResponseFilter) {
    final String message = encodeJson(new SniffMessage(interval, canResponseFilter));
    return new WsMessage(id, WsMessage.Type.CAN_SNIFF, message);
  }

  private void sendWsMessageNow(final WsMessage message) {
    try {
      final String msg = json.writeValueAsString(message);
      sendTextMessage(msg);
    } catch (final JsonProcessingException e) {
      log.error("Can't encode message to JSON", e);
    }
  }

  private void sendTextMessage(final String message) {
    log.trace("Send WS message, size: {}", message.length());
    wsSession.getAsyncRemote().sendText(message);
  }
}
