package com.visualthreat.api.data;

import lombok.RequiredArgsConstructor;

import java.util.Iterator;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
public class FlattenCANIterator implements Iterator<CANFrame> {
  private final Iterator<Response> responses;
  private Iterator<CANFrame> current = null;
  private CANFrame next = null;

  @Override
  public boolean hasNext() {
    if (current != null && current.hasNext()) {
      return true;
    } else if (next != null) {
      return true;
    } else {
      if (responses.hasNext()) {
        final Response res = responses.next();
        next = res.getRequest();
        current = res.getResponses();
        return true;
      }
    }

    return false;
  }

  @Override
  public CANFrame next() {
    if (next != null) {
      final CANFrame tmp = next;
      next = null;
      return tmp;
    }

    if (hasNext()) {
      return current.next();
    }

    throw new NoSuchElementException();
  }
}
