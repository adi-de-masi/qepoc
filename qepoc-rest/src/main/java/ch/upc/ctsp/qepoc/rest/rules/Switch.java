/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest.rules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import lombok.Data;
import ch.upc.ctsp.qepoc.rest.Query;
import ch.upc.ctsp.qepoc.rest.model.CallbackFuture;
import ch.upc.ctsp.qepoc.rest.model.QueryRequest;
import ch.upc.ctsp.qepoc.rest.model.QueryResult;
import ch.upc.ctsp.qepoc.rest.rules.RulesUtil.ProcessAsyncResponse;
import ch.upc.ctsp.qepoc.rest.spi.Backend;
import ch.upc.ctsp.qepoc.rest.spi.DirectResult;

/**
 * TODO: add type comment.
 * 
 */
public class Switch implements Backend {
    public static class ConditionBuilder {
        public ConditionBuilder(){
            
        }
    }

    @Data
    private static class CompareCondition {
        private CompareOperator operator;
        private ComponentEntry  op1;
        private ComponentEntry  op2;
    }

    @Data
    private static class ConditionalEntry {
        private List<CompareCondition> condition;
        private Backend                value;
    }

    enum CompareOperator {
        EQUALS
    }

    private final List<ConditionalEntry> conditionalEntries = new ArrayList<ConditionalEntry>();
    private Backend                      defaultValue;

    @Override
    public CallbackFuture<QueryResult> query(final QueryRequest request, final Map<String, String> parameters, final Query executingQuery) {
        final List<CallbackFuture<Boolean>> conditionResults = new ArrayList<CallbackFuture<Boolean>>();
        for (final ConditionalEntry conditionalEntry : conditionalEntries) {
            final List<CompareCondition> condition = conditionalEntry.getCondition();
            conditionResults.add(resolveCondition(condition, request, parameters, executingQuery));
        }
        return RulesUtil.processCallbacks(conditionResults, new ProcessAsyncResponse<CallbackFuture<QueryResult>, Boolean>() {
            @Override
            public CallbackFuture<QueryResult> processResponses(final List<Boolean> components) {
                final int matchingIndex = components.indexOf(Boolean.TRUE);
                if (matchingIndex >= 0) {
                    return conditionalEntries.get(matchingIndex).getValue().query(request, parameters, executingQuery);
                } else if (defaultValue != null) {
                    return defaultValue.query(request, parameters, executingQuery);
                } else {
                    return new DirectResult<QueryResult>(new QueryResult(null));
                }
            }
        });
    }

    /**
     * @param conditions
     * @param request
     * @param parameters
     * @param query
     * @return
     */
    private CallbackFuture<Boolean> resolveCondition(final List<CompareCondition> conditions, final QueryRequest request,
            final Map<String, String> parameters, final Query query) {
        if (conditions.isEmpty()) {
            return new DirectResult<Boolean>(Boolean.TRUE);
        }
        final CompareCondition condition = conditions.get(0);
        final List<ComponentEntry> conditionParameters = Arrays.asList(condition.getOp1(), condition.getOp2());
        return RulesUtil.processComponents(conditionParameters, new ProcessAsyncResponse<CallbackFuture<Boolean>, QueryResult>() {

            @Override
            public CallbackFuture<Boolean> processResponses(final List<QueryResult> components) {
                final QueryResult op1 = components.get(0);
                final QueryResult op2 = components.get(1);
                final boolean compareOk = op1.equals(op2);
                if (!compareOk) {
                    return new DirectResult<Boolean>(Boolean.FALSE);
                }
                if (conditions.size() == 1) {
                    return new DirectResult<Boolean>(Boolean.TRUE);
                }
                return resolveCondition(conditions.subList(1, conditions.size()), request, parameters, query);
            }
        }, request, parameters, query);
    }
}