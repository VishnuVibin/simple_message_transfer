import java.net.*;
import java.io.*;
import java.util.*;

public class Server {

    static HashMap<String, ClientHandler> clients =
            new HashMap<>();

    public static void main(String[] args) {

        try {

            ServerSocket server =
                    new ServerSocket(5000);

            System.out.println("Server Started");

            while (true) {

                Socket socket =
                        server.accept();

                ClientHandler client =
                        new ClientHandler(socket);

                client.start();
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}