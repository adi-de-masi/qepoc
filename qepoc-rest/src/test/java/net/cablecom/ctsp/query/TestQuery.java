/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package net.cablecom.ctsp.query;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import net.cablecom.ctsp.query.impl.QueryImpl;
import net.cablecom.ctsp.query.model.CallbackFuture;
import net.cablecom.ctsp.query.model.PathDescription;
import net.cablecom.ctsp.query.model.QueryRequest;
import net.cablecom.ctsp.query.model.QueryResult;
import net.cablecom.ctsp.query.spi.Backend;
import net.cablecom.ctsp.query.spi.DirectResult;

import org.junit.Test;

/**
 * TODO: add type comment.
 * 
 */
public class TestQuery {
  @Test
  public void testResolver() throws InterruptedException, ExecutionException {
    final QueryImpl query = new QueryImpl();
    query.registerBackend(PathDescription.createFromString("value/{value}"), new Backend() {

      @Override
      public CallbackFuture<QueryResult> query(final QueryRequest request, final Map<String, String> parameters, final Query query) {
        return new DirectResult(QueryResult.newWithLifeTime(parameters.get("value"), 5));
      }
    });
    query.registerBackend(PathDescription.createFromString("alias/"), new Backend() {

      @Override
      public CallbackFuture<QueryResult> query(final QueryRequest request, final Map<String, String> parameters, final Query query) {
        final List<String> pathComps = request.getPath();
        final List<String> remainingComps = pathComps.subList(Integer.parseInt(parameters.get("match-length")), pathComps.size());
        final QueryRequest nextRequest = new QueryRequest.Builder(request).path("value").appendPathComps(remainingComps).build();
        return query.query(nextRequest);
      }
    });
    final String value = query.query(QueryRequest.createRequest("value/Hello World")).get().getValue();
    assertEquals("Hello World", value);
    final String aliasValue = query.query(QueryRequest.createRequest("alias/Hello Mirror World")).get().getValue();
    assertEquals("Hello Mirror World", aliasValue);
    final String v2 = query.query(QueryRequest.createRequest("notexists/v2")).get().getValue();
    System.out.println(v2);
  }
}
