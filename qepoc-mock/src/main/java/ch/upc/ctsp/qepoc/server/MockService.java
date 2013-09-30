package ch.upc.ctsp.qepoc.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MockService {

  private ServerSocket server;

  public void work() {
    try {
      startupAndWait();
      while (true) {
        final Socket clientSocket = server.accept();
        // if we get here, a client has connected
        System.out.println("received a connection.");
        final Thread t = new Thread(new ServerThread(clientSocket));
        t.start();
      }
    } catch (final IOException e) {
      System.out.println("Could not listen on port 4321");
      System.exit(-1);
    }
  }

  private void startupAndWait() throws IOException {
    System.out.println("starting up server...");
    server = new ServerSocket(4321);
    System.out.println("... ready, listening at port 4321.");
    System.out.println("Say 'Bye.' to disconnect.");
    System.out.println("Say 'Shut.' to shutdown.");
  }

}
