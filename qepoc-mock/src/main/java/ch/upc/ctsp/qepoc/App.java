package ch.upc.ctsp.qepoc;

import ch.upc.ctsp.qepoc.server.MockService;

/**
 * A minimalistic Mock which is going to reply to specified requests in a
 * specified manner:
 * <ol>
 * <li>/modem/00AB123456/ip → 10.30.22.12</li>
 * <li>/scopes/10.30.22.12 → mbsXZY001-01-pc</li>
 * <li>/scopes/mbsXZY001-01-pc/cmts → mbsXZY001</li>
 * <li>/cmts/mbsXZY001/modem/00AB123456/cmId → 203342</li>
 * <li>/cmts/mbsXZY001/ip → 62.2.11.22</li>
 * <li>/snmp/62.2.11.22/1.3.6.2.1.2.3.4.20334 → 'L1/0/1/1'</li>
 * </ol>
 * 
 * Usage: Start this app and connect to the socket via client, e.g. telnet:
 * 'telnet localhost 4321'
 * 
 * Then send one of the valid requests, and you will get the expected response.
 * 
 */
public class App {
    public static void main(String[] args) {
	new MockService().work();
    }
}
