package ch.upc.ctsp.qepoc;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import ch.upc.ctsp.qepoc.model.QueryEngineRequest;

public class TestQueryReplacement {

	private static final String MAC = "000000000000";
	private static final String BAR = "the foos bar";

	@Test
	public void test() {
		QueryEngineRequest req = new QueryEngineRequest();
		String query = "/blah/{MAC}/foo/{bar}/doh";
		req.setQueryBlueprint(query);
		Map<String, String> arguments = new HashMap<String, String>();
		arguments.put("MAC", MAC);
		arguments.put("bar", BAR);
		req.setArguments(arguments);
		assertEquals("/blah/" + MAC + "/foo/" + BAR + "/doh", req.getQuery());

	}

}
