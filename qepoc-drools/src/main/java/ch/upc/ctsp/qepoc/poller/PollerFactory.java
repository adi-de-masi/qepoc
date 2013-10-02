package ch.upc.ctsp.qepoc.poller;

import ch.upc.ctsp.qepoc.model.Resource;

public class PollerFactory {
    

    public static Poller newPoller(String poller) {
	Resource resource = Resource.valueOf(poller);
	switch (resource) {
	case SNMP:
	    return new SnmpPoller();
	case DATABASE:
	    throw new UnsupportedOperationException();
	case MOCK:
	    throw new UnsupportedOperationException();
	case TELNET:
	    throw new UnsupportedOperationException();
	    default: 
	    throw new UnsupportedOperationException();
	}
    }

}
