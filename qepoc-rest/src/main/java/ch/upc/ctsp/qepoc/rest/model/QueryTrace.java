/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * TODO: add type comment.
 * 
 */
@Data
public class QueryTrace {
    public static String dumpTrace(final QueryResult result) {
        final StringBuffer sb = new StringBuffer();
        dumpTrace(sb, 0, result.getTrace());
        return sb.toString();
    }

    private static void dumpTrace(final StringBuffer buffer, final int level, final QueryTrace trace) {
        for (int i = 0; i < level; i++) {
            buffer.append("  ");
        }
        buffer.append(trace.getNodeType());
        buffer.append(": ");
        buffer.append(trace.getQuery());
        buffer.append(" -> ");
        buffer.append(trace.getCurrentResult());
        buffer.append("\n");
        for (final QueryTrace childTrace : trace.getChildTraces()) {
            if (!"constant-entry".equals(childTrace.getNodeType())) {
                dumpTrace(buffer, level + 1, childTrace);
            }
        }
    }

    private String                 currentResult;
    private String                 query;
    private final List<QueryTrace> childTraces = new ArrayList<QueryTrace>();
    private String                 nodeType;
}
