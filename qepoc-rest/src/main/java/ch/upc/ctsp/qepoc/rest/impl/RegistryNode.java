/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest.impl;

/**
 * TODO: add type comment.
 * 
 */
public interface RegistryNode {
  RegistryNode getParentNode();

  String describePath();
}
