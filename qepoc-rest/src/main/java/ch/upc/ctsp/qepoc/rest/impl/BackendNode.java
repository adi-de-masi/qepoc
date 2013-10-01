/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest.impl;

import java.io.IOException;
import java.util.Arrays;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ch.upc.ctsp.qepoc.rest.spi.Backend;

/**
 * TODO: add type comment.
 * 
 */
@Data
@EqualsAndHashCode(
        callSuper = true)
public class BackendNode extends AbstractRegistryNode {
    private final String[] variableEntries;

    private final Backend  backend;

    public BackendNode(final AbstractRegistryNode parentNode, final String[] variableEntries, final Backend backend) {
        super(parentNode);
        this.variableEntries = variableEntries;
        this.backend = backend;
    }

    @Override
    protected void dumpValue(final Appendable out, final int level) throws IOException {
        intent(out, level);
        out.append(Arrays.toString(variableEntries));
        out.append(": ");
        out.append(String.valueOf(backend));
        out.append("\n");
    }

    @Override
    protected String getComponentNameOf(final RegistryNode childNode) {
        // cannot have any children
        return null;
    }

}
