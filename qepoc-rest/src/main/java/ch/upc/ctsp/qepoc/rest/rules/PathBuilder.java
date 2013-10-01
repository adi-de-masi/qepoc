/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest.rules;

public interface PathBuilder {
    PathBuilder addConstEntry(final String componentName);

    PathBuilder addVariableEntry(final String variableName);

    PathBuilder createSubpath();

    PathBuilder createPatternEntry(final String pattern);
}