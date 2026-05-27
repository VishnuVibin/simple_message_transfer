import java.net.*;
import java.io.*;

public class ClientHandler extends Thread {

    Socket socket;

    BufferedReader br;
    PrintWriter out;

    // Constructor
    public ClientHandler(Socket socket) {

        try {

            this.socket = socket;

            // Receive data from client
            br = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream()
                    )
            );

            // Send data to client
            out = new PrintWriter(
                    socket.getOutputStream(),
                    true
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // Thread starts here
    public void run() {

        try {

            while (true) {

                // Read message from client
                String message = br.readLine();

                // If client disconnects
                if (message == null) {

                    break;
                }

                System.out.println("Client: " + message);

                // Send message to all connected clients
                for (ClientHandler client : Server.clients) {

                    // Don't send back to sender
                    if(client != this) {

                        client.out.println(message);
                    }
}
            }

        } catch (Exception e) {

            System.out.println("Client Disconnected");

        } finally {

            try {

                // Remove client
                Server.clients.remove(this);

                // Close socket
                socket.close();

            } catch (Exception e) {

                e.printStackTrace();
            }
        }
    }
}