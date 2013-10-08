/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest.rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.upc.ctsp.qepoc.rest.impl.BackendWrapper;
import ch.upc.ctsp.qepoc.rest.impl.VariableResolver;
import ch.upc.ctsp.qepoc.rest.model.CallbackFuture;
import ch.upc.ctsp.qepoc.rest.model.QueryResult;
import ch.upc.ctsp.qepoc.rest.spi.Backend;
import ch.upc.ctsp.qepoc.rest.spi.QueryContext;

/**
 * TODO: add type comment.
 * 
 */
public class Alias implements Backend {

    public static class Builder extends DefaultPathBuilder<Builder> implements BackendBuilder {
        private boolean              appendTail       = false;
        private String               referenceString;
        private String[]             parameterNames   = new String[0];

        private static final Pattern VARIABLE_PATTERN = Pattern.compile(Pattern.quote("{") + "([a-zA-Z]+)" + Pattern.quote("}"));

        public Builder appendTail() {
            appendTail = true;
            return this;
        }

        @Override
        public BackendWrapper build(final VariableResolver resolver) {
            if (referenceString != null) {
                buildAlias(resolver);
                referenceString = null;
            }
            return new BackendWrapper(parameterNames, new Alias(new LookupComponentEntry(path), appendTail));
        }

        public Builder parameterNames(final Collection<String> parameterNames) {
            this.parameterNames = parameterNames.toArray(new String[parameterNames.size()]);
            return this;
        }

        public Builder parameterNames(final String[] parameterNames) {
            this.parameterNames = parameterNames;
            return this;
        }

        public Builder reference(final String referenceString) {
            this.referenceString = referenceString;
            return this;
        }

        private void buildAlias(final VariableResolver variableResolver) {
            final String[] pathComps = referenceString.split("/");
            for (final String pathComp : pathComps) {
                final Matcher matcher = VARIABLE_PATTERN.matcher(pathComp);
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
                    addConstEntry(pathComp);
                } else {
                    final String patternString = patternSB.toString();
                    if (patternString.equals("{0}")) {
                        variableResolver.appendPath(this, variables.get(0), parameterNames);
                    } else {
                        final PathBuilder patternBuilder = createPatternEntry(patternString);
                        for (final String variableName : variables) {
                            variableResolver.appendPath(patternBuilder, variableName, parameterNames);
                        }
                    }
                }
            }
        }
    }

    private final LookupComponentEntry lookup;
    private final boolean              appendTail;

    private Alias(final LookupComponentEntry lookup, final boolean appendTail) {
        this.lookup = lookup;
        this.appendTail = appendTail;
    }

    @Override
    public CallbackFuture<QueryResult> query(final QueryContext context) {
        return RulesUtil.processLookup(lookup, context, appendTail);
    }

    @Override
    public String toString() {
        return String.valueOf(lookup);
    }
}
