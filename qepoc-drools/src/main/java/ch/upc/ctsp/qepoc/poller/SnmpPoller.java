package ch.upc.ctsp.qepoc.poller;

import java.io.IOException;

import ch.upc.ctsp.qepoc.model.QueryEngineRequest;
import ch.upc.ctsp.qepoc.client.Client;

;

/**
 * A fake implementation of an SNMP Poller.
 * 
 * @author ademasi
 * 
 */
public class SnmpPoller implements Poller {

	private static final Object WRONG_REQUEST = "Wrong is the Request. Mind what you have learned. Save you it can.\n";

	public void validate(QueryEngineRequest request)
			throws InvalidRequestException {
		// TODO: Implement smart validation
	}

	public String execute(QueryEngineRequest request) throws IOException {
		String response = Client.query(request.getQuery());
		if (response.equals(WRONG_REQUEST)) {
			throw new IllegalArgumentException(String.format(
					"Error with request %s, [query = %s]", new Object[] {
							request.getName(), request.getQuery() }));
		}
		return response.trim();
	}

}
