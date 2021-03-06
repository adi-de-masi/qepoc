/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest.rules;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import ch.upc.ctsp.qepoc.rest.model.CallbackFuture;
import ch.upc.ctsp.qepoc.rest.model.CallbackFuture.CallbackHandler;
import ch.upc.ctsp.qepoc.rest.model.QueryRequest;
import ch.upc.ctsp.qepoc.rest.model.QueryResult;
import ch.upc.ctsp.qepoc.rest.model.QueryResult.Builder;
import ch.upc.ctsp.qepoc.rest.spi.CallbackFutureImpl;
import ch.upc.ctsp.qepoc.rest.spi.DirectResult;
import ch.upc.ctsp.qepoc.rest.spi.QueryContext;

/**
 * TODO: add type comment.
 * 
 */
public class RulesUtil {
    public interface ProcessAsyncResponse<R, C> {
        R processResponses(final List<C> components);
    }

    /**
     * @param componentEntry
     * @param request
     * @param parameters
     * @param query
     * @return
     */
    public static CallbackFuture<QueryResult> createComponentLookup(final ComponentEntry componentEntry, final QueryContext context) {
        if (componentEntry instanceof ConstComponentEntry) {
            final String componentName = ((ConstComponentEntry) componentEntry).getComponentName();
            return new DirectResult<QueryResult>(new QueryResult.Builder().value(componentName).traceNodeType("constant-entry").query(componentName)
                    .build());
        }
        if (componentEntry instanceof VariableComponentEntry) {
            final Map<String, String> parameters = context.getParameterMap();
            final String variableName = ((VariableComponentEntry) componentEntry).getVariableName();
            return new DirectResult<QueryResult>(new QueryResult.Builder().value(parameters.get(variableName)).traceNodeType("variable-entry")
                    .query("{" + variableName + "}").build());
        }
        if (componentEntry instanceof LookupComponentEntry) {
            return processLookup((LookupComponentEntry) componentEntry, context, false);
        }
        if (componentEntry instanceof PatternComponentEntry) {
            final PatternComponentEntry patternEntry = (PatternComponentEntry) componentEntry;

            return processComponents(patternEntry.getPatternVariables(), new ProcessAsyncResponse<CallbackFuture<QueryResult>, QueryResult>() {

                @Override
                public CallbackFuture<QueryResult> processResponses(final List<QueryResult> components) {
                    final QueryResult.Builder builder = new QueryResult.Builder();
                    for (final QueryResult queryResult : components) {
                        builder.appendResult(queryResult);
                    }
                    return new DirectResult<QueryResult>(builder.value(patternEntry.pattern.format(collectValuesAsArray(components)))
                            .traceNodeType("pattern-entry").query(patternEntry.pattern.toPattern()).build());
                }

            }, context);
        }
        throw new IllegalArgumentException("Component-Type " + componentEntry.getClass() + " not supported");
    }

    public static String decodePathPart(final String part) {
        if (part == null) {
            return "<null>";
        }
        try {
            return URLDecoder.decode(part, "utf-8");
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException("This VM doesnt support utf-8", e);
        }
    }

    public static String encodePath(final Iterable<String> path) {
        final StringBuffer sb = new StringBuffer();
        for (final String part : path) {
            sb.append("/");
            sb.append(encodePathPart(part));
        }
        return sb.toString();

    }

    public static String encodePathPart(final String part) {
        if (part == null) {
            return "<null>";
        }
        try {
            return URLEncoder.encode(part, "utf-8");
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException("This VM doesnt support utf-8", e);
        }
    }

    public static <R, V> CallbackFuture<R> processCallbacks(final List<CallbackFuture<V>> callbacks,
            final ProcessAsyncResponse<CallbackFuture<R>, V> processor) {
        final CallbackFutureImpl<R> ret = new CallbackFutureImpl<R>();
        final AtomicReference<CallbackFuture<R>> finalResultReference = new AtomicReference<CallbackFuture<R>>(null);
        ret.setPollerHandler(new Runnable() {

            @Override
            public void run() {
                try {
                    for (final CallbackFuture<V> callbackFuture : callbacks) {
                        callbackFuture.get();
                    }
                    final CallbackFuture<R> finalResult = finalResultReference.get();
                    assert finalResult != null;
                    finalResult.get();
                } catch (final InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (final ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        final int componentCount = callbacks.size();
        final ArrayList<V> componentValues = new ArrayList<V>(componentCount);
        while (componentValues.size() < componentCount) {
            componentValues.add(null);
        }
        final AtomicInteger returnedResults = new AtomicInteger(0);
        for (int i = 0; i < componentCount; i++) {
            final int index = i;
            final CallbackFuture<V> callbackFuture = callbacks.get(i);
            callbackFuture.registerCallback(new CallbackHandler<V>() {

                @Override
                public void handleException(final Throwable exception) {
                    ret.setResultException(exception);
                }

                @Override
                public void handleValue(final V value) {
                    synchronized (componentValues) {
                        final V oldValue = componentValues.set(index, value);
                        if (oldValue != null) {
                            throw new RuntimeException("Called handler for component " + index + " twice");
                        }
                        if (returnedResults.incrementAndGet() == componentCount) {

                            final CallbackFuture<R> finalResult = processor.processResponses(componentValues);
                            ret.takeResultFrom(finalResult);
                            finalResultReference.set(finalResult);
                        }
                    }
                }
            });
        }
        return ret;
    }

    public static <R> CallbackFuture<R> processComponents(final List<ComponentEntry> components,
            final ProcessAsyncResponse<CallbackFuture<R>, QueryResult> processor, final QueryContext context) {
        final List<CallbackFuture<QueryResult>> componentLookups = new ArrayList<CallbackFuture<QueryResult>>();
        for (final ComponentEntry componentEntry : components) {
            componentLookups.add(createComponentLookup(componentEntry, context));
        }
        return processCallbacks(componentLookups, processor);
    }

    public static CallbackFuture<QueryResult> processLookup(final LookupComponentEntry lookup, final QueryContext context, final boolean appendTail) {

        return processComponents(lookup.getLookupPath(), new ProcessAsyncResponse<CallbackFuture<QueryResult>, QueryResult>() {

            @Override
            public CallbackFuture<QueryResult> processResponses(final List<QueryResult> components) {
                final Builder resultBuilder = new QueryResult.Builder();
                resultBuilder.query(lookup.toString());
                resultBuilder.traceNodeType("lookup");
                for (final QueryResult queryResult : components) {
                    resultBuilder.appendResult(queryResult);
                }
                final List<String> requestPath = collectValues(components);
                final List<String> path = context.getRequest().getPath();
                if (appendTail && context.getPathLength() != path.size()) {
                    requestPath.addAll(path.subList(context.getPathLength(), path.size()));
                }
                try {
                    final QueryRequest executingRequest = new QueryRequest.Builder(context.getRequest()).path(requestPath).build();

                    return withResultBuilder(context.getExecutingQuery().query(executingRequest), resultBuilder);
                } catch (final Throwable t) {
                    throw new RuntimeException("Error resolving path " + encodePath(requestPath), t);
                }
            }

        }, context);
    }

    /**
     * @param query
     * @param age
     * @return
     */
    protected static CallbackFuture<QueryResult> withResultBuilder(final CallbackFuture<QueryResult> query, final Builder resultBuilder) {
        return new CallbackFuture<QueryResult>() {

            @Override
            public boolean cancel(final boolean mayInterruptIfRunning) {
                return query.cancel(mayInterruptIfRunning);
            }

            @Override
            public QueryResult get() throws InterruptedException, ExecutionException {
                return query.get();
            }

            @Override
            public QueryResult get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return query.get(timeout, unit);
            }

            @Override
            public boolean isCancelled() {
                return query.isCancelled();
            }

            @Override
            public boolean isDone() {
                return query.isDone();
            }

            @Override
            public void registerCallback(final CallbackFuture.CallbackHandler<QueryResult> handler) {
                query.registerCallback(new CallbackHandler<QueryResult>() {

                    @Override
                    public void handleException(final Throwable exception) {
                        handler.handleException(exception);
                    }

                    @Override
                    public void handleValue(final QueryResult value) {
                        handler.handleValue(createResult(value));
                    }
                });
            }

            private QueryResult createResult(final QueryResult value) {
                resultBuilder.appendResult(value);
                resultBuilder.value(value.getValue());
                return resultBuilder.build();
            }
        };
    }

    private static List<String> collectValues(final List<QueryResult> components) {
        final List<String> requestPath = new ArrayList<String>();
        for (final QueryResult resultComponent : components) {
            requestPath.add(resultComponent.getValue());
        }
        return requestPath;
    }

    private static String[] collectValuesAsArray(final List<QueryResult> components) {
        final String[] requestPath = new String[components.size()];
        int i = 0;
        for (final QueryResult resultComponent : components) {
            requestPath[i++] = resultComponent.getValue();
        }
        return requestPath;
    }

    private static Date findOldestAge(final List<QueryResult> components) {
        Date oldestPart = new Date();
        for (final QueryResult resultComponent : components) {
            if (resultComponent.getCreationDate().before(oldestPart)) {
                oldestPart = resultComponent.getCreationDate();
            }
        }
        return oldestPart;
    }

}
