package com.visualthreat.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.visualthreat.api.data.ByteArrayFilter;
import com.visualthreat.api.data.CANResponseFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.visualthreat.api.Tests.rnd;
import static com.visualthreat.api.v1.Utils.json;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("CAN Response Filter")
class CANResponseFilterTest {
  @Test
  @DisplayName("ID Filter, serialized to JSON")
  void idFilterToJson() throws JsonProcessingException {
    final int id = rnd.nextInt(CANResponseFilter.MAX_ID + 1);
    final CANResponseFilter filter = CANResponseFilter.filterId(id);
    final String expectedJson = String.format("{\"ids\":[%d],\"byteFilters\":[]}", id);
    final String actualJson = json.writeValueAsString(filter);

    assertEquals(expectedJson, actualJson, "Incorrect JSON serialization");
  }

  @Test
  @DisplayName("Byte filters, serialized to JSON")
  void byteFiltersToJson() throws JsonProcessingException {
    final byte[] bytes = new byte[3];
    rnd.nextBytes(bytes);

    final Set<ByteArrayFilter> byteFilters = new HashSet<>();
    byteFilters.add(ByteArrayFilter.Builder.newBuilder()
        .byte1(bytes[0])
        .byte3(bytes[1])
        .byte6(bytes[2])
        .build());

    final CANResponseFilter filter = CANResponseFilter.filterBytes(byteFilters);

    final String expectedJson = String.format("{\"ids\":[],\"byteFilters\":" +
            "[{\"byte0\":null,\"byte1\":%d,\"byte2\":null,\"byte3\":%d," +
            "\"byte4\":null,\"byte5\":null,\"byte6\":%d,\"byte7\":null}]}",
        bytes[0], bytes[1], bytes[2]);
    final String actualJson = json.writeValueAsString(filter);

    assertEquals(expectedJson, actualJson, "Incorrect JSON serialization");
  }

  @Test
  @DisplayName("ID and Byte Filters, serialized to JSON")
  void idAndByteFiltersToJson() throws JsonProcessingException {
    final Set<Integer> ids = Collections.singleton(rnd.nextInt(CANResponseFilter.MAX_ID));
    final byte[] bytes = new byte[2];
    rnd.nextBytes(bytes);

    final Set<ByteArrayFilter> byteFilters = new HashSet<>();
    byteFilters.add(ByteArrayFilter.Builder.newBuilder()
        .byte0(bytes[0])
        .byte5(bytes[1])
        .build());

    final CANResponseFilter filter = CANResponseFilter.filter(ids, byteFilters);

    final String expectedJson = String.format("{\"ids\":[%d],\"byteFilters\":" +
            "[{\"byte0\":%d,\"byte1\":null,\"byte2\":null,\"byte3\":null," +
            "\"byte4\":null,\"byte5\":%d,\"byte6\":null,\"byte7\":null}]}",
        ids.iterator().next(), bytes[0], bytes[1]);
    final String actualJson = json.writeValueAsString(filter);

    assertEquals(expectedJson, actualJson, "Incorrect JSON serialization");
  }
}
