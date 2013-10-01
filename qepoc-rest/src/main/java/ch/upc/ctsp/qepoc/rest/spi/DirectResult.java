/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest.spi;

import java.util.concurrent.TimeUnit;

import ch.upc.ctsp.qepoc.rest.model.CallbackFuture;

/**
 * A Future with only one String as Value
 * 
 * @param <V>
 * 
 */
public class DirectResult<V> implements CallbackFuture<V> {
    private final V value;

    public DirectResult(final V value) {
        this.value = value;
    }

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        // already finished
        return false;
    }

    @Override
    public V get() {
        return value;
    }

    @Override
    public V get(final long timeout, final TimeUnit unit) {
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
    public void registerCallback(final CallbackFuture.CallbackHandler<V> handler) {
        handler.handleValue(value);
    }

}
