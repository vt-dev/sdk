package com.visualthreat.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.visualthreat.api.data.*;
import com.visualthreat.api.v1.SniffMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.visualthreat.api.Tests.rnd;
import static com.visualthreat.api.v1.Utils.json;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Simple entities serialization test")
public class SimpleEntitiesSerializationTest {

  @Test
  @DisplayName("CAN Frame, serialized to JSON")
  void canFrameToJson() throws JsonProcessingException {

    final byte[] bytes = new byte[8];
    rnd.nextBytes(bytes);
    final int id = rnd.nextInt(1000);

    final CANFrame canFrame = new CANFrame(id, bytes);

    final String actualJson = json.writeValueAsString(canFrame);
    final byte[] encodedData = Base64.getEncoder().encode(bytes);

    final String expectedJson = String.format("{\"timestamp\":%d,\"id\":%d,\"data\":\"%s\"}",
        canFrame.getTimestamp(), id, new String(encodedData));

    assertEquals(expectedJson, actualJson, "Incorrect JSON serialization");
  }

  @Test
  @DisplayName("Request, serialized to JSON")
  public void requestToJson() throws JsonProcessingException {
    final byte[] bytes = new byte[8];
    rnd.nextBytes(bytes);
    final int frameId = rnd.nextInt(1000);
    final long waitTime = 1000L;
    final Request request = Request.Builder.newBuilder().data(bytes).id(frameId).waitTime(waitTime).build();

    final byte[] encodedData = Base64.getEncoder().encode(bytes);
    final String expectedJson = String.format("{\"canFrame\":{\"timestamp\":%d,\"id\":%d,\"data\":\"%s\"},\"waitTime\":%d}",
        request.getCanFrame().getTimestamp(), frameId, new String(encodedData), waitTime);

    final String actualJson = json.writeValueAsString(request);
    assertEquals(expectedJson, actualJson, "Incorrect JSON serialization");
  }

  @Test
  @DisplayName("SniffMessage, serialized to JSON")
  public void sniffMessageToJson() throws JsonProcessingException {
    final long interval = 1000L;

    final byte[] bytes = new byte[3];
    rnd.nextBytes(bytes);

    final Set<ByteArrayFilter> byteFilters = new HashSet<>();
    byteFilters.add(ByteArrayFilter.Builder.newBuilder()
        .byte1(bytes[0])
        .byte3(bytes[1])
        .byte6(bytes[2])
        .build());
    final CANResponseFilter filter = CANResponseFilter.filterBytes(byteFilters);

    final String expectedJson = String.format("{\"interval\":1000,\"canResponseFilter\":{\"ids\":[],\"byteFilters\":[{\"byte0\":null,\"byte1\":%d,\"byte2\":null,\"byte3\":%d,\"byte4\":null,\"byte5\":null,\"byte6\":%d,\"byte7\":null}],\"minId\":-1,\"maxId\":-1}}", bytes[0], bytes[1], bytes[2]);

    final SniffMessage sniffMessage = new SniffMessage(interval, filter);

    final String actualJson = json.writeValueAsString(sniffMessage);

    assertEquals(expectedJson, actualJson, "Incorrect JSON serialization");
  }

  @Test
  @DisplayName("CanMessage, serialized to JSON")
  public void canMessageToJson() throws JsonProcessingException {
    final byte[] requestBytes = new byte[8];
    rnd.nextBytes(requestBytes);
    final int frameId = rnd.nextInt(1000);
    final long waitTime = 1000L;

    final Request request = Request.Builder.newBuilder().data(requestBytes).id(frameId).waitTime(waitTime).build();

    final byte[] bytes = new byte[3];
    rnd.nextBytes(bytes);

    final Set<ByteArrayFilter> byteFilters = new HashSet<>();
    byteFilters.add(ByteArrayFilter.Builder.newBuilder()
        .byte1(bytes[0])
        .byte3(bytes[1])
        .byte6(bytes[2])
        .build());
    final CANResponseFilter filter = CANResponseFilter.filterBytes(byteFilters);
    final CANMessage canMessage = new CANMessage(Collections.singletonList(request), filter);
    final String actualJson = json.writeValueAsString(canMessage);

    final byte[] encodedData = Base64.getEncoder().encode(requestBytes);

    final String expectedJson = String.format(
        "{\"requests\":[{\"canFrame\":{\"timestamp\":%d,\"id\":%d,\"data\":\"%s\"},\"waitTime\":1000}],\"canResponseFilter\":{\"ids\":[],\"byteFilters\":[{\"byte0\":null,\"byte1\":%d,\"byte2\":null,\"byte3\":%d,\"byte4\":null,\"byte5\":null,\"byte6\":%d,\"byte7\":null}],\"minId\":-1,\"maxId\":-1}}",
        request.getCanFrame().getTimestamp(), frameId, new String(encodedData), bytes[0], bytes[1], bytes[2]);

    assertEquals(expectedJson, actualJson, "Incorrect JSON serialization");
  }
}
