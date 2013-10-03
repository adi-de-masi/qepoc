/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest.impl;

import java.io.IOException;
import java.util.Arrays;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * TODO: add type comment.
 * 
 */
@Data
@EqualsAndHashCode(
        callSuper = true)
public class BackendNode extends AbstractRegistryNode {
    private final BackendWrapper wrapper;

    public BackendNode(final AbstractRegistryNode parentNode, final BackendWrapper wrapper) {
        super(parentNode);
        this.wrapper = wrapper;
    }

    @Override
    protected void dumpValue(final Appendable out, final int level) throws IOException {
        intent(out, level);
        out.append(Arrays.toString(wrapper.getVariableEntries()));
        out.append(": ");
        out.append(String.valueOf(wrapper.getBackend()));
        out.append("\n");
    }

    @Override
    protected String getComponentNameOf(final RegistryNode childNode) {
        // cannot have any children
        return null;
    }

}
