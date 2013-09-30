/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest.impl;

import ch.upc.ctsp.qepoc.rest.spi.Backend;
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
  private final String[] variableEntries;

  private final Backend  backend;

  public BackendNode(final AbstractRegistryNode parentNode, final String[] variableEntries, final Backend backend) {
    super(parentNode);
    this.variableEntries = variableEntries;
    this.backend = backend;
  }

  @Override
  protected String getComponentNameOf(final RegistryNode childNode) {
    // cannot have any children
    return null;
  }

}
