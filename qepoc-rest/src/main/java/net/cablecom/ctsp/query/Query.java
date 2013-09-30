/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package net.cablecom.ctsp.query;

import net.cablecom.ctsp.query.model.CallbackFuture;
import net.cablecom.ctsp.query.model.QueryRequest;
import net.cablecom.ctsp.query.model.QueryResult;

/**
 * TODO: add type comment.
 * 
 */
public interface Query {
  CallbackFuture<QueryResult> query(final QueryRequest request);
}
