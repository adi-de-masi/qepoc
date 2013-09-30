/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest.alias;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import lombok.Data;
import ch.upc.ctsp.qepoc.rest.Query;
import ch.upc.ctsp.qepoc.rest.model.CallbackFuture;
import ch.upc.ctsp.qepoc.rest.model.CallbackFuture.CallbackHandler;
import ch.upc.ctsp.qepoc.rest.model.PathDescription;
import ch.upc.ctsp.qepoc.rest.model.QueryRequest;
import ch.upc.ctsp.qepoc.rest.model.QueryResult;
import ch.upc.ctsp.qepoc.rest.spi.Backend;
import ch.upc.ctsp.qepoc.rest.spi.CallbackFutureImpl;
import ch.upc.ctsp.qepoc.rest.spi.DirectResult;

/**
 * TODO: add type comment.
 * 
 */
public class Alias implements Backend {

    public static class Builder implements PathBuilder {
        private final List<ComponentEntry> path = new ArrayList<Alias.ComponentEntry>();

        @Override
        public Builder addConstEntry(final String componentName) {
            path.add(new ConstComponentEntry(componentName));
            return this;
        }

        @Override
        public Builder addVariableEntry(final String variableName) {
            path.add(new VariableComponentEntry(variableName));
            return this;
        }

        public Alias build() {
            return new Alias(new LookupComponentEntry(path));
        }

        @Override
        public PathBuilder createPatternEntry(final String pattern) {
            final Builder builder = new Builder();
            path.add(new PatternComponentEntry(new MessageFormat(pattern), builder.path));
            return builder;
        }

        @Override
        public PathBuilder createSubpath() {
            final Builder builder = new Builder();
            path.add(new LookupComponentEntry(builder.path));
            return builder;
        }
    }

    public static interface PathBuilder {
        PathBuilder addConstEntry(final String componentName);

        PathBuilder addVariableEntry(final String variableName);

        PathBuilder createSubpath();

        PathBuilder createPatternEntry(final String pattern);
    }

    private interface ComponentEntry {

    }

    @Data
    private static class ConstComponentEntry implements ComponentEntry {
        private final String componentName;

        @Override
        public String toString() {
            return componentName;
        }
    }

    @Data
    private static class LookupComponentEntry implements ComponentEntry {
        private final List<ComponentEntry> lookupPath;

        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            builder.append('<');
            for (final ComponentEntry comp : lookupPath) {
                if (builder.length() > 0) {
                    builder.append("/");
                }
                builder.append(comp);
            }
            builder.append('>');
            return builder.toString();
        }
    }

    @Data
    private static class PatternComponentEntry implements ComponentEntry {
        private final MessageFormat        pattern;
        private final List<ComponentEntry> patternVariables;

        @Override
        public String toString() {
            final StringBuffer sb = new StringBuffer();
            sb.append("[");
            sb.append(pattern.toPattern());
            if (!patternVariables.isEmpty()) {
                sb.append(":");
                boolean first = true;
                for (final ComponentEntry variable : patternVariables) {
                    if (!first) {
                        sb.append(",");
                    }
                    sb.append(variable.toString());
                    first = false;
                }
            }
            sb.append("]");
            return sb.toString();
        }
    }

    private interface ProcessAsyncResponse<R, C> {
        R processResponses(final List<C> components);
    }

    @Data
    private static class VariableComponentEntry implements ComponentEntry {
        private final String variableName;

        @Override
        public String toString() {
            return "{" + variableName + "}";
        }
    }

    public static Map<PathDescription, Alias> parseProperties(final Properties props) {
        final HashMap<PathDescription, Alias> ret = new HashMap<PathDescription, Alias>();
        for (final Entry<Object, Object> aliasEntry : props.entrySet()) {
            final PathDescription targetPath = PathDescription.createFromString(aliasEntry.getKey().toString());
            final String rule = (String) aliasEntry.getValue();
            final Builder builder = new Builder();
            final LinkedList<PathBuilder> builderStack = new LinkedList<Alias.PathBuilder>();
            builderStack.add(builder);
            boolean patternMode = false;
            final StringBuilder currenName = new StringBuilder();
            for (int i = 0; i < rule.length(); i++) {
                final char currentChar = rule.charAt(i);
                switch (currentChar) {
                    case '/':
                        if (currenName.length() > 0) {
                            builderStack.getLast().addConstEntry(currenName.toString());
                        }
                        currenName.setLength(0);
                        break;
                    case '<':
                        builderStack.add(builderStack.getLast().createSubpath());
                        break;
                    case '>':
                        if (currenName.length() > 0) {
                            builderStack.getLast().addConstEntry(currenName.toString());
                        }
                        currenName.setLength(0);
                        builderStack.removeLast();
                        break;
                    case '[':
                        currenName.setLength(0);
                        patternMode = true;
                        break;
                    case ':':
                        patternMode = false;
                        builderStack.add(builderStack.getLast().createPatternEntry(currenName.toString()));
                        builderStack.add(builderStack.getLast().createSubpath());
                        currenName.setLength(0);
                        break;
                    case ',':
                        if (currenName.length() > 0) {
                            builderStack.getLast().addConstEntry(currenName.toString());
                        }
                        currenName.setLength(0);
                        builderStack.removeLast();
                        builderStack.add(builderStack.getLast().createSubpath());
                        break;
                    case ']':
                        if (currenName.length() > 0) {
                            builderStack.getLast().addConstEntry(currenName.toString());
                        }
                        currenName.setLength(0);
                        builderStack.removeLast();
                        builderStack.removeLast();
                        break;
                    default:
                        if (!patternMode) {
                            if (currentChar == '}') {
                                if (currenName.length() > 0) {
                                    builderStack.getLast().addVariableEntry(currenName.toString());
                                }
                                currenName.setLength(0);
                            } else if (currentChar != '{') {
                                currenName.append(currentChar);
                            }
                        } else {
                            currenName.append(currentChar);
                        }
                        break;
                }
            }
            if (currenName.length() > 0) {
                builderStack.getLast().addConstEntry(currenName.toString());
            }
            currenName.setLength(0);
            ret.put(targetPath, builder.build());
        }
        return ret;
    }

    private final LookupComponentEntry lookup;

    private Alias(final LookupComponentEntry lookup) {
        this.lookup = lookup;
    }

    @Override
    public CallbackFuture<QueryResult> query(final QueryRequest request, final Map<String, String> parameters, final Query query) {
        final int matchLength = Integer.parseInt(parameters.get("match-length"));
        final List<String> requestPath = request.getPath();
        final List<String> appendPath;
        if (matchLength != requestPath.size()) {
            appendPath = requestPath.subList(matchLength, requestPath.size());
        } else {
            appendPath = null;
        }
        return processLookup(lookup, request, parameters, query, appendPath);
    }

    @Override
    public String toString() {
        return String.valueOf(lookup);
    }

    /**
     * @param componentEntry
     * @param request
     * @param parameters
     * @param query
     * @return
     */
    private CallbackFuture<String> createComponentLookup(final ComponentEntry componentEntry, final QueryRequest request,
            final Map<String, String> parameters, final Query query) {
        if (componentEntry instanceof ConstComponentEntry) {
            return new DirectResult<String>(((ConstComponentEntry) componentEntry).getComponentName());
        }
        if (componentEntry instanceof VariableComponentEntry) {
            return new DirectResult<String>(parameters.get(((VariableComponentEntry) componentEntry).getVariableName()));
        }
        if (componentEntry instanceof LookupComponentEntry) {
            final CallbackFuture<QueryResult> processLookup = processLookup((LookupComponentEntry) componentEntry, request, parameters, query, null);
            return new CallbackFuture<String>() {

                @Override
                public boolean cancel(final boolean mayInterruptIfRunning) {
                    return processLookup.cancel(mayInterruptIfRunning);
                }

                @Override
                public String get() throws InterruptedException, ExecutionException {
                    return processLookup.get().getValue();
                }

                @Override
                public String get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                    return processLookup.get(timeout, unit).getValue();
                }

                @Override
                public boolean isCancelled() {
                    return processLookup.isCancelled();
                }

                @Override
                public boolean isDone() {
                    return processLookup.isDone();
                }

                @Override
                public void registerCallback(final CallbackFuture.CallbackHandler<String> handler) {
                    processLookup.registerCallback(new CallbackHandler<QueryResult>() {

                        @Override
                        public void handleException(final Throwable exception) {
                            handler.handleException(exception);
                        }

                        @Override
                        public void handleValue(final QueryResult value) {
                            handler.handleValue(value.getValue());
                        }
                    });
                }
            };
        }
        if (componentEntry instanceof PatternComponentEntry) {
            final PatternComponentEntry patternEntry = (PatternComponentEntry) componentEntry;

            return processComponents(patternEntry.getPatternVariables(), new ProcessAsyncResponse<CallbackFuture<String>, String>() {

                @Override
                public CallbackFuture<String> processResponses(final List<String> components) {
                    return new DirectResult<String>(patternEntry.pattern.format(components.toArray(new String[components.size()])));
                }

            }, request, parameters, query);
        }
        throw new IllegalArgumentException("Component-Type " + componentEntry.getClass() + " not supported");
    }

    private <R> CallbackFuture<R> processComponents(final List<ComponentEntry> components,
            final ProcessAsyncResponse<CallbackFuture<R>, String> processor, final QueryRequest request, final Map<String, String> parameters,
            final Query query) {
        final List<CallbackFuture<String>> componentLookups = new ArrayList<CallbackFuture<String>>();
        for (final ComponentEntry componentEntry : components) {
            componentLookups.add(createComponentLookup(componentEntry, request, parameters, query));
        }
        final CallbackFutureImpl<R> ret = new CallbackFutureImpl<R>();
        final AtomicReference<CallbackFuture<R>> finalResultReference = new AtomicReference<CallbackFuture<R>>(null);
        ret.setPollerHandler(new Runnable() {

            @Override
            public void run() {
                try {
                    for (final CallbackFuture<String> callbackFuture : componentLookups) {
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
        final int componentCount = componentLookups.size();
        final ArrayList<String> componentValues = new ArrayList<String>(componentCount);
        while (componentValues.size() < componentCount) {
            componentValues.add(null);
        }
        final AtomicInteger returnedResults = new AtomicInteger(0);
        for (int i = 0; i < componentCount; i++) {
            final int index = i;
            final CallbackFuture<String> callbackFuture = componentLookups.get(i);
            callbackFuture.registerCallback(new CallbackHandler<String>() {

                @Override
                public void handleException(final Throwable exception) {
                    ret.setResultException(exception);
                }

                @Override
                public void handleValue(final String value) {
                    synchronized (componentValues) {
                        final String oldValue = componentValues.set(index, value);
                        if (oldValue != null) {
                            throw new RuntimeException("Called handler for component " + index + " twice");
                        }
                        if (returnedResults.incrementAndGet() == componentCount) {

                            final CallbackFuture<R> finalResult = processor.processResponses(componentValues);

                            finalResult.registerCallback(new CallbackHandler<R>() {

                                @Override
                                public void handleException(final Throwable exception) {
                                    ret.setResultException(exception);
                                }

                                @Override
                                public void handleValue(final R value) {
                                    ret.setResultValue(value);
                                }
                            });
                            finalResultReference.set(finalResult);
                        }
                    }
                }
            });
        }
        return ret;
    }

    private CallbackFuture<QueryResult> processLookup(final LookupComponentEntry lookup, final QueryRequest request,
            final Map<String, String> parameters, final Query query, final List<String> appendPath) {

        return processComponents(lookup.getLookupPath(), new ProcessAsyncResponse<CallbackFuture<QueryResult>, String>() {

            @Override
            public CallbackFuture<QueryResult> processResponses(final List<String> components) {
                if (appendPath != null) {
                    components.addAll(appendPath);
                }
                final QueryRequest request2 = new QueryRequest.Builder(request).path(components).build();
                final CallbackFuture<QueryResult> finalResult = query.query(request2);
                return finalResult;
            }

        }, request, parameters, query);
    }
}
