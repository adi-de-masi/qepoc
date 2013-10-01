/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest.rules;

import lombok.Data;

@Data class ConstComponentEntry implements ComponentEntry {
    private final String componentName;

    @Override
    public String toString() {
        return componentName;
    }
}