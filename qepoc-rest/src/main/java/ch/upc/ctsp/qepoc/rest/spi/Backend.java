/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest.spi;

import java.util.Map;

import ch.upc.ctsp.qepoc.rest.Query;
import ch.upc.ctsp.qepoc.rest.model.CallbackFuture;
import ch.upc.ctsp.qepoc.rest.model.QueryRequest;
import ch.upc.ctsp.qepoc.rest.model.QueryResult;


/**
 * TODO: add type comment.
 * 
 */
public interface Backend {
  CallbackFuture<QueryResult> query(final QueryRequest request, final Map<String, String> parameters, final Query executingQuery);
}
