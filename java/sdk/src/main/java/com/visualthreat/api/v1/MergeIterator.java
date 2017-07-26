package com.visualthreat.api.v1;

import java.util.Iterator;
import java.util.List;

public class MergeIterator<T> implements Iterator<T> {
  private final List<Iterator<T>> iterators;
  private int current;

  MergeIterator(final List<Iterator<T>> iterators) {
    this.iterators = iterators;
    current = 0;
  }

  @Override
  public boolean hasNext() {
    while (current < iterators.size() && !iterators.get(current).hasNext()) {
      current++;
    }

    return current < iterators.size();
  }

  @Override
  public T next() {
    while (current < iterators.size() && !iterators.get(current).hasNext())
      current++;

    return iterators.get(current).next();
  }
}
