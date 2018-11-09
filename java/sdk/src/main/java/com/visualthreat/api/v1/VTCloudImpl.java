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
import static com.visualthreat.api.v1.WsMessage.Type.*;

@Slf4j
@RequiredArgsConstructor
public class VTCloudImpl implements VTCloud {
  private static final long PING_INTERVAL = 30000L;
  private static final CANFrame SNIFF_FRAME = null;
  private static final int MAX_FRAMES = 100;

  private final Timer timer = new Timer();
  private final Map<Long, AsyncIterator<Response>> iterators = new ConcurrentHashMap<>();
  private final AtomicLong id = new AtomicLong();
  private final PingTask pingTask;
  private final Session wsSession;
  private final Set<Long> sniffIDs = Collections.synchronizedSet(new HashSet<>());

  VTCloudImpl(final Session wsSession) {
    this.wsSession = wsSession;
    pingTask = new PingTask(wsSession);
    timer.schedule(pingTask, PING_INTERVAL, PING_INTERVAL);
    wsSession.addMessageHandler(String.class, this::textMessageHandler);
  }

  @Override
  public Iterator<Response> sendCANFrames(final Collection<Request> requests,
                                          final CANResponseFilter canResponseFilter) throws APIException {
    final List<List<Request>> batches = batches(new ArrayList<>(requests), MAX_FRAMES);
    final List<Iterator<Response>> batchIterators = new ArrayList<>();
    for (final List<Request> batch : batches) {
      final long msgId = this.id.incrementAndGet();
      final WsMessage message = buildCANMessage(msgId, batch, canResponseFilter);
      batchIterators.add(sendWsMessage(msgId, message));
    }

    return new MergeIterator<>(batchIterators);
  }

  @Override
  public Iterator<CANFrame> sniff(final long interval,
                                  final CANResponseFilter canResponseFilter) {
    final long msgId = this.id.incrementAndGet();
    sniffIDs.add(msgId);
    final WsMessage message = buildSniffMessage(msgId, interval, canResponseFilter);
    return new FlattenCANIterator(sendWsMessage(msgId, message));
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

  private void textMessageHandler(final String msg) {
    try {
      final WsMessage message = json.readValue(msg, WsMessage.class);
      syncHandler(message);
    } catch (final IOException e) {
      log.warn("Incorrect message received: " + msg, e);
    }
  }

  private synchronized void syncHandler(final WsMessage message) throws IOException {
    messageHandler(message);
  }

  @SuppressWarnings("Duplicates")
  private void messageHandler(final WsMessage message) throws IOException {
    switch (message.getType()) {
      case CAN_FRAMES:
        final APICanMessage[] canFrames = json.readValue(message.getMessage(), APICanMessage[].class);
        final AsyncIterator<Response> async = iterators.get(message.getId());
        boolean isSniff = sniffIDs.contains(message.getId());
        if (async != null) {
          AsyncIterator<CANFrame> frames = Optional.ofNullable(async.last())
              .map(Response::getResponses)
              .filter(f -> !f.isStopped())
              .orElseGet(() -> {
                if (isSniff) {
                  final AsyncIterator<CANFrame> newIter = new AsyncIterator<>();
                  async.getQueue().offer(new Response(SNIFF_FRAME, newIter));
                  return newIter;
                }

                return null;
              });
          for (final APICanMessage canFrame : canFrames) {
            if (!canFrame.isResponse() && !isSniff) {
              // stop last iterator
              if (frames != null) {
                frames.stop();
              }
              // and add new
              frames = new AsyncIterator<>();
              async.getQueue().offer(new Response(canFrame, frames));
            } else {
              if (frames != null) {
                frames.getQueue().add(canFrame);
              }
            }
          }
        }
        break;
      case ZIP_CAN_FRAMES:
        final List<byte[]> data = Zip.decompress(Base64.getDecoder().decode(message.getMessage()));
        for (final byte[] b : data) {
          final WsMessage decompressed = new WsMessage(message.getId(), CAN_FRAMES, new String(b));
          messageHandler(decompressed);
        }
        break;
      case CAN_FINAL:
        final AsyncIterator<Response> iter = iterators.get(message.getId());
        if (iter != null) {
          final Response lastResponse = iter.last();
          if (lastResponse != null) {
            lastResponse.getResponses().stop();
          }
          iter.stop();
        }
        break;
      case DISCONNECTED:
        close();
    }
  }

  private void finish(final AsyncIterator<Response> async) {
    final Response lastResponse = async.last();
    if (lastResponse != null) {
      lastResponse.getResponses().stop();
    }
    async.stop();
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

  private static WsMessage buildCANMessage(final long id,
                                           final Collection<Request> requests,
                                           final CANResponseFilter canResponseFilter) {
    final String message = encodeJson(new CANMessage(requests, canResponseFilter));
    return new WsMessage(id, ZIP_CAN_REQUEST, compress(message));
  }

  private static WsMessage buildSniffMessage(final long id,
                                             final long interval,
                                             final CANResponseFilter canResponseFilter) {
    final String message = encodeJson(new SniffMessage(interval, canResponseFilter));
    return new WsMessage(id, ZIP_CAN_SNIFF, compress(message));
  }

  private static String compress(final String text) {
    return new String(Base64.getEncoder().encode(Zip.compress(text, "can.traffic")));
  }

  private static <T> List<List<T>> batches(final List<T> list, final int batchSize) {
    final int size = list.size();
    final List<List<T>> batches = new ArrayList<>();
    for (int from = 0; from < size; from += batchSize) {
      final int to = Math.min(from + batchSize, size);
      batches.add(list.subList(from, to));
    }

    return batches;
  }
}
