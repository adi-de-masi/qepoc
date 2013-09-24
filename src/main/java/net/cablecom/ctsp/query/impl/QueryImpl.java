package net.cablecom.ctsp.query.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.cablecom.ctsp.query.Query;
import net.cablecom.ctsp.query.model.CallbackFuture;
import net.cablecom.ctsp.query.model.PathDescription;
import net.cablecom.ctsp.query.model.PathDescription.FixedPathComp;
import net.cablecom.ctsp.query.model.PathDescription.PathComp;
import net.cablecom.ctsp.query.model.PathDescription.VariablePathComp;
import net.cablecom.ctsp.query.model.QueryRequest;
import net.cablecom.ctsp.query.model.QueryResult;
import net.cablecom.ctsp.query.spi.Backend;

/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */

/**
 * TODO: add type comment.
 * 
 */
public class QueryImpl implements Query {
  private final FixedListNode rootNode = new FixedListNode(null);

  /*
   * (non-Javadoc)
   * 
   * @see net.cablecom.ctsp.query.Query#query(java.lang.String)
   */
  @Override
  public CallbackFuture<QueryResult> query(final QueryRequest request) {
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
        nextNode = (AbstractRegistryNode) ((VariableNode) currentNode).getSubNode();
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
    return callBackend((BackendNode) currentNode, request, parameters, pathLength);
  }

  public void registerBackend(final PathDescription path, final Backend backend) {
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
          final AbstractRegistryNode existingSubNode = (AbstractRegistryNode) variableNode.getSubNode();
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
    final Map<String, String> variables = createParameterMap(backendNode, parameters, pathLength);
    final CallbackFuture<QueryResult> result = backendNode.getBackend().query(request, variables, this);
    return result;
  }

  private AbstractRegistryNode createNextNode(final PathComp nextPathComp, final AbstractRegistryNode currentNode, final Backend backend,
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
      nextNode = new BackendNode(currentNode, variableNames.toArray(new String[variableNames.size()]), backend);
    }
    return nextNode;
  }

  private Map<String, String> createParameterMap(final BackendNode backendNode, final List<String> parameters, final int pathLength) {
    final String[] variableNames = backendNode.getVariableEntries();
    assert variableNames.length == parameters.size();
    final Map<String, String> variables = new HashMap<String, String>();
    variables.put("match-length", Integer.toString(pathLength));
    for (int i = 0; i < variableNames.length; i++) {
      final String name = variableNames[i];
      final String value = parameters.get(i);
      variables.put(name, value);
    }
    return variables;
  }

}
