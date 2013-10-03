/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest.impl;

import java.util.HashMap;
import java.util.Map;

import ch.upc.ctsp.qepoc.rest.config.RuleSet;
import ch.upc.ctsp.qepoc.rest.spi.Backend;

/**
 * TODO: add type comment.
 * 
 */
public class ConfigBuilder {
    private static class PathEntry {
        private Backend                    defaultEntry;
        private final Map<String, Backend> conditionalEntries = new HashMap<String, Backend>();
    }

    public void buildConfig(final RuleSet ruleSet) {
        // Map<PathDescription, Backend>
    }
}
