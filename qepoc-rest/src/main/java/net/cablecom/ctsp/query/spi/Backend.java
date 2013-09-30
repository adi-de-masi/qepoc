/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package net.cablecom.ctsp.query.spi;

import java.util.Map;

import net.cablecom.ctsp.query.Query;
import net.cablecom.ctsp.query.model.CallbackFuture;
import net.cablecom.ctsp.query.model.QueryRequest;
import net.cablecom.ctsp.query.model.QueryResult;

/**
 * TODO: add type comment.
 * 
 */
public interface Backend {
  CallbackFuture<QueryResult> query(final QueryRequest request, final Map<String, String> parameters, final Query executingQuery);
}
