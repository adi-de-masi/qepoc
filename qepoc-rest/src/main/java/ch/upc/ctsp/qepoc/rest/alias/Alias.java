/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest.alias;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import ch.upc.ctsp.qepoc.rest.Query;
import ch.upc.ctsp.qepoc.rest.model.CallbackFuture;
import ch.upc.ctsp.qepoc.rest.model.QueryRequest;
import ch.upc.ctsp.qepoc.rest.model.QueryResult;
import ch.upc.ctsp.qepoc.rest.spi.Backend;

import lombok.Data;

/**
 * TODO: add type comment.
 * 
 */
public class Alias implements Backend {
  public static class Builder {

  }

  private interface ComponentEntry {

  }

  @Data
  private static class ConstComponentEntry implements ComponentEntry {
    private String componentName;
  }

  @Data
  private static class LookupComponentEntry implements ComponentEntry {
    private List<ComponentEntry> lookupPath;
  }

  @Data
  private static class VariableComponentEntry implements ComponentEntry {
    private String variableName;
  }

  private final LookupComponentEntry lookup = new LookupComponentEntry();

  @Override
  public CallbackFuture<QueryResult> query(final QueryRequest request, final Map<String, String> parameters, final Query query) {
    return processLookup(lookup, request, parameters, query);
  }

  private CallbackFuture<QueryResult> processLookup(final LookupComponentEntry lookup, final QueryRequest request,
      final Map<String, String> parameters, final Query query) {
    final Collection<LookupComponentEntry> recursiveLookups = new ArrayList<Alias.LookupComponentEntry>();
    for (final ComponentEntry componentEntry : lookup.getLookupPath()) {
      if (componentEntry instanceof LookupComponentEntry) {
        recursiveLookups.add((LookupComponentEntry) componentEntry);
      }
    }
    if (recursiveLookups.isEmpty()) {
      final ch.upc.ctsp.qepoc.rest.model.QueryRequest.Builder builder = new QueryRequest.Builder(request);
      for (final ComponentEntry componentEntry : lookup.getLookupPath()) {
        if (componentEntry instanceof ConstComponentEntry) {
          builder.appendPathComp(((ConstComponentEntry) componentEntry).getComponentName());
        } else if (componentEntry instanceof VariableComponentEntry) {
          builder.appendPathComp(parameters.get(((VariableComponentEntry) componentEntry).getVariableName()));
        }
      }
    }
    // TODO Auto-generated method stub
    return null;
  }

}
