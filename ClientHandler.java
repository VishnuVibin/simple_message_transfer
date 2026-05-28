import java.net.*;
import java.io.*;

public class ClientHandler extends Thread {

    Socket socket;

    BufferedReader br;

    PrintWriter out;

    String username;

    public ClientHandler(Socket socket) {

        try {

            this.socket = socket;

            br = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream()
                    )
            );

            out = new PrintWriter(
                    socket.getOutputStream(),
                    true
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public void sendOnlineUsers() {

        String users = "ONLINE:";

        for (String user : Server.clients.keySet()) {

            users += user + ",";
        }

        for (ClientHandler client :
                Server.clients.values()) {

            client.out.println(users);
        }
    }

    public void run() {

        try {

            // RECEIVE USERNAME
            username = br.readLine();

            // STORE CLIENT
            Server.clients.put(username, this);

            System.out.println(
                    username + " Connected"
            );

            // UPDATE ONLINE USERS
            sendOnlineUsers();

            while (true) {

                String msg = br.readLine();

                if (msg == null)
                    break;

                // FORMAT:
                // sender:receiver:message

                String[] parts =
                        msg.split(":", 3);

                String sender =
                        parts[0];

                String receiver =
                        parts[1];

                String text =
                        parts[2];

                // FIND RECEIVER
                ClientHandler target =
                        Server.clients.get(receiver);

                // SEND ONLY TO TARGET USER
                if (target != null) {

                    target.out.println(
                            sender + ":" + text
                    );
                }
        }} catch (Exception e) {

            System.out.println(
                    username + " Disconnected"
            );

        } finally {

            Server.clients.remove(username);

            sendOnlineUsers();

            try {

                socket.close();

            } catch (Exception e) {

                e.printStackTrace();
            }
        }
    }
}