import java.net.*;
import java.io.*;

public class Client {

    Socket socket;

    BufferedReader br;
    PrintWriter out;

    // Constructor
    public Client() {

        try {

            // Connect to server
            socket = new Socket("localhost", 5000);

            System.out.println("Connected To Server");

            // Receive messages
            br = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream()
                    )
            );

            // Send messages
            out = new PrintWriter(
                    socket.getOutputStream(),
                    true
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // Send message to server
    public void sendMessage(String msg) {

        out.println(msg);
    }

    // Receive messages from server
    public void receiveMessage() {

        Thread thread = new Thread(() -> {

            try {

                while (true) {

                    String msg = br.readLine();

                    if (msg == null) {

                        break;
                    }

                    System.out.println(msg);
                }

            } catch (Exception e) {

                System.out.println("Disconnected From Server");
            }
        });

        thread.start();
    }

    // Main Method
    public static void main(String[] args) {

        try {

            Client client = new Client();

            // Start receiving messages
            client.receiveMessage();

            // Read keyboard input
            BufferedReader keyboard =
                    new BufferedReader(
                            new InputStreamReader(System.in)
                    );

            while (true) {

                String message = keyboard.readLine();

                client.sendMessage(message);
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}