package ch.upc.ctsp.qepoc;

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
    private static final String IP_REQUEST = "ip";
    private static final String IP_RESULT = "10.1.23.23";
    private QueryEngine queryEngine = new QueryEngine();

    @Test
    public void simpleTest() throws InvalidRequestException {
	Querist querist = mock(Querist.class);
	QueryEngineRequest request = new QueryEngineRequest();
	request.setName(IP_REQUEST);
	request.setRequester(querist);
	String result = queryEngine.executeRequest(request);
	verify(querist).retrieveAnswer(IP_REQUEST, IP_RESULT);
	assertEquals(IP_RESULT, result);
    }
}
