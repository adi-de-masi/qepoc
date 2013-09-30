/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest.impl;

import java.io.IOException;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * TODO: add type comment.
 * 
 */
@Data
@EqualsAndHashCode(
        callSuper = true)
public class VariableNode extends AbstractRegistryNode {
    private AbstractRegistryNode subNode;

    public VariableNode(final AbstractRegistryNode parentNode) {
        super(parentNode);
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.upc.ctsp.qepoc.rest.impl.AbstractRegistryNode#dumpValue(java.lang.Appendable, int)
     */
    @Override
    protected void dumpValue(final Appendable out, final int level) throws IOException {
        intent(out, level);
        out.append("*");
        out.append("\n");
        subNode.dumpValue(out, level + 1);
    }

    @Override
    protected String getComponentNameOf(final RegistryNode childNode) {
        return "*";
    }

}
