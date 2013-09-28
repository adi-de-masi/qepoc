package ch.upc.ctsp.qepoc.engine;

import java.util.Map;

import org.drools.KnowledgeBase;
import org.drools.agent.KnowledgeAgent;
import org.drools.agent.KnowledgeAgentFactory;
import org.drools.definition.type.FactType;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;

import ch.upc.ctsp.qepoc.model.QueryEngineRequest;

/**
 * A wrapper for rule engine queries.
 * 
 * @author ademasi
 * 
 */
public class RuleEngine {
    private KnowledgeAgent kagent;
    private KnowledgeBase kbase;

    public RuleEngine() {
	kagent = KnowledgeAgentFactory.newKnowledgeAgent("QepocAgent");
	Resource changeset = ResourceFactory
		.newClassPathResource("qepoc-rules.xml");
	kagent.applyChangeSet(changeset);
	kbase = kagent.getKnowledgeBase();
	kagent.dispose();
    }

    @SuppressWarnings("unchecked")
    public void execute(QueryEngineRequest request) {
	// Dynamic fact creation as the model was declared in the DRL
	FactType appType = kbase.getFactType("qepoc", "Request");

	Object req = null;
	try {
	    req = appType.newInstance();
	} catch (Exception e) {
	    // TODO Treat this with respect
	    e.printStackTrace();
	}

	appType.set(req, "name", request.getName());
	appType.set(req, "arguments", request.getArguments());

	StatefulKnowledgeSession ksession = null;
	// Invoke the magic
	ksession = kbase.newStatefulKnowledgeSession();
	ksession.insert(req);
	ksession.fireAllRules();
	request.setPoller((String) appType.get(req, "poller"));
	request.setQuery((String) appType.get(req, "query"));
	request.setRequiredParameters((Map<String, String>) appType.get(req,
		"requiredParameters"));
    }

}
