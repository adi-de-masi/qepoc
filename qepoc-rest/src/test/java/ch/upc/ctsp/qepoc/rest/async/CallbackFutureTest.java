/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest.async;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;


import org.junit.Test;

import ch.upc.ctsp.qepoc.rest.model.CallbackFuture;
import ch.upc.ctsp.qepoc.rest.model.CallbackFuture.CallbackHandler;
import ch.upc.ctsp.qepoc.rest.spi.CallbackFutureImpl;

/**
 * TODO: add type comment.
 * 
 */
public class CallbackFutureTest {
  /**
   * 
   */
  private static final int MASS_COUNT = 10000;

  @Test
  public void testMassAsyncResultWithCallback() throws InterruptedException {

    final ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(50);
    final Random random = new Random();
    try {
      final ArrayList<CallbackFuture<Integer>> callbacks = new ArrayList<CallbackFuture<Integer>>();
      for (int i = 0; i < MASS_COUNT; i++) {
        final CallbackFutureImpl<Integer> value = new CallbackFutureImpl<Integer>();
        final int index = i;
        callbacks.add(value);
        final int waitTime = random.nextInt(10);
        threadPool.schedule(new Runnable() {
          @Override
          public void run() {
            value.setResultValue(Integer.valueOf(index));
          }
        }, waitTime, TimeUnit.MILLISECONDS);
      }
      final Map<Integer, Boolean> resultsReturned = new ConcurrentHashMap<Integer, Boolean>();
      for (final CallbackFuture<Integer> callbackFuture : callbacks) {
        callbackFuture.registerCallback(new CallbackHandler<Integer>() {

          @Override
          public void handleException(final Throwable exception) {
            assertTrue(false);
          }

          @Override
          public void handleValue(final Integer value) {
            final Boolean oldValue = resultsReturned.put(value, Boolean.TRUE);
            assertNull(oldValue);
          }
        });
      }
      threadPool.shutdown();
      threadPool.awaitTermination(5, TimeUnit.MINUTES);
      assertEquals(MASS_COUNT, resultsReturned.size());
    } finally {
      threadPool.shutdown();
    }
  }

  @Test
  public void testMassAsyncResultWithGet() throws InterruptedException, ExecutionException {

    final ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(50);
    final Random random = new Random();
    try {
      final ArrayList<CallbackFuture<Integer>> callbacks = new ArrayList<CallbackFuture<Integer>>();
      for (int i = 0; i < MASS_COUNT; i++) {
        final CallbackFutureImpl<Integer> value = new CallbackFutureImpl<Integer>();
        final int index = i;
        callbacks.add(value);
        final int waitTime = random.nextInt(10);
        threadPool.schedule(new Runnable() {
          @Override
          public void run() {
            value.setResultValue(Integer.valueOf(index));
          }
        }, waitTime, TimeUnit.MILLISECONDS);
      }
      final Map<Integer, Boolean> resultsReturned = new ConcurrentHashMap<Integer, Boolean>();
      for (final CallbackFuture<Integer> callbackFuture : callbacks) {
        final Integer value = callbackFuture.get();
        final Boolean oldValue = resultsReturned.put(value, Boolean.TRUE);
        assertNull(oldValue);
      }
      assertEquals(MASS_COUNT, resultsReturned.size());
    } finally {
      threadPool.shutdown();
    }
  }

  @Test
  public void testSingleAsyncException() throws InterruptedException, TimeoutException {
    final ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(2);
    try {
      final CallbackFutureImpl<Integer> future = new CallbackFutureImpl<Integer>();
      threadPool.schedule(new Runnable() {

        @Override
        public void run() {
          future.setResultException(new RuntimeException("All Ok"));
        }
      }, 20, TimeUnit.MILLISECONDS);
      try {
        future.get(100, TimeUnit.MILLISECONDS);
        // Exception expected
        assertTrue(false);
      } catch (final ExecutionException ex) {
        final Throwable cause = ex.getCause();
        assertEquals(RuntimeException.class, cause.getClass());
        assertEquals("All Ok", cause.getMessage());
      }
    } finally {
      threadPool.shutdown();
    }
  }

  @Test
  public void testSingleAsyncResponse() throws InterruptedException, ExecutionException, TimeoutException {
    final ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(2);
    try {
      final CallbackFutureImpl<Integer> future = new CallbackFutureImpl<Integer>();
      threadPool.schedule(new Runnable() {

        @Override
        public void run() {
          future.setResultValue(Integer.valueOf(1));
        }
      }, 20, TimeUnit.MILLISECONDS);
      assertEquals(false, future.isDone());
      assertEquals(false, future.isCancelled());
      final Integer result = future.get(100, TimeUnit.MILLISECONDS);
      assertEquals(Integer.valueOf(1), result);
      assertEquals(true, future.isDone());
      assertEquals(false, future.isCancelled());
    } finally {
      threadPool.shutdown();
    }
  }

  @Test
  public void testSingleAsyncTimeout() throws InterruptedException, ExecutionException {
    final ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(2);
    try {
      final CallbackFutureImpl<Integer> future = new CallbackFutureImpl<Integer>();
      threadPool.schedule(new Runnable() {

        @Override
        public void run() {
          future.setResultValue(Integer.valueOf(1));
        }
      }, 20, TimeUnit.MILLISECONDS);
      try {
        // should call exception cause result is too late
        future.get(5, TimeUnit.MILLISECONDS);
        assertTrue(false);
      } catch (final TimeoutException ex) {
        // expected exception
      }
    } finally {
      threadPool.shutdown();
    }
  }

  @Test
  public void testSingleSyncResult() throws InterruptedException, ExecutionException {
    final CallbackFutureImpl<Integer> future = new CallbackFutureImpl<Integer>();
    future.setResultValue(Integer.valueOf(1));
    final Integer result = future.get();
    assertEquals(Integer.valueOf(1), result);
  }

  @Test
  public void testSingleSyncResultWithCallback() throws InterruptedException, ExecutionException {
    final CallbackFutureImpl<Integer> future = new CallbackFutureImpl<Integer>();
    future.setResultValue(Integer.valueOf(1));
    final AtomicBoolean handlerCalled = new AtomicBoolean(false);
    future.registerCallback(new CallbackHandler<Integer>() {

      @Override
      public void handleException(final Throwable exception) {
        assertTrue(false);
      }

      @Override
      public void handleValue(final Integer value) {
        handlerCalled.set(true);
        assertEquals(Integer.valueOf(1), value);
      }
    });
    assertTrue(handlerCalled.get());
  }
}
