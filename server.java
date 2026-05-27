import java.net.*;
import java.io.*;
import java.util.*;

public class Server {

    // Stores all connected clients
    static Vector<ClientHandler> clients = new Vector<>();

    public static void main(String[] args) {

        try {

            // Server starts on port 5000
            ServerSocket serverSocket =
                    new ServerSocket(5000);

            System.out.println("Server Started...");
            System.out.println("Waiting for Clients...");

            while (true) {

                // Accept client connection
                Socket socket =
                        serverSocket.accept();

                System.out.println("New Client Connected");

                // Create client thread
                ClientHandler client =
                        new ClientHandler(socket);

                // Add client to list
                clients.add(client);

                // Start thread
                client.start();
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}