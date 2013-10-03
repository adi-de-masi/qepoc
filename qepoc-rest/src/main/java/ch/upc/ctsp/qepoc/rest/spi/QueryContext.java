/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest.spi;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import ch.upc.ctsp.qepoc.rest.Query;
import ch.upc.ctsp.qepoc.rest.model.QueryRequest;

@Data
public class QueryContext {
    public static class Builder {
        private QueryRequest request;
        private List<String> parameterNames;
        private List<String> parameterValues;
        private Query        executingQuery;
        private int          pathLength;

        public Builder() {
        }

        public Builder(final QueryContext context) {
            request = context.request;
            parameterNames = context.parameterNames;
            parameterValues = context.parameterValues;
            executingQuery = context.executingQuery;
            pathLength = context.pathLength;
        }

        public QueryContext build() {
            return new QueryContext(request, parameterNames, parameterValues, executingQuery, pathLength);
        }

        public Builder parameterNames(final List<String> parameterNames) {
            this.parameterNames = Collections.unmodifiableList(parameterNames);
            return this;
        }

        /**
         * @param variableEntries
         * @return
         */
        public Builder parameterNames(final String[] variableEntries) {
            parameterNames = Arrays.asList(variableEntries);
            return this;
        }

        public Builder parameterValues(final List<String> parameterValues) {
            this.parameterValues = Collections.unmodifiableList(parameterValues);
            return this;
        }

        public Builder pathLength(final int pathLength) {
            this.pathLength = pathLength;
            return this;
        }

        public Builder query(final Query query) {
            executingQuery = query;
            return this;
        }

        public Builder request(final QueryRequest request) {
            this.request = request;
            return this;
        }

    }

    private final QueryRequest request;
    private final List<String> parameterNames;
    private final List<String> parameterValues;
    private final Query        executingQuery;
    private final int          pathLength;

    private QueryContext(final QueryRequest request, final List<String> parameterNames, final List<String> parameterValues,
            final Query executingQuery, final int pathLength) {
        assert parameterNames != null;
        assert parameterValues != null;
        assert parameterNames.size() == parameterValues.size();
        this.request = request;
        this.parameterNames = parameterNames;
        this.parameterValues = parameterValues;
        this.executingQuery = executingQuery;
        this.pathLength = pathLength;
    }

    public Map<String, String> getParameterMap() {
        final LinkedHashMap<String, String> ret = new LinkedHashMap<String, String>();
        final Iterator<String> valueIterator = parameterValues.iterator();
        final Iterator<String> nameIterator = parameterNames.iterator();
        while (valueIterator.hasNext() && nameIterator.hasNext()) {
            ret.put(nameIterator.next(), valueIterator.next());
        }
        return ret;
    }
}
