/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest.poc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang.StringUtils;

import ch.upc.ctsp.qepoc.client.Client;
import ch.upc.ctsp.qepoc.rest.Query;
import ch.upc.ctsp.qepoc.rest.impl.QueryBuilder;
import ch.upc.ctsp.qepoc.rest.model.CallbackFuture;
import ch.upc.ctsp.qepoc.rest.model.PathDescription;
import ch.upc.ctsp.qepoc.rest.model.QueryRequest;
import ch.upc.ctsp.qepoc.rest.model.QueryResult;
import ch.upc.ctsp.qepoc.rest.spi.Backend;
import ch.upc.ctsp.qepoc.rest.spi.CallbackFutureImpl;
import ch.upc.ctsp.qepoc.rest.spi.DirectResult;
import ch.upc.ctsp.qepoc.rest.spi.QueryContext;

/**
 * TODO: add type comment.
 * 
 */
public class PocMain {
    private static class BulkMockBackend implements Backend {
        private int                                                          queryNr         = 0;
        private final ConcurrentMap<String, CallbackFutureImpl<QueryResult>> pendingRequests = new ConcurrentHashMap<String, CallbackFutureImpl<QueryResult>>();

        @Override
        public CallbackFuture<QueryResult> query(final QueryContext context) {
            final QueryRequest request = context.getRequest();
            final List<String> path = request.getPath();
            final List<String> remainingPath = path.subList(context.getPathLength(), path.size());
            final String pathKey = "/" + StringUtils.join(remainingPath, "/");

            final CallbackFutureImpl<QueryResult> ret = new CallbackFutureImpl<QueryResult>();
            ret.setPollerHandler(new Runnable() {

                @Override
                public void run() {
                    final List<String> requests = new ArrayList<String>();
                    final List<CallbackFutureImpl<QueryResult>> results = new ArrayList<CallbackFutureImpl<QueryResult>>();
                    final Set<String> keys = pendingRequests.keySet();
                    for (final String key : keys) {
                        final CallbackFutureImpl<QueryResult> resultImpl = pendingRequests.remove(key);
                        if (resultImpl != null) {
                            requests.add(key);
                            results.add(resultImpl);
                        }
                    }
                    try {
                        System.out.println("-------------------------------------------------------------------------------------------------");
                        System.out.println("Querying (" + (++queryNr) + "): " + requests);
                        System.out.println("-------------------------------------------------------------------------------------------------");
                        final Date queryTime = new Date();
                        final String result = Client.query(requests.toArray(new String[requests.size()]));
                        final String[] answers = result.split("\n");
                        for (int i = 0; i < results.size(); i++) {
                            results.get(i).setResultValue(new QueryResult(answers[i], queryTime, null));
                        }
                    } catch (final IOException e) {
                        for (final CallbackFutureImpl<QueryResult> result : results) {
                            result.setResultException(e);
                        }
                    }
                }
            });
            final CallbackFutureImpl<QueryResult> oldHandler = pendingRequests.putIfAbsent(pathKey, ret);
            if (oldHandler != null) {
                return oldHandler;
            }
            return ret;
        }
    }

    private static class SimpleMockBackend implements Backend {
        private int queryNr = 0;

        @Override
        public CallbackFuture<QueryResult> query(final QueryContext context) {
            final QueryRequest request = context.getRequest();
            final List<String> path = request.getPath();
            final List<String> remainingPath = path.subList(context.getPathLength(), path.size());
            try {
                final String requestCommand = "/" + StringUtils.join(remainingPath, '/');
                final Date queryTime = new Date();
                System.out.println("-------------------------------------------------------------------------------------------------");
                System.out.println("Querying (" + (++queryNr) + "): " + requestCommand);
                System.out.println("-------------------------------------------------------------------------------------------------");
                final String result = Client.query(requestCommand);
                return new DirectResult<QueryResult>(new QueryResult(result.trim(), queryTime, null));
            } catch (final Throwable e) {
                throw new RuntimeException("Cannot call " + remainingPath, e);
            }
        }
    }

    public static void main(final String args[]) throws IOException, InterruptedException, ExecutionException {
        final QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.appendNativeBackend(PathDescription.createFromString("mock"), new BulkMockBackend());
        queryBuilder.appendRuleSet(ClassLoader.getSystemResourceAsStream("rules.xml"));

        final Query query = queryBuilder.build();

        System.out.println();
        System.out.println("-------------");
        System.out.println("Configuration");
        System.out.println("-------------");
        // System.out.println(query.dump());

        for (int i = 0; i < 1; i++) {
            final long startTime = System.currentTimeMillis();
            System.out.println();
            System.out.println("---------------");
            System.out.println("Anouncing Query");
            System.out.println("---------------");
            final CallbackFuture<QueryResult> response = query.query(QueryRequest.createRequest("modem/00AB123456/mainUpstreamIfc"));

            System.out.println();
            System.out.println("--------------");
            System.out.println("Taking Result");
            System.out.println("--------------");
            final QueryResult queryResult = response.get();

            final long endTime = System.currentTimeMillis();
            System.out.println();
            System.out.println("------");
            System.out.println("Result");
            System.out.println("------");
            System.out.println("Result: " + queryResult.getValue());
            System.out.println("Query-Time: " + (endTime - startTime) + " ms");
            System.out.println("Data-Age: " + (endTime - queryResult.getCreationDate().getTime()) + " ms");
            Thread.sleep(1000);
        }
        System.out.println(query.query(QueryRequest.createRequest("modem/00AB123456")).get().getValue());
        System.out.println(query.query(QueryRequest.createRequest("scope/cmtsXYZ")).get().getValue());
        System.out.println(query.query(QueryRequest.createRequest("modem")).get().getValue());
    }
}
