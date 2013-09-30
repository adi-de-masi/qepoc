package ch.upc.ctsp.qepoc.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerThread implements Runnable {
  private final Socket clientSocket;

  public ServerThread(final Socket socket) {
    this.clientSocket = socket;
  }

  public void run() {
    try {
      handleInput();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  private void disconnectClient(final PrintWriter out) {
    String outputLine;
    outputLine = "Bye.";
    out.println(outputLine);
  }

  private void handleInput() throws IOException {
    final PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
    final BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

    String inputLine, outputLine;

    // initiate conversation with client
    final Protocol kkp = new Protocol();
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

}
