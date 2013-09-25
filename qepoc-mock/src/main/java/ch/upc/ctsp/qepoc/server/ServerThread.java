package ch.upc.ctsp.qepoc.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerThread implements Runnable {
    private Socket clientSocket;

    public ServerThread(Socket socket) {
	this.clientSocket = socket;
    }

    public void run() {
	try {
	    handleInput();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    private void handleInput() throws IOException {
	PrintWriter out = new PrintWriter(clientSocket.getOutputStream(),
	    true);
	BufferedReader in = new BufferedReader(new InputStreamReader(
	    clientSocket.getInputStream()));

	String inputLine, outputLine;

	// initiate conversation with client
	Protocol kkp = new Protocol();
	outputLine = kkp.processInput(null);
	out.println(outputLine);
	while ((inputLine = in.readLine()) != null) {
	outputLine = kkp.processInput(inputLine);
	out.println(outputLine);
	if (inputLine.equals("Bye.")) {
	    disconnectClient(out);
	    clientSocket.close();
	    break;
	} else if (inputLine.equals("Shut.")) {
	    disconnectClient(out);
	    clientSocket.close();
	    System.exit(0);
	}
	}
    }

    private void disconnectClient(PrintWriter out) {
	String outputLine;
	outputLine = "Bye.";
	out.println(outputLine);
    }

}
