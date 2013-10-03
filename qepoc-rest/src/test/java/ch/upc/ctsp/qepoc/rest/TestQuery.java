/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

import ch.upc.ctsp.qepoc.rest.impl.QueryImpl;
import ch.upc.ctsp.qepoc.rest.model.CallbackFuture;
import ch.upc.ctsp.qepoc.rest.model.PathDescription;
import ch.upc.ctsp.qepoc.rest.model.QueryRequest;
import ch.upc.ctsp.qepoc.rest.model.QueryResult;
import ch.upc.ctsp.qepoc.rest.rules.Alias;
import ch.upc.ctsp.qepoc.rest.rules.Alias.Builder;
import ch.upc.ctsp.qepoc.rest.spi.Backend;
import ch.upc.ctsp.qepoc.rest.spi.DirectResult;
import ch.upc.ctsp.qepoc.rest.spi.QueryContext;

/**
 * TODO: add type comment.
 * 
 */
public class TestQuery {
    @Test
    public void testAlias() throws InterruptedException, ExecutionException {
        final QueryImpl query = new QueryImpl();
        query.registerBackend(PathDescription.createFromString("value/{value}"), new Backend() {

            @Override
            public CallbackFuture<QueryResult> query(final QueryContext context) {
                return new DirectResult(QueryResult.newWithLifeTime(context.getParameterMap().get("value"), 5));
            }
        });
        query.registerBackend(PathDescription.createFromString("alias1/{value}"), new Alias.Builder().addConstEntry("value")
                .addVariableEntry("value").build());
        query.registerBackend(PathDescription.createFromString("alias2"), new Alias.Builder().addConstEntry("value").build());
        final Builder alias3Builder = new Alias.Builder().addConstEntry("value");
        alias3Builder.createPatternEntry("{0} World").addVariableEntry("value");
        query.registerBackend(PathDescription.createFromString("alias3/{value}"), alias3Builder.build());
        final String value = query.query(QueryRequest.createRequest("value/Hello World")).get().getValue();
        assertEquals("Hello World", value);
        final String aliasValue = query.query(QueryRequest.createRequest("alias1/Hello Mirror World")).get().getValue();
        assertEquals("Hello Mirror World", aliasValue);
        final String secondAliasValue = query.query(QueryRequest.createRequest("alias2/Hello second Mirror World")).get().getValue();
        assertEquals("Hello second Mirror World", secondAliasValue);
        final String thirdAliasValue = query.query(QueryRequest.createRequest("alias3/Hello")).get().getValue();
        assertEquals("Hello World", thirdAliasValue);
    }

    @Test
    public void testResolver() throws InterruptedException, ExecutionException {
        final QueryImpl query = new QueryImpl();
        query.registerBackend(PathDescription.createFromString("value/{value}"), new Backend() {

            @Override
            public CallbackFuture<QueryResult> query(final QueryContext context) {
                return new DirectResult(QueryResult.newWithLifeTime(context.getParameterMap().get("value"), 5));
            }
        });
        query.registerBackend(PathDescription.createFromString("alias/"), new Backend() {

            @Override
            public CallbackFuture<QueryResult> query(final QueryContext context) {
                final QueryRequest request = context.getRequest();
                final List<String> pathComps = request.getPath();
                final List<String> remainingComps = pathComps.subList(context.getPathLength(), pathComps.size());
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
