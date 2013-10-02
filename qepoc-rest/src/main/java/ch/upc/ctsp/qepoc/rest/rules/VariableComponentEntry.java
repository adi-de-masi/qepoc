/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest.rules;

import lombok.Data;

@Data class VariableComponentEntry implements ComponentEntry {
    private final String variableName;

    @Override
    public String toString() {
        return "{" + variableName + "}";
    }
}