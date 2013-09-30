/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package net.cablecom.ctsp.query.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.cablecom.ctsp.query.spi.Backend;

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
