package ch.upc.ctsp.qepoc.poller;

import java.io.IOException;

import ch.upc.ctsp.qepoc.model.QueryEngineRequest;

/**
 * Describes the abilities of a CTSP Query Engine Poller.
 * 
 * @author ademasi
 * 
 */
public interface Poller { 

    /**
     * Makes sure the incoming request can be processed.
     * 
     * @param request
     * @throws InvalidRequestException
     *             If something's missing there.
     */
    public void validate(QueryEngineRequest request)
	    throws InvalidRequestException;

    /**
     * Performs the request for real.
     * 
     * @param request
     * @return The polled result as String.
     * 
     *         TODO: Add all kinds of exceptions that may occur.
     * @throws IOException 
     */
    public String execute(QueryEngineRequest request) throws IOException;

}
