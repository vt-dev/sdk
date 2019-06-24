package com.visualthreat.api.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.Collections;
import java.util.Set;

/**
 * Data class.
 * CAN filter for responses.
 * Filters CAN ID and/or byte values.
 */
@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CANResponseFilter {
  private static final Integer WRONG_ID = -1;

  public static final CANResponseFilter NONE = new CANResponseFilter(Collections.emptySet(), Collections.emptySet(),
      WRONG_ID, WRONG_ID);
  public static final int MAX_ID = 0x7ff;

  @JsonProperty
  private final Set<Integer> ids;
  @JsonProperty
  private final Set<ByteArrayFilter> byteFilters;
  @JsonProperty
  private final Integer minId;
  @JsonProperty
  private final Integer maxId;

  public static CANResponseFilter filter(final Set<Integer> ids, final Set<ByteArrayFilter> byteFilters) {
    return new CANResponseFilter(ids, byteFilters, WRONG_ID, WRONG_ID);
  }

  public static CANResponseFilter filter(final Set<Integer> ids, final Set<ByteArrayFilter> byteFilters, final int minId, final int maxId) {
    return new CANResponseFilter(ids, byteFilters, WRONG_ID, WRONG_ID);
  }

  public static CANResponseFilter filterIds(final Set<Integer> ids) {
    return new CANResponseFilter(ids, Collections.emptySet(), WRONG_ID, WRONG_ID);
  }

  public static CANResponseFilter filterIds(final int minId, final int maxId) {
    return new CANResponseFilter(Collections.emptySet(), Collections.emptySet(), minId, maxId);
  }

  public static CANResponseFilter filterBytes(final Set<ByteArrayFilter> byteFilters) {
    return new CANResponseFilter(Collections.emptySet(), byteFilters, WRONG_ID, WRONG_ID);
  }

  public static CANResponseFilter filterId(final int id) {
    return filterIds(Collections.singleton(id));
  }
}
