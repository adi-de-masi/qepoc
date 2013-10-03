/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest.model;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.Data;

/**
 * TODO: add type comment.
 * 
 */
@Data
public class PathDescription {
    public static class Builder {
        private final List<PathComp> components = new ArrayList<PathComp>();
        private boolean              isPrefix   = false;

        public Builder appendString(final String path) {
            isPrefix = path.endsWith("/");
            for (final String compDescription : path.split("/")) {
                if (compDescription.startsWith("{") && compDescription.endsWith("}")) {
                    components.add(new VariablePathComp(compDescription.substring(1, compDescription.length() - 1)));
                } else {
                    components.add(new FixedPathComp(decode(compDescription)));
                }
            }
            return this;
        }

        public Builder appendTail(final PathDescription path, final int fromIndex) {
            isPrefix = path.isPrefix;
            components.addAll(Arrays.asList(path.components).subList(fromIndex, path.components.length));
            return this;
        }

        public PathDescription build() {
            return new PathDescription(isPrefix, components.toArray(new PathComp[components.size()]));
        }
    }

    @Data
    public static class FixedPathComp implements PathComp {
        private final String value;
    }

    public static interface PathComp {
    }

    @Data
    public static class VariablePathComp implements PathComp {
        private final String variableName;
    }

    public static PathDescription createFromString(final String description) {
        return new Builder().appendString(description).build();
    }

    private static String decode(final String compDescription) {
        try {
            return URLDecoder.decode(compDescription, "utf-8");
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException("Strange, this JVM doesn't support utf-8", e);
        }
    }

    private static String encode(final String value) {
        try {
            return URLEncoder.encode(value, "utf-8");
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException("Strange, this JVM doesn't support utf-8", e);
        }
    }

    private final PathComp[] components;
    private final boolean    isPrefix;

    public PathDescription(final boolean isPrefix, final PathComp[] components) {
        this.isPrefix = isPrefix;
        this.components = components;
    }

    /**
     * create a new {@link PathDescription} with more path-components
     * 
     * @param appendComps
     *            components to add
     * @return new {@link PathDescription}
     */
    public PathDescription appendComps(final java.util.List<PathComp> appendComps) {
        final ArrayList<PathComp> comps = new ArrayList<PathComp>(Arrays.asList(components));
        comps.addAll(appendComps);
        return new PathDescription(false, comps.toArray(new PathComp[comps.size()]));
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        for (final PathComp comp : components) {
            if (builder.length() > 0) {
                builder.append("/");
            }
            if (comp instanceof FixedPathComp) {
                builder.append(encode(((FixedPathComp) comp).getValue()));
            } else if (comp instanceof VariablePathComp) {
                builder.append("{");
                builder.append(((VariablePathComp) comp).getVariableName());
                builder.append("}");
            }
        }
        if (isPrefix) {
            builder.append("/");
        }
        return builder.toString();
    }
}
