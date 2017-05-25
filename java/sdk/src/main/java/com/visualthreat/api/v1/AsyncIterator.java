package com.visualthreat.api.v1;

import lombok.Getter;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class AsyncIterator<T> implements Iterator<T> {
  @SuppressWarnings("unchecked")
  private final T TERMINATOR = (T) new Object();
  @Getter
  private final BlockingDeque<T> queue = new LinkedBlockingDeque<>();

  private T next = TERMINATOR;

  @Override
  public boolean hasNext() {
    if (next != TERMINATOR) {
      return true;
    }

    try {
      next = queue.take(); // blocks
    } catch (final InterruptedException e) {
      return false;
    }

    return next != TERMINATOR;
  }

  @Override
  public T next() {
    if (hasNext()) {
      T tmp = next;
      next = TERMINATOR;
      return tmp;
    }

    throw new NoSuchElementException();
  }

  public void stop() {
    queue.offer(TERMINATOR);
  }
}
