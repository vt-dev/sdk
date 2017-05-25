package com.visualthreat.api.data;

import lombok.RequiredArgsConstructor;

import java.util.Iterator;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
public class FlattenCANIterator implements Iterator<CANFrame> {
  private final Iterator<Response> responses;
  private Iterator<CANFrame> current = null;

  @Override
  public boolean hasNext() {
    if (current != null && current.hasNext()) {
      return true;
    } else {
      if (responses.hasNext()) {
        current = responses.next().getResponses();
        return true;
      }
    }

    return false;
  }

  @Override
  public CANFrame next() {
    if (hasNext()) {
      return current.next();
    }

    throw new NoSuchElementException();
  }
}
