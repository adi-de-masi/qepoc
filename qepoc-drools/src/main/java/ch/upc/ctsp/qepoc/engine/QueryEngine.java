package ch.upc.ctsp.qepoc.engine;

import java.io.IOException;

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
	 * @throws IOException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public void executeRequest(QueryEngineRequest request)
			throws InvalidRequestException, IOException {

		ruleEngine.execute(request);
		request.validate();

		while (request.hasSubRequest()) {
			this.executeRequest(request.getNextSubRequest());
		}

		Poller poller = PollerFactory.newPoller(request.getPoller());

		poller.validate(request);

		String result = poller.execute(request);
		request.retrieveAnswer(request.getName(), result);
	}
}
