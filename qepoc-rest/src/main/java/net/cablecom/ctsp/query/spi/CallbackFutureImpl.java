/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package net.cablecom.ctsp.query.spi;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import net.cablecom.ctsp.query.model.CallbackFuture;

/**
 * TODO: add type comment.
 * 
 * @param <V>
 * 
 */
public class CallbackFutureImpl<V> implements CallbackFuture<V> {
  private V                               value           = null;
  private boolean                         cancelled       = false;
  private boolean                         done            = false;
  private final Queue<CallbackHandler<V>> waitingHandlers = new ConcurrentLinkedQueue<CallbackFuture.CallbackHandler<V>>();
  private Throwable                       exception       = null;
  private final AtomicReference<Runnable> pollerHandler   = new AtomicReference<Runnable>(null);

  @Override
  public synchronized boolean cancel(final boolean mayInterruptIfRunning) {
    cancelled = true;
    return !done;
  }

  @Override
  public V get() throws InterruptedException, ExecutionException {
    try {
      return get(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    } catch (final TimeoutException e) {
      throw new RuntimeException("Ups, es sind schon 146'235'604 Jahre vergangen", e);
    }
  }

  @Override
  public V get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    if (done) {
      return value;
    }
    final AtomicBoolean done = new AtomicBoolean(false);
    final AtomicReference<V> resultValue = new AtomicReference<V>(null);
    final AtomicReference<Throwable> resultException = new AtomicReference<Throwable>(null);
    registerCallback(new CallbackHandler<V>() {

      @Override
      public void handleException(final Throwable exception) {
        done.set(true);
        resultException.set(exception);
      }

      @Override
      public void handleValue(final V value) {
        done.set(true);
        resultValue.set(value);
      }
    });
    final Runnable existingPollerHandler = pollerHandler.getAndSet(null);
    if (existingPollerHandler != null) {
      existingPollerHandler.run();
    }
    synchronized (this) {
      if (done.get()) {
        return takeResult(resultValue, resultException);
      }
      wait(unit.toMillis(timeout));
      if (done.get()) {
        return takeResult(resultValue, resultException);
      }
      throw new TimeoutException();
    }
  }

  @Override
  public boolean isCancelled() {
    return cancelled;
  }

  @Override
  public boolean isDone() {
    return done;
  }

  @Override
  public void registerCallback(final CallbackFuture.CallbackHandler<V> handler) {
    waitingHandlers.add(handler);
    if (done) {
      callAllHandlers();
    }
  }

  /**
   * Sets the pollerHandler.
   * 
   * @param pollerHandler
   *          the pollerHandler to set
   */
  public void setPollerHandler(final Runnable pollerHandler) {
    this.pollerHandler.set(pollerHandler);
  }

  public synchronized void setResultException(final Throwable exception) {
    checkIfDone(null);
    this.exception = exception;
    done = true;
    callAllHandlers();
    this.notifyAll();
  }

  public synchronized void setResultValue(final V value) {
    checkIfDone(value);
    this.value = value;
    done = true;
    callAllHandlers();
    this.notifyAll();
  }

  private void callAllHandlers() {
    while (true) {
      final CallbackFuture.CallbackHandler<V> handler = waitingHandlers.poll();
      if (handler == null) {
        break;
      }
      if (exception != null) {
        handler.handleException(exception);
      } else {
        handler.handleValue(this.value);
      }
    }
  }

  private void checkIfDone(final V value) {
    if (done) {
      throw new IllegalStateException("Cannot set result value to " + value + ", result is already set to " + this.value);
    }
  }

  private V takeResult(final AtomicReference<V> resultValue, final AtomicReference<Throwable> resultException) throws ExecutionException {
    final Throwable exceptionValue = resultException.get();
    if (exceptionValue != null) {
      throw new ExecutionException(exceptionValue);
    }
    return resultValue.get();
  }
}
