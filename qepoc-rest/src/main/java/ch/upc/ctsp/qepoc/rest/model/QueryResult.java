/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest.model;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * TODO: add type comment.
 * 
 */
@Data
@AllArgsConstructor
public class QueryResult {
    public static QueryResult newWithLifeTime(final String value, final int lifeMinutes) {
        final long now = System.currentTimeMillis();
        return new QueryResult(value, new Date(now), new Date(now + TimeUnit.MINUTES.toMillis(lifeMinutes)));
    }

    private final String value;

    private final Date   creationDate;
    private final Date   validUntil;

    public QueryResult(final String value) {
        this.value = value;
        creationDate = new Date();
        validUntil = null;
    }

    public QueryResult withMaxCreationDate(final Date maxCreationDate) {
        if (maxCreationDate == null || maxCreationDate.after(creationDate)) {
            return this;
        }
        return new QueryResult(value, maxCreationDate, validUntil);
    }
}
