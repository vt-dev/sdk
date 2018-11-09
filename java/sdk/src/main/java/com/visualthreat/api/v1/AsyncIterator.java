package com.visualthreat.api.v1;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
public class AsyncIterator<T> implements Iterator<T> {
  @SuppressWarnings("unchecked")
  private final T TERMINATOR = (T) new Object();
  @Getter
  private final BlockingQueue<T> queue = new LinkedBlockingQueue<>();

  @Getter
  private boolean isStopped = false;
  private T next = TERMINATOR;
  private T last = null;

  @Override
  public boolean hasNext() {
    if (next != TERMINATOR) {
      return true;
    }

    try {
      next = queue.take(); // blocks
    } catch (final InterruptedException e) {
      // stop iterator, if we have been interrupted
      return false;
    }

    return next != TERMINATOR;
  }

  @Override
  public T next() {
    if (hasNext()) {
      last = next;
      next = TERMINATOR;
      return last;
    }

    throw new NoSuchElementException();
  }

  // non-blocking method
  T last() {
    if (last != TERMINATOR) {
      return last;
    } else if (!queue.isEmpty()) {
      if (hasNext()) {
        return last;
      }
    }

    return last;
  }

  void stop() {
    this.isStopped = true;
    queue.offer(TERMINATOR);
  }
}
