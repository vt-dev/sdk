package com.visualthreat.api.data;

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
  public static final CANResponseFilter NONE = new CANResponseFilter(Collections.emptySet(), Collections.emptySet());
  public static final int MAX_ID = 0x7ff;

  private final Set<Integer> ids;
  private final Set<ByteArrayFilter> byteFilters;

  public static CANResponseFilter filter(final Set<Integer> ids, final Set<ByteArrayFilter> byteFilters) {
    return new CANResponseFilter(ids, byteFilters);
  }

  public static CANResponseFilter filterIds(final Set<Integer> ids) {
    return new CANResponseFilter(ids, Collections.emptySet());
  }

  public static CANResponseFilter filterBytes(final Set<ByteArrayFilter> byteFilters) {
    return new CANResponseFilter(Collections.emptySet(), byteFilters);
  }

  public static CANResponseFilter filterId(final int id) {
    return filterIds(Collections.singleton(id));
  }
}
