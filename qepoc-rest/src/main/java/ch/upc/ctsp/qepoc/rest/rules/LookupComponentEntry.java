/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest.rules;

import java.util.List;

import lombok.Data;

@Data class LookupComponentEntry implements ComponentEntry {
    private final List<ComponentEntry> lookupPath;

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append('<');
        for (final ComponentEntry comp : lookupPath) {
            if (builder.length() > 0) {
                builder.append("/");
            }
            builder.append(comp);
        }
        builder.append('>');
        return builder.toString();
    }
}