/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package net.cablecom.ctsp.query.impl;

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
  protected String getComponentNameOf(final RegistryNode childNode) {
    for (final Entry<String, AbstractRegistryNode> entry : subNodes.entrySet()) {
      if (entry.getValue() == childNode) {
        return entry.getKey();
      }
    }
    return null;
  }
}
