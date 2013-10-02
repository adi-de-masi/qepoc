package ch.upc.ctsp.qepoc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ch.upc.ctsp.qepoc.engine.RuleEngine;
import ch.upc.ctsp.qepoc.model.QueryEngineRequest;

public class MainUpstreamIfcRuleTest {

    @Test
    public void testMainUpstreamIfcRule() {
	RuleEngine ruleEngine = new RuleEngine();
	QueryEngineRequest request = new QueryEngineRequest();
	request.setName("mainUpstreamIfc");

	ruleEngine.execute(request);

	assertEquals("/snmp/{cmtsIp}/1.3.6.2.1.2.3.4.{cmId}", request.getQueryBlueprint());
	assertNotNull(request.getRequiredParameters());
	assertTrue(request.getRequiredParameters().contains("{cmtsIp}"));
	assertTrue(request.getRequiredParameters().contains("{cmId}"));
    }

}
