/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest.model;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import lombok.Data;

/**
 * TODO: add type comment.
 * 
 */
@Data
public class QueryResult {
  public static QueryResult newWithLifeTime(final String value, final int lifeMinutes) {
    final long now = System.currentTimeMillis();
    return new QueryResult(value, new Date(now), new Date(now + TimeUnit.MINUTES.toMillis(lifeMinutes)));
  }

  private final String value;
  private final Date   creationDate;
  private final Date   validUntil;
}
