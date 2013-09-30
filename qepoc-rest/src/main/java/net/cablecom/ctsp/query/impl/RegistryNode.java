/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package net.cablecom.ctsp.query.impl;

/**
 * TODO: add type comment.
 * 
 */
public interface RegistryNode {
  RegistryNode getParentNode();

  String describePath();
}
