/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest.rules;

import ch.upc.ctsp.qepoc.rest.impl.BackendWrapper;
import ch.upc.ctsp.qepoc.rest.impl.VariableResolver;

/**
 * TODO: add type comment.
 * 
 */
public interface BackendBuilder {

    public BackendWrapper build(final VariableResolver resolver);

}