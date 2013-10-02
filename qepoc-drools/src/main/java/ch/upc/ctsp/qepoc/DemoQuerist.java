package ch.upc.ctsp.qepoc;

import static ch.upc.ctsp.qepoc.util.SimplestLogger.log;
import ch.upc.ctsp.qepoc.engine.Querist;

public class DemoQuerist implements Querist {
	
	public void retrieveAnswer(String parameter, String result) {
		log("retrieved answer %s for parameter %s", result, parameter);
	}

}
