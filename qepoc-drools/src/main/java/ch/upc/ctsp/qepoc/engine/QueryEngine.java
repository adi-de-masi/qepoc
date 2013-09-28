package ch.upc.ctsp.qepoc.engine;

import ch.upc.ctsp.qepoc.model.QueryEngineRequest;
import ch.upc.ctsp.qepoc.poller.InvalidRequestException;
import ch.upc.ctsp.qepoc.poller.Poller;
import ch.upc.ctsp.qepoc.poller.PollerFactory;

/**
 * A minimal query engine that currently only talks to a mock resource.
 * 
 * @author ademasi
 * 
 */
public class QueryEngine {

    private RuleEngine ruleEngine = new RuleEngine();

    /**
     * Executes a query request.
     * 
     * @param request
     * @return
     * @throws InvalidRequestException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public String executeRequest(QueryEngineRequest request)
	    throws InvalidRequestException {

	while (request.hasSubRequest()) {
	    this.executeRequest(request.getNextSubRequest());
	}

	ruleEngine.execute(request);
	
	Poller poller = PollerFactory.newPoller(request.getPoller());
	
	poller.validate(request);

	return poller.execute(request);
    }
}
