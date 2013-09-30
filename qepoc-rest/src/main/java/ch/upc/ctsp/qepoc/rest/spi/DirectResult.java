/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest.spi;

import java.util.concurrent.TimeUnit;

import ch.upc.ctsp.qepoc.rest.model.CallbackFuture;
import ch.upc.ctsp.qepoc.rest.model.QueryResult;


/**
 * A Future with only one String as Value
 * 
 */
public class DirectResult implements CallbackFuture<QueryResult> {
  private final QueryResult value;

  public DirectResult(final QueryResult value) {
    this.value = value;
  }

  @Override
  public boolean cancel(final boolean mayInterruptIfRunning) {
    // already finished
    return false;
  }

  @Override
  public QueryResult get() {
    return value;
  }

  @Override
  public QueryResult get(final long timeout, final TimeUnit unit) {
    return value;
  }

  @Override
  public boolean isCancelled() {
    // cannot be cancelled
    return false;
  }

  @Override
  public boolean isDone() {
    // is always done
    return true;
  }

  @Override
  public void registerCallback(final CallbackFuture.CallbackHandler<QueryResult> handler) {
    handler.handleValue(value);
  }

}