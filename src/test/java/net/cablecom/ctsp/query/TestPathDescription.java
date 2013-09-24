/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package net.cablecom.ctsp.query;

import static org.junit.Assert.assertEquals;
import net.cablecom.ctsp.query.model.PathDescription;

import org.junit.Test;

/**
 * TODO: add type comment.
 * 
 */
public class TestPathDescription {
  @Test
  public void testConstantPath() {
    final PathDescription simplePath = PathDescription.createFromString("comp");
    assertEquals("comp", simplePath.toString());
    final PathDescription multiPartPath = PathDescription.createFromString("comp1/comp2");
    assertEquals("comp1/comp2", multiPartPath.toString());
  }

  @Test
  public void testPathWithVariables() {
    final PathDescription simplePath = PathDescription.createFromString("{param}");
    assertEquals("{param}", simplePath.toString());
    final PathDescription multiPartPath = PathDescription.createFromString("comp/{param}");
    assertEquals("comp/{param}", multiPartPath.toString());
  }

  @Test
  public void testPrefixPathWithVariables() {
    final PathDescription multiPartPath = PathDescription.createFromString("comp/{param}/");
    assertEquals("comp/{param}/", multiPartPath.toString());
  }
}
