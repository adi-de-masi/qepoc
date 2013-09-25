package ch.upc.ctsp.qepoc;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import ch.upc.ctsp.qepoc.server.Protocol;

public class ProtocolTest {
    private Protocol instance;

    @Before
    public void setUp() {
	instance = new Protocol();
    }

    @Test
    public void testResponses() {
	for (int i = 0; i < Protocol.VALID_INPUTS.length; i++) {
	    String out = instance.processInput(Protocol.VALID_INPUTS[i]);
	    assertEquals(Protocol.ANSWERS[i], out);
	}
    }
}
