/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package net.cablecom.ctsp.query.model;

import java.util.concurrent.Future;

/**
 * TODO: add type comment.
 * 
 * @param <V>
 * 
 */
public interface CallbackFuture<V> extends Future<V> {
  public interface CallbackHandler<V> {
    void handleValue(final V value);

    void handleException(final Throwable exception);
  }

  void registerCallback(final CallbackHandler<V> handler);
}
