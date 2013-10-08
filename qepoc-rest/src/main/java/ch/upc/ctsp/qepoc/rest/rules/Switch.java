/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest.rules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import lombok.Data;
import ch.upc.ctsp.qepoc.rest.impl.BackendWrapper;
import ch.upc.ctsp.qepoc.rest.impl.VariableResolver;
import ch.upc.ctsp.qepoc.rest.model.CallbackFuture;
import ch.upc.ctsp.qepoc.rest.model.PathDescription;
import ch.upc.ctsp.qepoc.rest.model.QueryResult;
import ch.upc.ctsp.qepoc.rest.rules.RulesUtil.ProcessAsyncResponse;
import ch.upc.ctsp.qepoc.rest.spi.Backend;
import ch.upc.ctsp.qepoc.rest.spi.DirectResult;
import ch.upc.ctsp.qepoc.rest.spi.QueryContext;

/**
 * TODO: add type comment.
 * 
 */
public class Switch implements Backend {
    public static class Builder implements BackendBuilder {
        private final List<CaseBuilder> cases = new ArrayList<Switch.CaseBuilder>();

        public CaseBuilder appendCase() {
            final CaseBuilder caseBuilder = new CaseBuilder();
            cases.add(caseBuilder);
            return caseBuilder;
        }

        /*
         * (non-Javadoc)
         * 
         * @see ch.upc.ctsp.qepoc.rest.rules.BackendBuilder#build()
         */
        @Override
        public BackendWrapper build(final VariableResolver variableResolver) {
            final List<ConditionalEntry> conditionalEntries = new ArrayList<Switch.ConditionalEntry>();
            for (final CaseBuilder singleCase : cases) {
                final ConditionalEntry conditionalEntry = singleCase.build(variableResolver);
                conditionalEntries.add(conditionalEntry);

            }
            if (conditionalEntries.size() == 1 && conditionalEntries.get(0).getConditions().isEmpty()) {
                return conditionalEntries.get(0).getValue();
            }
            final Switch sw = new Switch(conditionalEntries);
            return new BackendWrapper(null, sw);
        }
    }

    public static class CaseBuilder {
        private final List<ConditionBuilder> conditionBuilders = new ArrayList<Switch.ConditionBuilder>();
        private Backend                      backend;
        private String[]                     variableNames;
        private BackendBuilder               backendBuilder;

        public ConditionBuilder appendCondition() {
            final ConditionBuilder ret = new ConditionBuilder();
            conditionBuilders.add(ret);
            return ret;
        }

        public CaseBuilder backend(final Backend backend) {
            this.backend = backend;
            return this;
        }

        public CaseBuilder backendBuilder(final BackendBuilder backendBuilder) {
            this.backendBuilder = backendBuilder;
            return this;
        }

        public CaseBuilder path(final PathDescription path) {
            variableNames = path.getVariableNames();
            return this;
        }

        private ConditionalEntry build(final VariableResolver variableResolver) {
            final ArrayList<CompareCondition> conditions = new ArrayList<CompareCondition>();
            for (final ConditionBuilder conditionBuilder : conditionBuilders) {
                conditions.add(conditionBuilder.build());
            }
            BackendWrapper backendWrapper;
            if (backend == null) {
                backendWrapper = backendBuilder.build(variableResolver);
            } else {
                backendWrapper = new BackendWrapper(variableNames, backend);
            }
            return new ConditionalEntry(Collections.unmodifiableList(conditions), backendWrapper);
        }
    }

    public static class ConditionBuilder {
        private OperandBuilder op1Builder;
        private OperandBuilder op2Builder;

        public ConditionBuilder() {
        }

        @SuppressWarnings("rawtypes")
        public PathBuilder createOp1() {
            op1Builder = new OperandBuilder();
            return op1Builder;
        }

        @SuppressWarnings("rawtypes")
        public PathBuilder createOp2() {
            op2Builder = new OperandBuilder();
            return op2Builder;
        }

        private CompareCondition build() {
            return new CompareCondition(CompareOperator.EQUALS, op1Builder.build(), op2Builder.build());
        }
    }

    @Data
    private static class CompareCondition {
        private final CompareOperator operator;
        private final ComponentEntry  op1;
        private final ComponentEntry  op2;
    }

    @Data
    private static class ConditionalEntry {
        private final List<CompareCondition> conditions;
        private final BackendWrapper         value;
    }

    private static class OperandBuilder extends DefaultPathBuilder<OperandBuilder> {
        private ComponentEntry build() {
            return new LookupComponentEntry(path);
        }
    }

    enum CompareOperator {
        EQUALS
    }

    private final List<ConditionalEntry> conditionalEntries;

    /**
     * @param defaultWrapper
     * @param conditionalEntries
     */
    public Switch(final List<ConditionalEntry> conditionalEntries) {
        this.conditionalEntries = conditionalEntries;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.upc.ctsp.qepoc.rest.spi.Backend#query(ch.upc.ctsp.qepoc.rest.spi.QueryContext)
     */
    @Override
    public CallbackFuture<QueryResult> query(final QueryContext context) {
        final List<CallbackFuture<Boolean>> conditionResults = new ArrayList<CallbackFuture<Boolean>>();
        for (final ConditionalEntry conditionalEntry : conditionalEntries) {
            conditionResults.add(resolveCondition(conditionalEntry, context));
        }
        return RulesUtil.processCallbacks(conditionResults, new ProcessAsyncResponse<CallbackFuture<QueryResult>, Boolean>() {
            @Override
            public CallbackFuture<QueryResult> processResponses(final List<Boolean> components) {
                final int matchingIndex = components.indexOf(Boolean.TRUE);
                if (matchingIndex >= 0) {
                    return call(conditionalEntries.get(matchingIndex).getValue(), context);
                } else {
                    return new DirectResult<QueryResult>(new QueryResult(null));
                }
            }
        });
    }

    /**
     * Executing query to a {@link Backend}
     * 
     * @param backendWrapper
     *            Definition of this {@link Backend}
     * @param context
     *            query-context
     * @return result of query
     */
    protected CallbackFuture<QueryResult> call(final BackendWrapper backendWrapper, final QueryContext context) {
        return backendWrapper.call(new QueryContext.Builder(context));
    }

    /**
     * @param conditionalEntry
     * @param context
     * @return
     */
    private CallbackFuture<Boolean> resolveCondition(final ConditionalEntry conditionalEntry, final QueryContext context) {
        return resolveCondition(conditionalEntry.getConditions(),
                new QueryContext.Builder(context).parameterNames(conditionalEntry.getValue().getVariableEntries()).build());
    }

    /**
     * @param conditions
     * @param request
     * @param parameters
     * @param query
     * @return
     */
    private CallbackFuture<Boolean> resolveCondition(final List<CompareCondition> conditions, final QueryContext context) {
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
                return resolveCondition(conditions.subList(1, conditions.size()), context);
            }
        }, context);
    }
}
