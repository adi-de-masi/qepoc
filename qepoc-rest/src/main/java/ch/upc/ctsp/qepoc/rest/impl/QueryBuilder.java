/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXB;

import ch.upc.ctsp.qepoc.rest.Query;
import ch.upc.ctsp.qepoc.rest.config.AttributeComplexType;
import ch.upc.ctsp.qepoc.rest.config.ConditionalComplexType;
import ch.upc.ctsp.qepoc.rest.config.ReferenceComplexType;
import ch.upc.ctsp.qepoc.rest.config.RuleCollection;
import ch.upc.ctsp.qepoc.rest.config.RuleSet;
import ch.upc.ctsp.qepoc.rest.model.PathDescription;
import ch.upc.ctsp.qepoc.rest.model.PathDescription.FixedPathComp;
import ch.upc.ctsp.qepoc.rest.model.PathDescription.PathComp;
import ch.upc.ctsp.qepoc.rest.model.PathDescription.VariablePathComp;
import ch.upc.ctsp.qepoc.rest.rules.Alias;
import ch.upc.ctsp.qepoc.rest.rules.BackendBuilder;
import ch.upc.ctsp.qepoc.rest.rules.PathBuilder;
import ch.upc.ctsp.qepoc.rest.rules.Switch;
import ch.upc.ctsp.qepoc.rest.rules.Switch.Builder;
import ch.upc.ctsp.qepoc.rest.spi.Backend;

/**
 * TODO: add type comment.
 * 
 */
public class QueryBuilder {

    private final Map<PathDescription, Switch.Builder> builders         = new LinkedHashMap<PathDescription, Switch.Builder>();
    private final Map<PathDescription, Switch.Builder> iterableBuilders = new LinkedHashMap<PathDescription, Switch.Builder>();

    public QueryBuilder appendNativeBackend(final PathDescription path, final Backend backend) {
        getBuilderForPath(builders, path).appendCase().path(path).backend(backend);
        return this;
    }

    public QueryBuilder appendRuleSet(final InputStream inputStream) {
        return appendRuleSet(JAXB.unmarshal(inputStream, RuleSet.class));
    }

    public QueryBuilder appendRuleSet(final RuleSet ruleSet) {
        PathDescription currentPath;
        if (ruleSet.getPath() == null) {
            currentPath = new PathDescription.Builder().build();
        } else {
            currentPath = PathDescription.createFromString(ruleSet.getPath());
        }
        fillBuilders(ruleSet, currentPath, new ArrayList<String>());
        return this;
    }

    public Query build() {
        final QueryImpl impl = new QueryImpl();
        for (final Entry<PathDescription, Builder> builderEntry : builders.entrySet()) {
            final PathDescription path = builderEntry.getKey();
            impl.registerBackend(path, builderEntry.getValue().build(makeVariableResolver(path)));
        }
        for (final Entry<PathDescription, Builder> builderEntry : iterableBuilders.entrySet()) {
            final PathDescription path = builderEntry.getKey();
            impl.registerIterableBackend(path, builderEntry.getValue().build(makeVariableResolver(path)));
        }
        System.out.println(impl.dump());
        return impl;
    }

    /**
     * @param newCase
     * @param conditions
     * @param parentPath
     * @param parameters
     */
    private void appendConditions(final Switch.CaseBuilder newCase, final List<String> conditions, final PathDescription parentPath) {
        final List<String> parameters = Arrays.asList(parentPath.getVariableNames());
        for (final String conditionString : conditions) {
            final Switch.ConditionBuilder condition = newCase.appendCondition();
            final String[] conditionParts = conditionString.split("==");
            if (conditionParts.length != 2) {
                throw new RuntimeException("Error in Condition " + conditionString);
            }
            fillOperation(condition.createOp1(), conditionParts[0].trim(), parentPath, parameters);
            fillOperation(condition.createOp2(), conditionParts[1].trim(), parentPath, parameters);
        }
    }

    private void appendVariablePath(final PathBuilder builder, final String variableName, final PathDescription parentPath,
            final List<String> parameters) {
        if (parameters.contains(variableName)) {
            // parameter
            builder.addVariableEntry(variableName);
        } else {
            // variable in current context
            // -> insert absolute reference
            final PathBuilder subpath = builder.createSubpath();
            for (final PathComp currentPathComp : parentPath.getComponents()) {
                if (currentPathComp instanceof FixedPathComp) {
                    subpath.addConstEntry(((FixedPathComp) currentPathComp).getValue());
                } else if (currentPathComp instanceof VariablePathComp) {
                    subpath.addVariableEntry(((VariablePathComp) currentPathComp).getVariableName());
                }
            }
            subpath.addConstEntry(variableName);
        }
    }

    /**
     * @param iterableReference
     * @param path
     * @param iterablePlace
     * @param b
     * @return
     */
    private BackendBuilder buildAliasBuilder(final String reference, final PathDescription parentPath, final PathDescription attributePath,
            final boolean isReference) {
        final List<String> parameters = Arrays.asList(attributePath.getVariableNames());
        final Alias.Builder aliasBuilder = new Alias.Builder();
        if (isReference) {
            aliasBuilder.appendTail();
        }
        return aliasBuilder.parameterNames(parameters).reference(reference).relativePath(parentPath);
    }

    private PathDescription createParentPath(final PathDescription path) {
        final PathDescription.Builder builder = new PathDescription.Builder();
        final PathComp[] components = path.getComponents();
        for (int i = 0; i < components.length - 1; i++) {
            builder.appendComponent(components[i]);
        }
        return builder.build();
    }

    /**
     * @param rules
     * @param path
     * @param conditions
     */
    private void fillBuilders(final RuleCollection rules, final PathDescription path, final List<String> conditions) {
        for (final ConditionalComplexType conditional : rules.getConditional()) {
            final ArrayList<String> newConditions = new ArrayList<String>(conditions);
            newConditions.add(conditional.getCondition());
            fillBuilders(conditional, path, newConditions);
        }
        for (final RuleSet ruleSet : rules.getRuleSet()) {
            final PathDescription ruleSetPath = new PathDescription.Builder(path).appendString(ruleSet.getPath()).build();
            fillBuilders(ruleSet, ruleSetPath, conditions);
            final String iterableReference = ruleSet.getIterableReference();
            if (iterableReference != null) {
                final PathDescription iterablePlace = createParentPath(ruleSetPath);
                final Switch.CaseBuilder newCase = getBuilderForPath(iterableBuilders, iterablePlace).appendCase();
                newCase.path(iterablePlace).backendBuilder(buildAliasBuilder(iterableReference, path, iterablePlace, false));
            }
        }
        for (final AttributeComplexType attribute : rules.getAttribute()) {
            final PathDescription attributePath = new PathDescription.Builder(path).appendString(attribute.getName()).build();
            final Switch.Builder builder = getBuilderForPath(builders, attributePath);
            final Switch.CaseBuilder newCase = builder.appendCase();
            newCase.path(attributePath).backendBuilder(buildAliasBuilder(attribute.getReference(), path, attributePath, false));
            appendConditions(newCase, conditions, path);
        }
        for (final ReferenceComplexType reference : rules.getReference()) {
            final PathDescription referencePath = new PathDescription.Builder(path).appendString(reference.getName()).build();
            final Switch.Builder builder = getBuilderForPath(builders, referencePath);
            final Switch.CaseBuilder newCase = builder.appendCase();
            newCase.path(referencePath).backendBuilder(buildAliasBuilder(reference.getReference(), path, referencePath, true));
            appendConditions(newCase, conditions, path);
        }
    }

    private void fillOperation(final PathBuilder opBuilder, final String opString, final PathDescription parentPath, final List<String> parameters) {
        if (opString.startsWith("{") && opString.endsWith("}")) {
            final String variableName = opString.substring(1, opString.length() - 1);
            appendVariablePath(opBuilder, variableName, parentPath, parameters);
        } else {
            opBuilder.addConstEntry(opString);
        }
    }

    private Switch.Builder getBuilderForPath(final Map<PathDescription, Builder> builders, final PathDescription attributePath) {
        final PathDescription normalPath = attributePath.normalize();
        final Switch.Builder existingBuilder = builders.get(normalPath);
        if (existingBuilder != null) {
            return existingBuilder;
        }
        final Switch.Builder newBuilder = new Switch.Builder();
        builders.put(normalPath, newBuilder);
        return newBuilder;
    }

    private VariableResolver makeVariableResolver(final PathDescription path) {
        return new VariableResolver() {

            @Override
            public void appendPath(final PathBuilder builder, final String variableName, final String[] parameterNames) {
                if (Arrays.asList(parameterNames).contains(variableName)) {
                    // Parameter with given name found -> add and return
                    builder.addVariableEntry(variableName);
                    return;
                }
                final PathDescription resolveablePath = path.replaceParameterNames(parameterNames);
                final PathComp[] components = resolveablePath.getComponents();
                for (int i = components.length - 1; i > 0; i--) {
                    final PathComp currentComp = components[i];
                    if (currentComp instanceof FixedPathComp) {
                        final PathDescription variablePath = new PathDescription.Builder(resolveablePath.getHeadPath(i)).appendComponent(
                                new FixedPathComp(variableName)).build();
                        if (builders.containsKey(variablePath.normalize())) {
                            // Variable found
                            final PathBuilder subpath = builder.createSubpath();
                            for (final PathComp pathComp : variablePath.getComponents()) {
                                if (pathComp instanceof FixedPathComp) {
                                    subpath.addConstEntry(((FixedPathComp) pathComp).getValue());
                                } else if (pathComp instanceof VariablePathComp) {
                                    subpath.addVariableEntry(((VariablePathComp) pathComp).getVariableName());
                                } else {
                                    throw new RuntimeException("Unknown PathComp " + pathComp);
                                }
                            }
                            return;
                        }
                    }
                }
                throw new RuntimeException("Unresolveable variable " + variableName);
            }
        };
    }
}
