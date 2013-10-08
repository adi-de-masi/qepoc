/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest.impl;

import ch.upc.ctsp.qepoc.rest.rules.PathBuilder;

/**
 * TODO: add type comment.
 * 
 */
public interface VariableResolver {
    void appendPath(final PathBuilder builder, final String variableName, final String[] parameterNames);
}
