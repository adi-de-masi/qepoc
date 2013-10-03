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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXB;

import ch.upc.ctsp.qepoc.rest.Query;
import ch.upc.ctsp.qepoc.rest.config.Attribute;
import ch.upc.ctsp.qepoc.rest.config.Conditional;
import ch.upc.ctsp.qepoc.rest.config.Reference;
import ch.upc.ctsp.qepoc.rest.config.RuleCollection;
import ch.upc.ctsp.qepoc.rest.config.RuleSet;
import ch.upc.ctsp.qepoc.rest.model.PathDescription;
import ch.upc.ctsp.qepoc.rest.model.PathDescription.FixedPathComp;
import ch.upc.ctsp.qepoc.rest.model.PathDescription.PathComp;
import ch.upc.ctsp.qepoc.rest.model.PathDescription.VariablePathComp;
import ch.upc.ctsp.qepoc.rest.rules.Alias;
import ch.upc.ctsp.qepoc.rest.rules.PathBuilder;
import ch.upc.ctsp.qepoc.rest.rules.Switch;
import ch.upc.ctsp.qepoc.rest.rules.Switch.Builder;
import ch.upc.ctsp.qepoc.rest.spi.Backend;

/**
 * TODO: add type comment.
 * 
 */
public class QueryBuilder {

    private final Map<PathDescription, Switch.Builder> builders        = new LinkedHashMap<PathDescription, Switch.Builder>();

    private static final Pattern                       variablePattern = Pattern.compile(Pattern.quote("{") + "([a-zA-Z]+)" + Pattern.quote("}"));

    public QueryBuilder appendNativeBackend(final PathDescription path, final Backend backend) {
        getBuilderForPath(path).appendCase().path(path).backend(backend);
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
            impl.registerBackend(builderEntry.getKey(), builderEntry.getValue().build());
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

    private Alias buildAlias(final String reference, final PathDescription parentPath, final PathDescription attributePath, final boolean isReference) {
        final List<String> parameters = Arrays.asList(attributePath.getVariableNames());
        final String[] pathComps = reference.split("/");
        final Alias.Builder aliasBuilder = new Alias.Builder();
        for (final String pathComp : pathComps) {
            final Matcher matcher = variablePattern.matcher(pathComp);
            final StringBuilder patternSB = new StringBuilder();
            int lastPos = 0;
            final List<String> variables = new ArrayList<String>();
            while (matcher.find()) {
                patternSB.append(pathComp, lastPos, matcher.start());
                patternSB.append("{");
                patternSB.append(variables.size());
                patternSB.append("}");
                variables.add(matcher.group(1));
                lastPos = matcher.end();
            }
            patternSB.append(pathComp.subSequence(lastPos, pathComp.length()));
            if (variables.size() == 0) {
                aliasBuilder.addConstEntry(pathComp);
            } else {
                final String patternString = patternSB.toString();
                if (patternString.equals("{0}")) {
                    appendVariablePath(aliasBuilder, variables.get(0), parentPath, parameters);
                } else {
                    final PathBuilder patternBuilder = aliasBuilder.createPatternEntry(patternString);
                    for (final String variableName : variables) {
                        appendVariablePath(patternBuilder, variableName, parentPath, parameters);
                    }
                }
            }
        }
        if (isReference) {
            aliasBuilder.appendTail();
        }
        return aliasBuilder.build();
    }

    /**
     * @param rules
     * @param path
     * @param conditions
     */
    private void fillBuilders(final RuleCollection rules, final PathDescription path, final List<String> conditions) {
        if (rules instanceof RuleSet) {
            for (final Conditional conditional : ((RuleSet) rules).getConditional()) {
                final ArrayList<String> newConditions = new ArrayList<String>(conditions);
                newConditions.add(conditional.getCondition());
                fillBuilders(conditional, path, newConditions);
            }
        }
        for (final RuleSet ruleSet : rules.getRuleSets()) {
            final PathDescription ruleSetPath = new PathDescription.Builder(path).appendString(ruleSet.getPath()).build();
            fillBuilders(ruleSet, ruleSetPath, conditions);
        }
        for (final Attribute attribute : rules.getAttribute()) {
            final PathDescription attributePath = new PathDescription.Builder(path).appendString(attribute.getName()).build();
            final Switch.Builder builder = getBuilderForPath(attributePath);
            final Switch.CaseBuilder newCase = builder.appendCase();
            newCase.path(attributePath).backend(buildAlias(attribute.getReference(), path, attributePath, false));
            appendConditions(newCase, conditions, path);
        }
        for (final Reference reference : rules.getReference()) {
            final PathDescription referencePath = new PathDescription.Builder(path).appendString(reference.getName()).build();
            final Switch.Builder builder = getBuilderForPath(referencePath);
            final Switch.CaseBuilder newCase = builder.appendCase();
            newCase.path(referencePath).backend(buildAlias(reference.getReference(), path, referencePath, true));
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

    private Switch.Builder getBuilderForPath(final PathDescription attributePath) {
        final PathDescription normalPath = attributePath.normalize();
        final Switch.Builder existingBuilder = builders.get(normalPath);
        if (existingBuilder != null) {
            return existingBuilder;
        }
        final Switch.Builder newBuilder = new Switch.Builder();
        builders.put(normalPath, newBuilder);
        return newBuilder;
    }
}
