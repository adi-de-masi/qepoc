package ch.upc.ctsp.qepoc;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import ch.upc.ctsp.qepoc.engine.Querist;
import ch.upc.ctsp.qepoc.engine.QueryEngine;
import ch.upc.ctsp.qepoc.model.QueryEngineRequest;
import ch.upc.ctsp.qepoc.poller.InvalidRequestException;

/**
 * Unit test for simple App.
 */
public class EndToEndTests {
	private static final String IP_REQUEST = "modemIp";
	private static final String IP_RESULT = "10.30.22.12\n";
	private static final String MAC = "00AB123456";
	private QueryEngine queryEngine = new QueryEngine();

	@Test
	public void simpleTest() throws InvalidRequestException, IOException {
		Querist querist = mock(Querist.class);
		QueryEngineRequest request = new QueryEngineRequest();
		request.setName(IP_REQUEST);
		request.setRequester(querist);
		Map<String, String> arguments = new HashMap<String, String>();
		arguments.put("MAC", MAC);
		request.setArguments(arguments);
		queryEngine.executeRequest(request);
		verify(querist).retrieveAnswer(IP_REQUEST, IP_RESULT);
		assertEquals(IP_RESULT, request.getArguments().get(IP_REQUEST));
	}
}
