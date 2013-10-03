/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest.impl;

import lombok.Data;
import ch.upc.ctsp.qepoc.rest.model.CallbackFuture;
import ch.upc.ctsp.qepoc.rest.model.QueryResult;
import ch.upc.ctsp.qepoc.rest.spi.Backend;
import ch.upc.ctsp.qepoc.rest.spi.QueryContext;

/**
 * TODO: add type comment.
 * 
 */
@Data
public class BackendWrapper {
    private final String[] variableEntries;
    private final Backend  backend;

    public CallbackFuture<QueryResult> call(final QueryContext.Builder contextBuilder) {
        return backend.query(contextBuilder.parameterNames(getVariableEntries()).build());
    }

}
