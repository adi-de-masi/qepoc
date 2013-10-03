/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest.spi;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import ch.upc.ctsp.qepoc.rest.Query;
import ch.upc.ctsp.qepoc.rest.model.QueryRequest;

@Data
public class QueryContext {
    private final QueryRequest request;
    private final List<String> parameterNames;
    private final List<String> parameterValues;
    private final Query        executingQuery;
    private final int          pathLength;

    public QueryContext(final QueryRequest request, final List<String> parameterNames, final List<String> parameterValues,
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
