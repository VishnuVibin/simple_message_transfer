import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;

public class WhatsAppUI {

    JFrame frame;

    JPanel leftPanel;
    JPanel rightPanel;

    JTextArea messages;
    JTextField input;
    JButton send;

    Socket socket;

    BufferedReader br;
    PrintWriter out;
    JPanel chatPanel;
    JScrollPane scrollPane;
    String username;    

    public WhatsAppUI() {

        try {

            // CONNECT TO SERVER
            username = JOptionPane.showInputDialog(frame,"Enter Username");
            socket = new Socket("localhost", 5000);

            br = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream()
                    )
            );

            out = new PrintWriter(
                    socket.getOutputStream(),
                    true
            );

            System.out.println("Connected To Server");

        } catch (Exception e) {

            e.printStackTrace();
        }

        // FRAME
        frame = new JFrame("WhatsApp Clone");

        frame.setSize(1000, 650);

        frame.setLayout(null);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // LEFT PANEL
        leftPanel = new JPanel();

        leftPanel.setBounds(0, 0, 300, 650);

        leftPanel.setBackground(new Color(17, 27, 33));

        leftPanel.setLayout(null);

        // TOP BAR
        JPanel topBar = new JPanel();

        topBar.setBounds(0, 0, 300, 70);

        topBar.setBackground(new Color(32, 44, 51));

        topBar.setLayout(null);

        JLabel profile = new JLabel("V");

        profile.setBounds(20, 15, 40, 40);

        profile.setOpaque(true);

        profile.setBackground(Color.GRAY);

        profile.setForeground(Color.WHITE);

        profile.setHorizontalAlignment(SwingConstants.CENTER);

        profile.setFont(new Font("Arial", Font.BOLD, 20));

        topBar.add(profile);

        // SEARCH BAR
        JTextField search = new JTextField();

        search.setBounds(15, 80, 270, 35);

        search.setBackground(new Color(32, 44, 51));

        search.setForeground(Color.WHITE);

        search.setCaretColor(Color.WHITE);

        search.setBorder(
                BorderFactory.createEmptyBorder(
                        5, 10, 5, 10
                )
        );

        search.setText("Search or start new chat");

        // CHAT LIST
        JPanel chat1 = createChat(
                "Arun",
                "Hey bro!",
                130
        );

        JPanel chat2 = createChat(
                "Kumar",
                "Project completed?",
                210
        );

        JPanel chat3 = createChat(
                "Rahul",
                "Call me",
                290
        );

        leftPanel.add(topBar);

        leftPanel.add(search);

        leftPanel.add(chat1);

        leftPanel.add(chat2);

        leftPanel.add(chat3);

        // RIGHT PANEL
        rightPanel = new JPanel();

        rightPanel.setBounds(300, 0, 700, 650);

        rightPanel.setBackground(new Color(11, 20, 26));

        rightPanel.setLayout(null);

        // CHAT HEADER
        JPanel header = new JPanel();

        header.setBounds(0, 0, 700, 70);

        header.setBackground(new Color(32, 44, 51));

        header.setLayout(null);

        JLabel dp = new JLabel("A");

        dp.setBounds(20, 15, 40, 40);

        dp.setOpaque(true);

        dp.setBackground(Color.GRAY);

        dp.setForeground(Color.WHITE);

        dp.setHorizontalAlignment(SwingConstants.CENTER);

        dp.setFont(new Font("Arial", Font.BOLD, 20));

        JLabel name = new JLabel("Chat Room");

        name.setBounds(80, 20, 200, 30);

        name.setForeground(Color.WHITE);

        name.setFont(new Font("Arial", Font.BOLD, 20));

        header.add(dp);

        header.add(name);

        // MESSAGE AREA
        chatPanel = new JPanel();

        chatPanel.setLayout(
                new BoxLayout(
                        chatPanel,
                        BoxLayout.Y_AXIS
                )
        );

        chatPanel.setBackground(
                new Color(11,20,26)
        );

        scrollPane = new JScrollPane(chatPanel);

        scrollPane.setBounds(20,90,660,430);

        scrollPane.setBorder(null);

        scrollPane.getVerticalScrollBar()
                .setUnitIncrement(16);

        rightPanel.add(scrollPane);

        // SCROLL PANE
        JScrollPane scroll = new JScrollPane(messages);

        scroll.setBounds(20, 90, 660, 430);

        // INPUT FIELD
        input = new JTextField();

        input.setBounds(20, 540, 540, 40);

        input.setBackground(new Color(32, 44, 51));

        input.setForeground(Color.WHITE);

        input.setCaretColor(Color.WHITE);

        input.setBorder(
                BorderFactory.createEmptyBorder(
                        5, 10, 5, 10
                )
        );

        input.setFont(
                new Font(
                        "Arial",
                        Font.PLAIN,
                        16
                )
        );

        // SEND BUTTON
        send = new JButton("Send");

        send.setBounds(580, 540, 100, 40);

        send.setBackground(new Color(0, 168, 132));

        send.setForeground(Color.WHITE);

        send.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        15
                )
        );

        send.setFocusPainted(false);

        // SEND MESSAGE
        send.addActionListener(e -> sendMessage());

        // ENTER KEY SEND
        input.addActionListener(e -> sendMessage());

        rightPanel.add(header);

        rightPanel.add(scroll);

        rightPanel.add(input);

        rightPanel.add(send);

        frame.add(leftPanel);

        frame.add(rightPanel);

        frame.setVisible(true);

        // START RECEIVING
        receiveMessages();
    }

    public void addMessage(
        String message,
        boolean isSender
) {

    JPanel wrapper = new JPanel(
            new FlowLayout(
                    isSender ?
                            FlowLayout.RIGHT :
                            FlowLayout.LEFT
            )
    );

    wrapper.setBackground(
            new Color(11,20,26)
    );

    JLabel msg = new JLabel(
            "<html><p style='width: 200px'>"
                    + message +
                    "</p></html>"
    );

    msg.setOpaque(true);

    msg.setFont(
            new Font(
                    "Arial",
                    Font.PLAIN,
                    15
            )
    );

    msg.setBorder(
            BorderFactory.createEmptyBorder(
                    10,15,10,15
            )
    );

    if(isSender){

        msg.setBackground(
                new Color(0,168,132)
        );

        msg.setForeground(Color.WHITE);

    } else {

        msg.setBackground(
                new Color(32,44,51)
        );

        msg.setForeground(Color.WHITE);
    }

    wrapper.add(msg);

    chatPanel.add(wrapper);

    chatPanel.revalidate();

    SwingUtilities.invokeLater(() -> {

        JScrollBar vertical =
                scrollPane.getVerticalScrollBar();

        vertical.setValue(
                vertical.getMaximum()
        );
    });
}

    // SEND METHOD
    public void sendMessage() {

    String msg = input.getText();

    if(!msg.isEmpty()) {

        out.println(username + ":" + msg);

        input.setText("");
    }
}

    // RECEIVE METHOD
    public void receiveMessages() {

    Thread thread = new Thread(() -> {

        try {

            while (true) {

                String msg = br.readLine();

                if(msg == null)
                    break;

                // Split username and message
                String[] parts = msg.split(":", 2);

                String sender = parts[0];

                String text = parts[1];

                // If my own message
                if(sender.equals(username)) {

                    addMessage(text, true);

                } else {

                    addMessage(sender + ": " + text, false);
                }
            }

        } catch (Exception e) {

            System.out.println("Disconnected");
        }
    });

    thread.start();
}

    // CHAT PANEL CREATOR
    static JPanel createChat(
            String name,
            String message,
            int y
    ) {

        JPanel panel = new JPanel();

        panel.setBounds(0, y, 300, 70);

        panel.setBackground(new Color(17, 27, 33));

        panel.setLayout(null);

        JLabel dp = new JLabel(
                name.substring(0, 1)
        );

        dp.setBounds(15, 15, 40, 40);

        dp.setOpaque(true);

        dp.setBackground(Color.GRAY);

        dp.setForeground(Color.WHITE);

        dp.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        dp.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        18
                )
        );

        JLabel user = new JLabel(name);

        user.setBounds(70, 10, 150, 25);

        user.setForeground(Color.WHITE);

        user.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        16
                )
        );

        JLabel msg = new JLabel(message);

        msg.setBounds(70, 35, 180, 20);

        msg.setForeground(Color.LIGHT_GRAY);

        msg.setFont(
                new Font(
                        "Arial",
                        Font.PLAIN,
                        13
                )
        );

        panel.add(dp);

        panel.add(user);

        panel.add(msg);

        return panel;
    }

    // MAIN METHOD
    public static void main(String[] args) {

        new WhatsAppUI();
    }
}