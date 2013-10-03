/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest.rules;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import ch.upc.ctsp.qepoc.rest.model.CallbackFuture;
import ch.upc.ctsp.qepoc.rest.model.PathDescription;
import ch.upc.ctsp.qepoc.rest.model.QueryResult;
import ch.upc.ctsp.qepoc.rest.spi.Backend;
import ch.upc.ctsp.qepoc.rest.spi.QueryContext;

/**
 * TODO: add type comment.
 * 
 */
public class Alias implements Backend {

    public static class Builder implements PathBuilder {
        private final List<ComponentEntry> path = new ArrayList<ComponentEntry>();

        @Override
        public Builder addConstEntry(final String componentName) {
            path.add(new ConstComponentEntry(componentName));
            return this;
        }

        @Override
        public Builder addVariableEntry(final String variableName) {
            path.add(new VariableComponentEntry(variableName));
            return this;
        }

        public Alias build() {
            return new Alias(new LookupComponentEntry(path));
        }

        @Override
        public PathBuilder createPatternEntry(final String pattern) {
            final Builder builder = new Builder();
            path.add(new PatternComponentEntry(new MessageFormat(pattern), builder.path));
            return builder;
        }

        @Override
        public PathBuilder createSubpath() {
            final Builder builder = new Builder();
            path.add(new LookupComponentEntry(builder.path));
            return builder;
        }
    }

    public static Map<PathDescription, Alias> parseProperties(final Properties props) {
        final HashMap<PathDescription, Alias> ret = new HashMap<PathDescription, Alias>();
        for (final Entry<Object, Object> aliasEntry : props.entrySet()) {
            final PathDescription targetPath = PathDescription.createFromString(aliasEntry.getKey().toString());
            final String rule = (String) aliasEntry.getValue();
            final Builder builder = new Builder();
            final LinkedList<PathBuilder> builderStack = new LinkedList<PathBuilder>();
            builderStack.add(builder);
            boolean patternMode = false;
            final StringBuilder currenName = new StringBuilder();
            for (int i = 0; i < rule.length(); i++) {
                final char currentChar = rule.charAt(i);
                switch (currentChar) {
                    case '/':
                        if (currenName.length() > 0) {
                            builderStack.getLast().addConstEntry(currenName.toString());
                        }
                        currenName.setLength(0);
                        break;
                    case '<':
                        builderStack.add(builderStack.getLast().createSubpath());
                        break;
                    case '>':
                        if (currenName.length() > 0) {
                            builderStack.getLast().addConstEntry(currenName.toString());
                        }
                        currenName.setLength(0);
                        builderStack.removeLast();
                        break;
                    case '[':
                        currenName.setLength(0);
                        patternMode = true;
                        break;
                    case ':':
                        patternMode = false;
                        builderStack.add(builderStack.getLast().createPatternEntry(currenName.toString()));
                        builderStack.add(builderStack.getLast().createSubpath());
                        currenName.setLength(0);
                        break;
                    case ',':
                        if (currenName.length() > 0) {
                            builderStack.getLast().addConstEntry(currenName.toString());
                        }
                        currenName.setLength(0);
                        builderStack.removeLast();
                        builderStack.add(builderStack.getLast().createSubpath());
                        break;
                    case ']':
                        if (currenName.length() > 0) {
                            builderStack.getLast().addConstEntry(currenName.toString());
                        }
                        currenName.setLength(0);
                        builderStack.removeLast();
                        builderStack.removeLast();
                        break;
                    default:
                        if (!patternMode) {
                            if (currentChar == '}') {
                                if (currenName.length() > 0) {
                                    builderStack.getLast().addVariableEntry(currenName.toString());
                                }
                                currenName.setLength(0);
                            } else if (currentChar != '{') {
                                currenName.append(currentChar);
                            }
                        } else {
                            currenName.append(currentChar);
                        }
                        break;
                }
            }
            if (currenName.length() > 0) {
                builderStack.getLast().addConstEntry(currenName.toString());
            }
            currenName.setLength(0);
            ret.put(targetPath, builder.build());
        }
        return ret;
    }

    private final LookupComponentEntry lookup;

    private Alias(final LookupComponentEntry lookup) {
        this.lookup = lookup;
    }

    @Override
    public CallbackFuture<QueryResult> query(final QueryContext context) {
        return RulesUtil.processLookup(lookup, context);
    }

    @Override
    public String toString() {
        return String.valueOf(lookup);
    }
}
