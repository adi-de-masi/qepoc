/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest.model;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import lombok.Data;

/**
 * TODO: add type comment.
 * 
 */
@Data
public class QueryResult {
    public static class Builder {
        private String     value;
        private Date       creationDate;
        private Date       validUntil;
        private QueryTrace queryTrace = new QueryTrace();

        public Builder appendResult(final QueryResult result) {
            final List<QueryTrace> childTraces = queryTrace.getChildTraces();
            if (childTraces.size() > 0 && childTraces.get(childTraces.size() - 1).equals(result.getTrace())) {
                return this;
            }
            childTraces.add(result.getTrace());
            if (validUntil == null || validUntil.after(result.getValidUntil())) {
                validUntil = result.getValidUntil();
            }
            if (creationDate == null || creationDate.before(result.getCreationDate())) {
                creationDate = result.getCreationDate();
            }
            return this;
        }

        public QueryResult build() {
            if (creationDate == null) {
                creationDate = new Date();
            }
            if (queryTrace.getNodeType() == null) {
                final StackTraceElement[] stackTrace = new Throwable().getStackTrace();
                final StackTraceElement traceElement = stackTrace[1];
                queryTrace.setNodeType(traceElement.getClassName());
            }
            return new QueryResult(value, creationDate, validUntil, queryTrace);
        }

        public Builder creationDate(final Date date) {
            this.creationDate = date;
            return this;
        }

        public Builder lifeTime(final TimeUnit timeUnit, final int unitCount) {
            this.validUntil = new Date(System.currentTimeMillis() + timeUnit.toMillis(unitCount));
            return this;
        }

        public Builder query(final String query) {
            queryTrace.setQuery(query);
            return this;
        }

        public Builder queryTrace(final QueryTrace queryTrace) {
            this.queryTrace = queryTrace;
            return this;
        }

        public Builder traceNodeType(final String type) {
            queryTrace.setNodeType(type);
            return this;
        }

        public Builder validUntil(final Date validUntil) {
            this.validUntil = validUntil;
            return this;
        }

        public Builder value(final String value) {
            this.value = value;
            return this;
        }
    }

    private final String     value;
    private final Date       creationDate;
    private final Date       validUntil;
    private final QueryTrace trace;

    // public QueryResult(final String value) {
    // this.value = value;
    // creationDate = new Date();
    // validUntil = null;
    // trace = new QueryTrace();
    // trace.setCurrentResult(value);
    // }

    private QueryResult(final String value, final Date creationDate, final Date validUntil, final QueryTrace trace) {
        this.value = value;
        this.creationDate = creationDate;
        this.validUntil = validUntil;
        this.trace = trace;
        trace.setCurrentResult(value);
    }

    public QueryResult cacheInstance() {
        final QueryTrace trace = new QueryTrace();
        trace.setNodeType("cached");
        trace.setQuery(this.trace.getQuery());
        return new QueryResult(value, creationDate, validUntil, trace);
    }

    public QueryResult withMaxCreationDate(final Date maxCreationDate) {
        if (maxCreationDate == null || maxCreationDate.after(creationDate)) {
            return this;
        }
        return new QueryResult(value, maxCreationDate, validUntil, trace);
    }
}
