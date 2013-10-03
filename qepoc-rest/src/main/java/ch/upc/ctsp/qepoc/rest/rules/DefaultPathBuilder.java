/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest.rules;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO: add type comment.
 * 
 */
public class DefaultPathBuilder<B extends DefaultPathBuilder<B>> implements PathBuilder {

    /**
     * currently created Path
     */
    protected final List<ComponentEntry> path = new ArrayList<ComponentEntry>();

    @Override
    public B addConstEntry(final String componentName) {
        path.add(new ConstComponentEntry(componentName));
        return (B) this;
    }

    @Override
    public B addVariableEntry(final String variableName) {
        path.add(new VariableComponentEntry(variableName));
        return (B) this;
    }

    @Override
    public PathBuilder createPatternEntry(final String pattern) {
        final DefaultPathBuilder<?> builder = new DefaultPathBuilder();
        path.add(new PatternComponentEntry(new MessageFormat(pattern), builder.path));
        return builder;
    }

    @Override
    public PathBuilder createSubpath() {
        final DefaultPathBuilder<?> builder = new DefaultPathBuilder();
        path.add(new LookupComponentEntry(builder.path));
        return builder;
    }

}