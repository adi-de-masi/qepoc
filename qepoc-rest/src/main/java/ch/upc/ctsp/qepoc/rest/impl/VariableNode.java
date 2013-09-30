/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest.impl;

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
  private RegistryNode subNode;

  public VariableNode(final AbstractRegistryNode parentNode) {
    super(parentNode);
  }

  @Override
  protected String getComponentNameOf(final RegistryNode childNode) {
    return "*";
  }

}
