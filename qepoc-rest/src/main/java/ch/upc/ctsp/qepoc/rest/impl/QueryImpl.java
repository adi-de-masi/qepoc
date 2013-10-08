package ch.upc.ctsp.qepoc.rest.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.upc.ctsp.qepoc.rest.Query;
import ch.upc.ctsp.qepoc.rest.model.CallbackFuture;
import ch.upc.ctsp.qepoc.rest.model.CallbackFuture.CallbackHandler;
import ch.upc.ctsp.qepoc.rest.model.PathDescription;
import ch.upc.ctsp.qepoc.rest.model.PathDescription.FixedPathComp;
import ch.upc.ctsp.qepoc.rest.model.PathDescription.PathComp;
import ch.upc.ctsp.qepoc.rest.model.PathDescription.VariablePathComp;
import ch.upc.ctsp.qepoc.rest.model.QueryRequest;
import ch.upc.ctsp.qepoc.rest.model.QueryResult;
import ch.upc.ctsp.qepoc.rest.spi.Backend;
import ch.upc.ctsp.qepoc.rest.spi.CallbackFutureImpl;
import ch.upc.ctsp.qepoc.rest.spi.DirectResult;
import ch.upc.ctsp.qepoc.rest.spi.QueryContext;
import ch.upc.ctsp.qepoc.rest.spi.QueryContext.Builder;

/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */

/**
 * TODO: add type comment.
 * 
 */
public class QueryImpl implements Query {
    private final FixedListNode                            rootNode     = new FixedListNode(null);
    private final Map<String, QueryResult>                 oldResults   = new HashMap<String, QueryResult>();
    private final Map<String, CallbackFuture<QueryResult>> pendingQuery = new HashMap<String, CallbackFuture<QueryResult>>();

    /**
     * @return
     * 
     */
    public String dump() {
        return rootNode.dump();

    }

    /*
     * (non-Javadoc)
     * 
     * @see net.cablecom.ctsp.query.Query#query(java.lang.String)
     */
    @Override
    public CallbackFuture<QueryResult> query(final QueryRequest request) {
        try {
            final String key = createKeyFromPath(request.getPath());
            final CallbackFutureImpl<QueryResult> ret = new CallbackFutureImpl<QueryResult>();
            synchronized (oldResults) {
                final QueryResult oldResult = oldResults.get(key);
                if (oldResult != null && oldResult.getCreationDate().after(request.getAllowedSince())) {
                    System.out.println("Cached result for " + key + ": " + oldResult.getValue());
                    return new DirectResult<QueryResult>(oldResult);
                }
                final CallbackFuture<QueryResult> pendingResult = pendingQuery.get(key);
                if (pendingResult != null) {
                    System.out.println("Pending Query for " + key);
                    return pendingResult;
                }
                pendingQuery.put(key, ret);
            }
            System.out.println("Query for " + key);
            AbstractRegistryNode currentNode = rootNode;
            final List<String> path = request.getPath();
            final List<String> parameters = new ArrayList<String>();
            int pathLength = 0;
            for (final String pathComp : path) {
                pathLength += 1;
                final AbstractRegistryNode nextNode;
                if (currentNode instanceof FixedListNode) {
                    nextNode = ((FixedListNode) currentNode).getSubNodes().get(pathComp);
                } else if (currentNode instanceof VariableNode) {
                    nextNode = ((VariableNode) currentNode).getSubNode();
                    parameters.add(pathComp);
                } else if (currentNode instanceof BackendNode) {
                    return callBackend((BackendNode) currentNode, request, parameters, pathLength - 1);
                } else {
                    throw new RuntimeException("Unknown Node Type " + currentNode);
                }
                if (nextNode == null) {
                    throw new RuntimeException("Cannot resolve Path " + path);
                }
                currentNode = nextNode;
            }
            ret.takeResultFrom(callBackend((BackendNode) currentNode, request, parameters, pathLength));
            return ret;
        } catch (final Throwable t) {
            throw new RuntimeException("Exception in Path " + request.getPath(), t);
        }
    }

    public void registerBackend(final PathDescription path, final Backend backend) {
        registerBackend(path, new BackendWrapper(path.getVariableNames(), backend));
    }

    public void registerBackend(final PathDescription path, final BackendWrapper backend) {
        AbstractRegistryNode currentNode = rootNode;

        final PathComp[] components = path.getComponents();
        final List<String> variableNames = new ArrayList<String>();
        for (int i = 0; i < components.length; i++) {
            final PathComp currentPathComp = components[i];
            final PathComp nextPathComp = i < components.length - 1 ? components[i + 1] : null;
            if (currentPathComp instanceof FixedPathComp) {
                final FixedPathComp fixedPathComp = (FixedPathComp) currentPathComp;
                if (currentNode instanceof FixedListNode) {
                    final Map<String, AbstractRegistryNode> subNodes = ((FixedListNode) currentNode).getSubNodes();
                    final AbstractRegistryNode existingNextNode = subNodes.get(fixedPathComp.getValue());
                    if (existingNextNode != null) {
                        currentNode = existingNextNode;
                        continue;
                    }
                    final AbstractRegistryNode nextNode = createNextNode(nextPathComp, currentNode, backend, variableNames);
                    subNodes.put(fixedPathComp.getValue(), nextNode);
                    currentNode = nextNode;
                } else {
                    throw new RuntimeException("Error at " + currentNode.describePath() + " while adding path " + path);
                }
            } else if (currentPathComp instanceof VariablePathComp) {
                if (currentNode instanceof VariableNode) {
                    final VariableNode variableNode = (VariableNode) currentNode;
                    variableNames.add(((VariablePathComp) currentPathComp).getVariableName());
                    final AbstractRegistryNode existingSubNode = variableNode.getSubNode();
                    if (existingSubNode != null) {
                        currentNode = existingSubNode;
                        continue;
                    }
                    final AbstractRegistryNode newSubNode = createNextNode(nextPathComp, currentNode, backend, variableNames);
                    variableNode.setSubNode(newSubNode);
                    currentNode = newSubNode;
                } else {
                    throw new RuntimeException("Error at " + currentNode.describePath() + " while adding path " + path);
                }
            }
        }
    }

    private CallbackFuture<QueryResult> callBackend(final BackendNode backendNode, final QueryRequest request, final List<String> parameters,
            final int pathLength) {
        final Builder contextBuilder = new QueryContext.Builder().request(request).parameterValues(parameters).pathLength(pathLength).query(this);
        final CallbackFuture<QueryResult> result = backendNode.getWrapper().call(contextBuilder);
        result.registerCallback(new CallbackHandler<QueryResult>() {

            @Override
            public void handleException(final Throwable exception) {
                final String key = createKeyFromPath(request.getPath());
                synchronized (oldResults) {
                    pendingQuery.remove(key);
                }
            }

            @Override
            public void handleValue(final QueryResult value) {
                final String key = createKeyFromPath(request.getPath());
                synchronized (oldResults) {
                    final QueryResult cachedResult = oldResults.get(key);
                    if (cachedResult == null || cachedResult.getCreationDate().before(value.getCreationDate())) {
                        oldResults.put(key, value);
                    }
                    pendingQuery.remove(key);
                }
            }
        });
        return result;
    }

    private String createKeyFromPath(final List<String> path) {
        final StringBuffer sb = new StringBuffer();
        for (final String part : path) {
            sb.append("/");
            try {
                sb.append(URLEncoder.encode(part, "utf-8"));
            } catch (final UnsupportedEncodingException e) {
                throw new RuntimeException("This VM doesnt support utf-8", e);
            }
        }
        return sb.toString();
    }

    private AbstractRegistryNode createNextNode(final PathComp nextPathComp, final AbstractRegistryNode currentNode, final BackendWrapper backend,
            final List<String> variableNames) {
        final AbstractRegistryNode nextNode;
        if (nextPathComp != null) {
            if (nextPathComp instanceof FixedPathComp) {
                nextNode = new FixedListNode(currentNode);
            } else if (nextPathComp instanceof VariablePathComp) {
                nextNode = new VariableNode(currentNode);
            } else {
                throw new RuntimeException("Unsupported path comp " + nextPathComp);
            }
        } else {
            nextNode = new BackendNode(currentNode, backend);
        }
        return nextNode;
    }
}
