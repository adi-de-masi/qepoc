/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest.spi;

import ch.upc.ctsp.qepoc.rest.model.CallbackFuture;
import ch.upc.ctsp.qepoc.rest.model.QueryResult;

/**
 * TODO: add type comment.
 * 
 */
public interface Backend {
    CallbackFuture<QueryResult> query(final QueryContext context);
}
