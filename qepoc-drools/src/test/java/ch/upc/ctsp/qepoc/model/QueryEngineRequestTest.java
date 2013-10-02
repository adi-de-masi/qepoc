package ch.upc.ctsp.qepoc.model;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class QueryEngineRequestTest {

	@Test
	public void testHasNoSubrequest() {
		QueryEngineRequest req = new QueryEngineRequest();
		Map<String, String> arguments = new HashMap<String, String>();
		arguments.put("MAC", "blah");
		req.setArguments(arguments);
		req.setQueryBlueprint("/some/{MAC}/resource");
		assertFalse(req.hasSubRequest());
	}
	
	@Test
	public void testHasSubrequests() {
		QueryEngineRequest req = new QueryEngineRequest();
		Map<String, String> arguments = new HashMap<String, String>();
		arguments.put("MAC", "blah");
		req.setArguments(arguments);
		req.setQueryBlueprint("/some/{MAC}/cmts/{cmtsHostname}/resource");
		assertTrue(req.hasSubRequest());
	}

}
