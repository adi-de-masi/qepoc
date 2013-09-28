package ch.upc.ctsp.qepoc.model;

import java.util.Map;
import java.util.Map.Entry;

import ch.upc.ctsp.qepoc.engine.Querist;

/**
 * Represents a generic query engine request.
 * 
 * @author ademasi
 * 
 */
public class QueryEngineRequest implements Querist {
    private String name;
    private String query;
    private String poller;

    private Map<String, String> arguments;
    private Map<String, String> requiredParameters;

    private Querist requester;

    /**
     * Defines which parameters are needed to fulfil this request. Key: Name of
     * the parameter Value: The corresponding value, should be null for values
     * that need to be polled first.
     * 
     * @param requiredParameters
     */
    public void setRequiredParameters(Map<String, String> requiredParameters) {
	this.requiredParameters = requiredParameters;
    }

    /**
     * Checks if intermediate results are necessary in order to fulfil this
     * request.
     * 
     * @return true if there is at least one sub request left.
     */
    public boolean hasSubRequest() {
	if (requiredParameters == null || getNextMissingParameter() == null) {
	    return false;
	}
	return true;
    }

    /**
     * Gets the next subrequest.
     * 
     * @return The next subrequest for this query.
     * @throws IllegalStateException
     *             if there is no subRequest left in the stack.
     */
    public QueryEngineRequest getNextSubRequest() throws IllegalStateException {
	QueryEngineRequest subrequest = new QueryEngineRequest();
	subrequest.setName(getNextMissingParameter());
	subrequest.setArguments(this.arguments);
	subrequest.setRequester(this);
	return subrequest;
    }

    private String getNextMissingParameter() {
	for (Entry<String, String> e : requiredParameters.entrySet()) {
	    if (e.getValue() == null) {
		return e.getKey();
	    }
	}
	return null;
    }

    /**
     * @return the name
     */
    public String getName() {
	return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
	this.name = name;
    }

    /**
     * @return the arguments
     */
    public Map<String, String> getArguments() {
	return arguments;
    }

    /**
     * @param arguments
     *            the arguments to set
     */
    public void setArguments(Map<String, String> arguments) {
	this.arguments = arguments;
    }

    /**
     * @return the requester
     */
    public Querist getRequester() {
	return requester;
    }

    /**
     * @param requester
     *            the requester to set
     */
    public void setRequester(Querist requester) {
	this.requester = requester;
    }

    public void retrieveAnswer(String parameter, String result) {
	requiredParameters.put(parameter, result);
    }

    /**
     * @return the query
     */
    public String getQuery() {
        return query;
    }

    /**
     * @param query the query to set
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * @return the requiredParameters
     */
    public Map<String, String> getRequiredParameters() {
        return requiredParameters;
    }

    /**
     * @return the poller
     */
    public String getPoller() {
        return poller;
    }

    /**
     * @param poller the poller to set
     */
    public void setPoller(String poller) {
        this.poller = poller;
    }

}
