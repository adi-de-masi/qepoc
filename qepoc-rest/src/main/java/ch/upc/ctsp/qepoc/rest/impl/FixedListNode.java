/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * TODO: add type comment.
 * 
 */
@Data
@EqualsAndHashCode(
        callSuper = true)
public class FixedListNode extends AbstractRegistryNode {
    private Map<String, AbstractRegistryNode> subNodes = new HashMap<String, AbstractRegistryNode>();

    public FixedListNode(final AbstractRegistryNode parentNode) {
        super(parentNode);
    }

    @Override
    protected void dumpValue(final Appendable out, final int level) throws IOException {
        for (final Entry<String, AbstractRegistryNode> entries : subNodes.entrySet()) {
            intent(out, level);
            out.append(entries.getKey());
            out.append("\n");
            entries.getValue().dumpValue(out, level + 1);
        }
    }

    @Override
    protected String getComponentNameOf(final RegistryNode childNode) {
        for (final Entry<String, AbstractRegistryNode> entry : subNodes.entrySet()) {
            if (entry.getValue() == childNode) {
                return entry.getKey();
            }
        }
        return null;
    }
}
