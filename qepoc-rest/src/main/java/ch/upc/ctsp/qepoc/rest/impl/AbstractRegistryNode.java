/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest.impl;

import java.io.IOException;
import java.util.LinkedList;

import lombok.Data;

import org.apache.commons.lang.StringUtils;

/**
 * TODO: add type comment.
 * 
 */
@Data
public abstract class AbstractRegistryNode implements RegistryNode {
    private final AbstractRegistryNode parentNode;

    @Override
    public String describePath() {
        final LinkedList<String> comps = new LinkedList<String>();
        AbstractRegistryNode currentNode = this;
        AbstractRegistryNode currentParentNode = currentNode.getParentNode();
        while (currentParentNode != null) {
            final String componentName = currentParentNode.getComponentNameOf(currentNode);
            comps.addFirst(componentName);
            currentNode = currentParentNode;
            currentParentNode = currentNode.getParentNode();
        }
        if (comps.isEmpty()) {
            return "/";
        }
        return StringUtils.join(comps, '/');
    }

    public String dump() {
        try {
            final StringBuilder builder = new StringBuilder();
            dumpValue(builder, 0);
            return builder.toString();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract void dumpValue(final Appendable out, final int level) throws IOException;

    protected abstract String getComponentNameOf(final RegistryNode childNode);

    protected void intent(final Appendable out, final int count) throws IOException {
        for (int i = 0; i < count; i++) {
            out.append("  ");
        }
    }
}
