package ch.upc.ctsp.qepoc;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.upc.ctsp.qepoc.client.Client;
import ch.upc.ctsp.qepoc.server.Protocol;

/**
 * Unit test for simple App.
 */
public class AppTest {
    private static Thread t;

    @BeforeClass
    public static void setUpOnce() {
	t = new Thread(new AppTest().new TestRunner());
	t.start();
    }
    
    @After
    public void tearDown() throws IOException {
	Client.query("Bye.");	
    }

    @AfterClass
    public static void shutDown() throws IOException {
	Client.query("Shut.");
    }

    @Test
    public void testClientSingleQueries() throws IOException,
	    InterruptedException {
	for (int i = 0; i < Protocol.VALID_INPUTS.length; i++) {
	    String response = Client.query(Protocol.VALID_INPUTS[i]);
	    assertEquals(Protocol.ANSWERS[i], response.trim());
	}
    }

    @Test
    public void testBulkQuery() throws IOException {
	String response = Client.query(Protocol.VALID_INPUTS);
	String[] responses = response.split("\n");
	assertEquals(Protocol.ANSWERS.length, responses.length);
    }

    class TestRunner implements Runnable {

	public void run() {
	    App.main(null);
	}

    }
}
