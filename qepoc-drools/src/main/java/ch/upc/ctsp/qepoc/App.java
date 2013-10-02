package ch.upc.ctsp.qepoc;

import static ch.upc.ctsp.qepoc.util.SimplestLogger.log;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ch.upc.ctsp.qepoc.engine.QueryEngine;
import ch.upc.ctsp.qepoc.model.QueryEngineRequest;
import ch.upc.ctsp.qepoc.poller.InvalidRequestException;

/**
 * 
 *
 */
public class App {
	public static void main(String[] args) throws InvalidRequestException,
			IOException {
		log("In the morning everybody! I am your Query Engine Proof of Concept using JBoss Drools");
		log("Setting up the request...");
		QueryEngineRequest request = new QueryEngineRequest();
		Map<String, String> arguments = new HashMap<String, String>();
		arguments.put("MAC", "00AB123456");
		request.setArguments(arguments);
		request.setName("mainUpstreamIfc");
		request.setRequester(new DemoQuerist());

		QueryEngine engine = new QueryEngine();
		engine.executeRequest(request);
	}
}
