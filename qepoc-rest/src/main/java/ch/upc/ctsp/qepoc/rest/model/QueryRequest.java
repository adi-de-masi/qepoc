/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest.model;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * TODO: add type comment.
 * 
 */
@Data
@AllArgsConstructor
public class QueryRequest {
  public static class Builder {
    private List<String> path         = null;
    private Date         allowedSince = null;

    public Builder() {
    }

    public Builder(final QueryRequest request) {
      path = request.getPath();
      allowedSince = request.getAllowedSince();
    }

    public Builder allowedSince(final Date allowedSince) {
      this.allowedSince = allowedSince;
      return this;
    }

    public Builder appendPath(final String path) {
      if (this.path == null) {
        this.path = splitAndDecode(path);
      } else {
        this.path.addAll(splitAndDecode(path));
      }
      return this;
    }

    public Builder appendPathComp(final String pathComp) {
      if (this.path == null) {
        this.path = new ArrayList<String>();
      }
      this.path.add(pathComp);
      return this;
    }

    /**
     * @param remainingComps
     * @return
     */
    public Builder appendPathComps(final List<String> remainingComps) {
      if (this.path == null) {
        this.path = new ArrayList<String>();
      }
      this.path.addAll(remainingComps);
      return this;
    }

    public QueryRequest build() {
      if (path == null) {
        throw new IllegalArgumentException("Path is a required attribute");
      }
      if (allowedSince == null) {
        maxAge(DEFAULT_AGE);
      }
      return new QueryRequest(path, allowedSince);
    }

    public Builder maxAge(final long maxAge) {
      allowedSince = new Date(System.currentTimeMillis() - maxAge);
      return this;
    }

    public Builder path(final List<String> path) {
      this.path = new ArrayList<String>(path);
      return this;
    }

    public Builder path(final String path) {
      this.path = splitAndDecode(path);
      return this;
    }

    private List<String> splitAndDecode(final String path) {
      try {
        final ArrayList<String> ret = new ArrayList<String>();
        for (final String comp : path.split("/")) {
          ret.add(URLDecoder.decode(comp, "utf-8"));
        }
        return ret;
      } catch (final UnsupportedEncodingException e) {
        throw new RuntimeException("This JVM doesnt support utf-8");
      }
    }
  }

  private static long DEFAULT_AGE = TimeUnit.SECONDS.toMillis(2);

  public static QueryRequest createRequest(final String path) {
    return createRequest(path, DEFAULT_AGE);
  }

  public static QueryRequest createRequest(final String path, final long age) {
    return new Builder().path(path).maxAge(age).build();
  }

  private final List<String> path;
  private final Date         allowedSince;
}
