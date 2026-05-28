import javax.swing.*;
import java.awt.*;
import java.net.*;
import java.io.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

public class WhatsAppUI {

    JFrame frame;

    JPanel leftPanel;
    JPanel rightPanel;

    JPanel onlineUsersPanel;

    JTextField input;

    JButton send;

    JPanel chatPanel;

    JScrollPane scrollPane;

    Socket socket;

    BufferedReader br;

    PrintWriter out;

    String username;

    String currentChatUser = "";

    JLabel chatHeaderName;

    // CHAT HISTORY
    HashMap<String,
            ArrayList<String>>
            chatHistory =
            new HashMap<>();

    // UNREAD COUNTS
    HashMap<String, Integer>
            unreadCounts =
            new HashMap<>();

    // ONLINE USERS
    ArrayList<String> onlineUsers =
            new ArrayList<>();

    public WhatsAppUI() {

        username = JOptionPane.showInputDialog(
                null,
                "Enter Username"
        );

        try {

            socket = new Socket(
                    "localhost",
                    5000
            );

            br = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream()
                    )
            );

            out = new PrintWriter(
                    socket.getOutputStream(),
                    true
            );

            // SEND USERNAME
            out.println(username);

        } catch (Exception e) {

            e.printStackTrace();
        }

        // FRAME
        frame = new JFrame(
                "WhatsApp Clone"
        );

        frame.setSize(1000, 650);

        frame.setLayout(null);

        frame.setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        // LEFT PANEL
        leftPanel = new JPanel();

        leftPanel.setBounds(0, 0, 300, 650);

        leftPanel.setBackground(
                new Color(17, 27, 33)
        );

        leftPanel.setLayout(null);

        // TOP BAR
        JPanel topBar = new JPanel();

        topBar.setBounds(0, 0, 300, 70);

        topBar.setBackground(
                new Color(32, 44, 51)
        );

        topBar.setLayout(null);

        JLabel profile = new JLabel(
                username.substring(0,1)
                        .toUpperCase()
        );

        profile.setBounds(20,15,40,40);

        profile.setOpaque(true);

        profile.setBackground(Color.GRAY);

        profile.setForeground(Color.WHITE);

        profile.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        profile.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        20
                )
        );

        topBar.add(profile);

        // SEARCH BAR
        JTextField search =
                new JTextField();

        search.setBounds(15,80,270,35);

        search.setBackground(
                new Color(32,44,51)
        );

        search.setForeground(Color.WHITE);

        search.setCaretColor(Color.WHITE);

        search.setBorder(
                BorderFactory.createEmptyBorder(
                        5,10,5,10
                )
        );

        search.setText(
                "Search or start new chat"
        );

        // ONLINE USERS PANEL
        onlineUsersPanel = new JPanel();

        onlineUsersPanel.setLayout(
                new BoxLayout(
                        onlineUsersPanel,
                        BoxLayout.Y_AXIS
                )
        );

        onlineUsersPanel.setBackground(
                new Color(17,27,33)
        );

        JScrollPane userScroll =
                new JScrollPane(
                        onlineUsersPanel
                );

        userScroll.setBounds(
                0,130,300,500
        );

        leftPanel.add(topBar);

        leftPanel.add(search);

        leftPanel.add(userScroll);

        // RIGHT PANEL
        rightPanel = new JPanel();

        rightPanel.setBounds(
                300,0,700,650
        );

        rightPanel.setBackground(
                new Color(11,20,26)
        );

        rightPanel.setLayout(null);

        // HEADER
        JPanel header = new JPanel();

        header.setBounds(0,0,700,70);

        header.setBackground(
                new Color(32,44,51)
        );

        header.setLayout(null);

        JLabel dp = new JLabel("C");

        dp.setBounds(20,15,40,40);

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
                        20
                )
        );

        chatHeaderName =
                new JLabel("Select User");

        chatHeaderName.setBounds(
                80,20,300,30
        );

        chatHeaderName.setForeground(
                Color.WHITE
        );

        chatHeaderName.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        20
                )
        );

        header.add(dp);

        header.add(chatHeaderName);

        // CHAT PANEL
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

        scrollPane =
                new JScrollPane(chatPanel);

        scrollPane.setBounds(
                20,90,660,430
        );

        scrollPane.setBorder(null);

        // INPUT
        input = new JTextField();

        input.setBounds(
                20,540,540,40
        );

        input.setBackground(
                new Color(32,44,51)
        );

        input.setForeground(Color.WHITE);

        input.setCaretColor(Color.WHITE);

        input.setBorder(
                BorderFactory.createEmptyBorder(
                        5,10,5,10
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

        send.setBounds(
                580,540,100,40
        );

        send.setBackground(
                new Color(0,168,132)
        );

        send.setForeground(Color.WHITE);

        send.setFocusPainted(false);

        send.addActionListener(
                e -> sendMessage()
        );

        input.addActionListener(
                e -> sendMessage()
        );

        rightPanel.add(header);

        rightPanel.add(scrollPane);

        rightPanel.add(input);

        rightPanel.add(send);

        frame.add(leftPanel);

        frame.add(rightPanel);

        frame.setVisible(true);

        receiveMessages();
    }

    // SEND MESSAGE
    public void sendMessage() {

        String msg = input.getText();

        if (!msg.isEmpty()
                &&
                !currentChatUser.isEmpty()) {

            addMessage(
                    "You: " + msg,
                    true
            );

            // SAVE HISTORY
            chatHistory
                    .computeIfAbsent(
                            currentChatUser,
                            k -> new ArrayList<>()
                    )
                    .add("You: " + msg);

            out.println(
                    username
                            + ":"
                            + currentChatUser
                            + ":"
                            + msg
            );

            input.setText("");
        }
    }

    // RECEIVE MESSAGES
    public void receiveMessages() {

        Thread thread = new Thread(() -> {

            try {

                while (true) {

                    String msg =
                            br.readLine();

                    if (msg == null)
                        break;

                    // ONLINE USERS
                    if(msg.startsWith(
                            "ONLINE:"
                    )){

                        updateOnlineUsers(msg);

                        continue;
                    }

                    String[] parts =
                            msg.split(":",2);

                    String sender =
                            parts[0];

                    String text =
                            parts[1];

                    // SAVE HISTORY
                    chatHistory
                            .computeIfAbsent(
                                    sender,
                                    k -> new ArrayList<>()
                            )
                            .add(sender + ": " + text);

                    if(currentChatUser.equals(sender)) {

                        addMessage(
                                sender + ": " + text,
                                false
                        );

                    } else {

                        unreadCounts.put(
                                sender,
                                unreadCounts.getOrDefault(
                                        sender,
                                        0
                                ) + 1
                        );

                        updateOnlineUsersListUI();
                    }
                }

            } catch (Exception e) {

                System.out.println(
                        "Disconnected"
                );
            }
        });

        thread.start();
    }

    // UPDATE ONLINE USERS
    public void updateOnlineUsers(
            String users
    ){

        String data =
                users.replace(
                        "ONLINE:",
                        ""
                );

        String[] userList =
                data.split(",");

        onlineUsers.clear();

        for(String user : userList){

            if(user.trim().isEmpty())
                continue;

            if(user.equals(username))
                continue;

            onlineUsers.add(user);

            unreadCounts.putIfAbsent(
                    user,
                    0
            );
        }

        updateOnlineUsersListUI();
    }

    // REFRESH USER LIST
    public void updateOnlineUsersListUI(){

        onlineUsersPanel.removeAll();

        for(String user : onlineUsers){

            JPanel userPanel =
                    new JPanel();

            userPanel.setLayout(
                    new BorderLayout()
            );

            userPanel.setMaximumSize(
                    new Dimension(
                            280,
                            50
                    )
            );

            userPanel.setBackground(
                    new Color(17,27,33)
            );

            int count =
                    unreadCounts.getOrDefault(
                            user,
                            0
                    );

            String text =
                    "🟢 " + user;

            if(count > 0){

                text += " (" + count + ")";
            }

            JLabel label =
                    new JLabel(text);

            label.setForeground(
                    Color.WHITE
            );

            label.setFont(
                    new Font(
                            "Arial",
                            Font.BOLD,
                            16
                    )
            );

            label.setBorder(
                    BorderFactory
                            .createEmptyBorder(
                                    10,10,10,10
                            )
            );

            userPanel.add(label);

            // CLICK USER
            userPanel.addMouseListener(
                    new java.awt.event
                            .MouseAdapter() {

                        public void mouseClicked(
                                java.awt.event
                                        .MouseEvent evt
                        ) {

                            currentChatUser =
                                    user;

                            chatHeaderName
                                    .setText(user);

                            unreadCounts.put(
                                    user,
                                    0
                            );

                            loadChat(user);

                            updateOnlineUsersListUI();
                        }
                    }
            );

            onlineUsersPanel.add(
                    userPanel
            );
        }

        onlineUsersPanel.revalidate();

        onlineUsersPanel.repaint();
    }

    // LOAD CHAT
    public void loadChat(String user){

        chatPanel.removeAll();

        if(chatHistory.containsKey(user)) {

            for(String oldMsg :
                    chatHistory.get(user)) {

                boolean isMine =
                        oldMsg.startsWith("You:");

                addMessage(
                        oldMsg,
                        isMine
                );
            }
        }

        chatPanel.revalidate();

        chatPanel.repaint();
    }

    // ADD MESSAGE BUBBLE
    public void addMessage(
            String message,
            boolean isSender
    ){

        String time =
                LocalTime.now()
                        .format(
                                DateTimeFormatter
                                        .ofPattern(
                                                "hh:mm a"
                                        )
                        );

        JPanel wrapper =
                new JPanel(
                        new FlowLayout(
                                isSender ?
                                        FlowLayout.RIGHT :
                                        FlowLayout.LEFT
                        )
                );

        wrapper.setBackground(
                new Color(11,20,26)
        );

        JLabel msg =
                new JLabel(
                        "<html><p style='width: 200px'>"
                                + message
                                + "<br><br><small>"
                                + time
                                + "</small></p></html>"
                );

        msg.setOpaque(true);

        msg.setForeground(Color.WHITE);

        msg.setFont(
                new Font(
                        "Arial",
                        Font.PLAIN,
                        15
                )
        );

        msg.setBorder(
                BorderFactory
                        .createEmptyBorder(
                                10,15,10,15
                        )
        );

        if(isSender){

            msg.setBackground(
                    new Color(0,168,132)
            );

        } else {

            msg.setBackground(
                    new Color(32,44,51)
            );
        }

        wrapper.add(msg);

        chatPanel.add(wrapper);

        chatPanel.revalidate();

        SwingUtilities.invokeLater(() -> {

            JScrollBar vertical =
                    scrollPane
                            .getVerticalScrollBar();

            vertical.setValue(
                    vertical.getMaximum()
            );
        });
    }

    public static void main(String[] args) {

        new WhatsAppUI();
    }
}