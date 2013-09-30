/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package net.cablecom.ctsp.query.impl;

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

  /*
   * (non-Javadoc)
   * 
   * @see net.cablecom.ctsp.query.impl.RegistryNode#describePath()
   */
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

  protected abstract String getComponentNameOf(final RegistryNode childNode);

}
