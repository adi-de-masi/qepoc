/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

import ch.upc.ctsp.qepoc.rest.impl.QueryImpl;
import ch.upc.ctsp.qepoc.rest.model.CallbackFuture;
import ch.upc.ctsp.qepoc.rest.model.PathDescription;
import ch.upc.ctsp.qepoc.rest.model.QueryRequest;
import ch.upc.ctsp.qepoc.rest.model.QueryResult;
import ch.upc.ctsp.qepoc.rest.spi.Backend;
import ch.upc.ctsp.qepoc.rest.spi.DirectResult;

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
        // final String v2 = query.query(QueryRequest.createRequest("notexists/v2")).get().getValue();
        // System.out.println(v2);
    }
}
