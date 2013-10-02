/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest.rules;

import java.text.MessageFormat;
import java.util.List;

import lombok.Data;

@Data class PatternComponentEntry implements ComponentEntry {
    final MessageFormat        pattern;
    private final List<ComponentEntry> patternVariables;

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append("[");
        sb.append(pattern.toPattern());
        if (!patternVariables.isEmpty()) {
            sb.append(":");
            boolean first = true;
            for (final ComponentEntry variable : patternVariables) {
                if (!first) {
                    sb.append(",");
                }
                sb.append(variable.toString());
                first = false;
            }
        }
        sb.append("]");
        return sb.toString();
    }
}