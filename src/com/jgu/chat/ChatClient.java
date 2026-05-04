package com.jgu.chat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Swing chat client. The client connects to ChatServer, sends messages, and
 * receives broadcasts in real-time on a background listener thread.
 */
public class ChatClient extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final String ROSTER_PREFIX = "__ROSTER__:";

    private static final Color WHATSAPP_GREEN = new Color(0, 168, 132);
    private static final Color WHATSAPP_DARK = new Color(32, 44, 51);
    private static final Color HEADER_DARK = new Color(24, 28, 31);
    private static final Color CHAT_BACKGROUND = new Color(11, 20, 26);
    private static final Color INPUT_BAR = new Color(32, 44, 51);
    private static final Color INPUT_FIELD = new Color(42, 57, 66);
    private static final Color OWN_BUBBLE = new Color(0, 96, 184);
    private static final Color OTHER_BUBBLE = new Color(31, 36, 37);
    private static final Color SERVER_BUBBLE = new Color(28, 36, 41);
    private static final Color TEXT_LIGHT = new Color(233, 237, 239);
    private static final Color MUTED_TEXT = new Color(134, 150, 160);
    private static final Color IOS_BLUE = new Color(10, 132, 255);
    private static final Color GROUP_AVATAR = new Color(55, 48, 139);

    private JPanel messagesPanel;
    private JScrollPane scrollPane;
    private JTextField messageField;
    private JButton sendButton;
    private JLabel statusLabel;

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private String username;

    public ChatClient(String host, int port, String username) {
        super("Kelompok 5 - " + username);
        this.username = username;

        buildUserInterface();
        connect(host, port);
        startMessageListener();
    }

    private void buildUserInterface() {
        UIManager.put("Button.select", WHATSAPP_DARK);

        JPanel headerWrapper = new JPanel(new BorderLayout());
        headerWrapper.setBackground(HEADER_DARK);

        JPanel headerPanel = new HeaderPanel();
        headerPanel.setLayout(new BorderLayout(12, 0));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftHeader.setOpaque(false);

        JLabel avatarLabel = new CircleLabel("K5", GROUP_AVATAR, Color.WHITE);
        avatarLabel.setFont(appFont(Font.BOLD, 13));
        avatarLabel.setPreferredSize(new Dimension(46, 46));

        leftHeader.add(avatarLabel);

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel nameLabel = new JLabel("Kelompok 5");
        nameLabel.setForeground(TEXT_LIGHT);
        nameLabel.setFont(appFont(Font.BOLD, 21));

        statusLabel = new JLabel("Connecting...");
        statusLabel.setForeground(MUTED_TEXT);
        statusLabel.setFont(appFont(Font.PLAIN, 13));

        titlePanel.add(nameLabel);
        titlePanel.add(Box.createVerticalStrut(1));
        titlePanel.add(statusLabel);

        headerPanel.add(leftHeader, BorderLayout.WEST);
        headerPanel.add(titlePanel, BorderLayout.CENTER);

        headerWrapper.add(headerPanel, BorderLayout.CENTER);

        messagesPanel = new JPanel();
        messagesPanel.setBackground(CHAT_BACKGROUND);
        messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
        messagesPanel.setBorder(BorderFactory.createEmptyBorder(16, 14, 16, 14));

        scrollPane = new JScrollPane(messagesPanel);
        scrollPane.setPreferredSize(new Dimension(430, 640));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(CHAT_BACKGROUND);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));

        messageField = new RoundedTextField("Type a message");
        messageField.setFont(appFont(Font.PLAIN, 14));
        messageField.setForeground(TEXT_LIGHT);
        messageField.setCaretColor(TEXT_LIGHT);
        messageField.setSelectedTextColor(Color.WHITE);
        messageField.setSelectionColor(new Color(0, 128, 105));
        messageField.setBorder(BorderFactory.createEmptyBorder(11, 16, 11, 16));

        sendButton = new RoundedSendButton("Send");
        sendButton.setFont(appFont(Font.BOLD, 14));
        sendButton.setForeground(Color.WHITE);
        sendButton.setBackground(WHATSAPP_GREEN);
        sendButton.setFocusPainted(false);
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        sendButton.setPreferredSize(new Dimension(86, 46));
        sendButton.setToolTipText("Send message");

        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(CHAT_BACKGROUND);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 14, 16));

        GridBagConstraints inputConstraints = new GridBagConstraints();
        inputConstraints.gridx = 0;
        inputConstraints.gridy = 0;
        inputConstraints.weightx = 1.0;
        inputConstraints.fill = GridBagConstraints.HORIZONTAL;
        inputConstraints.insets = new Insets(0, 0, 0, 12);
        inputPanel.add(messageField, inputConstraints);

        inputConstraints.gridx = 1;
        inputConstraints.weightx = 0;
        inputConstraints.fill = GridBagConstraints.NONE;
        inputConstraints.insets = new Insets(0, 0, 0, 0);
        inputPanel.add(sendButton, inputConstraints);

        setLayout(new BorderLayout());
        add(headerWrapper, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);

        ActionListener sendAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        };
        sendButton.addActionListener(sendAction);
        messageField.addActionListener(sendAction);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeConnection();
            }
        });

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setMinimumSize(new Dimension(390, 580));
        setLocationRelativeTo(null);
    }

    private void connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

            writer.write(username);
            writer.newLine();
            writer.flush();

            statusLabel.setText(username);
            appendMessage("You joined the chat as " + username + ".");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Cannot connect to server: " + e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void startMessageListener() {
        Thread listener = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String message;
                    while ((message = reader.readLine()) != null) {
                        if (message.startsWith(ROSTER_PREFIX)) {
                            updateRoster(message.substring(ROSTER_PREFIX.length()));
                        } else {
                            appendMessage(message);
                        }
                    }
                } catch (IOException e) {
                    appendMessage("Disconnected from server.");
                } finally {
                    closeConnection();
                }
            }
        }, "ServerMessageListener");
        listener.setDaemon(true);
        listener.start();
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (message.isEmpty()) {
            return;
        }

        try {
            writer.write(message);
            writer.newLine();
            writer.flush();
            appendMessage("Me [" + currentTime() + "]: " + message);
            messageField.setText("");
            messageField.requestFocusInWindow();
        } catch (IOException e) {
            appendMessage("Message failed: " + e.getMessage());
        }
    }

    private void appendMessage(final String message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                addMessageBubble(message);
                messagesPanel.revalidate();
                messagesPanel.repaint();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
                    }
                });
            }
        });
    }

    private void updateRoster(final String rosterText) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                String[] names = rosterText.trim().isEmpty() ? new String[0] : rosterText.split(",");
                StringBuilder display = new StringBuilder();

                for (int i = 0; i < names.length; i++) {
                    String name = names[i].trim();
                    if (name.isEmpty()) {
                        continue;
                    }
                    if (display.length() > 0) {
                        display.append(", ");
                    }
                    display.append(name);
                }

                if (display.length() == 0) {
                    display.append(username);
                }
                statusLabel.setText(display.toString());
            }
        });
    }

    private void addMessageBubble(String message) {
        ChatMessage chatMessage = ChatMessage.parse(message);
        boolean ownMessage = chatMessage.own;
        boolean serverMessage = message.contains("[SERVER]") || message.startsWith("You joined") || message.startsWith("Disconnected")
                || message.startsWith("Message failed");

        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        if (serverMessage) {
            JLabel label = new JLabel("<html><body style='width: 280px; text-align:center;'>" + html(cleanServerMessage(message)) + "</body></html>");
            label.setFont(appFont(Font.PLAIN, 12));
            label.setForeground(MUTED_TEXT);

            RoundedPanel bubble = new RoundedPanel(SERVER_BUBBLE, 18, false);
            bubble.setLayout(new BorderLayout());
            bubble.setBorder(BorderFactory.createEmptyBorder(7, 13, 7, 13));
            bubble.add(label, BorderLayout.CENTER);

            JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            center.setOpaque(false);
            center.add(bubble);
            row.add(center, BorderLayout.CENTER);
        } else {
            JPanel content = new JPanel();
            content.setOpaque(false);
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

            if (!ownMessage) {
                JLabel senderLabel = new JLabel(chatMessage.sender);
                senderLabel.setForeground(senderColor(chatMessage.sender));
                senderLabel.setFont(appFont(Font.BOLD, 13));
                senderLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                content.add(senderLabel);
                content.add(Box.createVerticalStrut(3));
            }

            JLabel textLabel = new JLabel("<html><body style='width: 230px; line-height: 1.22;'>" + html(chatMessage.text) + "</body></html>");
            textLabel.setFont(appFont(Font.PLAIN, 17));
            textLabel.setForeground(TEXT_LIGHT);
            textLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel timeLabel = new JLabel(chatMessage.time);
            timeLabel.setFont(appFont(Font.PLAIN, 12));
            timeLabel.setForeground(ownMessage ? new Color(180, 213, 241) : MUTED_TEXT);
            timeLabel.setHorizontalAlignment(JLabel.RIGHT);
            timeLabel.setPreferredSize(new Dimension(54, 16));

            content.add(textLabel);

            JPanel timeRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            timeRow.setOpaque(false);
            timeRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            timeRow.add(timeLabel);
            content.add(Box.createVerticalStrut(3));
            content.add(timeRow);

            RoundedPanel bubble = new RoundedPanel(ownMessage ? OWN_BUBBLE : OTHER_BUBBLE, 18, true);
            bubble.setLayout(new BorderLayout());
            bubble.setBorder(BorderFactory.createEmptyBorder(10, 14, 8, 14));
            bubble.add(content, BorderLayout.CENTER);

            bubble.setMaximumSize(new Dimension(310, Integer.MAX_VALUE));

            JPanel side = new JPanel(new FlowLayout(ownMessage ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
            side.setOpaque(false);

            if (!ownMessage) {
                JLabel smallAvatar = new CircleLabel(initials(chatMessage.sender), senderColor(chatMessage.sender), Color.WHITE);
                smallAvatar.setFont(appFont(Font.BOLD, 12));
                smallAvatar.setPreferredSize(new Dimension(34, 34));
                side.add(smallAvatar);
                side.add(Box.createHorizontalStrut(8));
            }

            side.add(bubble);
            row.add(side, ownMessage ? BorderLayout.EAST : BorderLayout.WEST);
        }

        messagesPanel.add(row);
        messagesPanel.add(Box.createVerticalStrut(2));
    }

    private static Font appFont(int style, int size) {
        return new Font("Segoe UI", style, size);
    }

    private static String currentTime() {
        return new SimpleDateFormat("HH.mm").format(new Date());
    }

    private static String cleanServerMessage(String value) {
        int serverIndex = value.indexOf("[SERVER]");
        if (serverIndex >= 0) {
            return value.substring(serverIndex + "[SERVER]".length()).trim();
        }
        return value;
    }

    private static Color senderColor(String sender) {
        Color[] colors = {
            new Color(255, 203, 107),
            new Color(123, 220, 181),
            new Color(189, 147, 249),
            new Color(82, 201, 198),
            new Color(255, 132, 164)
        };
        int index = Math.abs(sender.hashCode()) % colors.length;
        return colors[index];
    }

    private static String html(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace(System.lineSeparator(), "<br>");
    }

    private static String initials(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "DC";
        }
        String[] parts = value.trim().split("\\s+");
        String result = parts[0].substring(0, 1);
        if (parts.length > 1) {
            result += parts[1].substring(0, 1);
        }
        return result.toUpperCase();
    }

    private void closeConnection() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignored) {
            // Socket is already closed.
        }
    }

    public static void main(String[] args) {
        final ConnectionInfo info;
        if (args.length >= 3) {
            info = new ConnectionInfo(args[0], parsePort(args[1]), args[2]);
        } else {
            info = ConnectionDialog.showDialog(null);
            if (info == null) {
                return;
            }
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ChatClient(info.host, info.port, info.username).setVisible(true);
            }
        });
    }

    private static int parsePort(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return ChatServer.DEFAULT_PORT;
        }
    }

    private static class ChatMessage {
        private final String sender;
        private final String text;
        private final String time;
        private final boolean own;

        ChatMessage(String sender, String text, String time, boolean own) {
            this.sender = sender;
            this.text = text;
            this.time = time;
            this.own = own;
        }

        static ChatMessage parse(String raw) {
            if (raw.startsWith("Me [")) {
                int endTime = raw.indexOf("]:");
                if (endTime > 4) {
                    return new ChatMessage("Me", raw.substring(endTime + 2).trim(), raw.substring(4, endTime), true);
                }
            }
            if (raw.startsWith("Me:")) {
                return new ChatMessage("Me", raw.substring(3).trim(), currentTime(), true);
            }
            if (raw.startsWith("[")) {
                int endTime = raw.indexOf("]");
                int colon = raw.indexOf(": ", endTime);
                if (endTime > 0 && colon > endTime) {
                    String time = raw.substring(1, endTime);
                    if (time.length() >= 5) {
                        time = time.substring(0, 5).replace(":", ".");
                    }
                    String sender = raw.substring(endTime + 1, colon).trim();
                    String text = raw.substring(colon + 2).trim();
                    return new ChatMessage(sender, text, time, false);
                }
            }
            return new ChatMessage("Friend", raw, currentTime(), false);
        }
    }

    private static class ConnectionInfo {
        private final String host;
        private final int port;
        private final String username;

        ConnectionInfo(String host, int port, String username) {
            this.host = host;
            this.port = port;
            this.username = username;
        }
    }

    private static class ConnectionDialog extends JDialog {
        private static final long serialVersionUID = 1L;

        private final JTextField usernameField = new RoundedTextField("Username");
        private ConnectionInfo result;

        private ConnectionDialog(Window owner) {
            super(owner, "Join Kelompok 5", ModalityType.APPLICATION_MODAL);
            build();
        }

        static ConnectionInfo showDialog(Window owner) {
            ConnectionDialog dialog = new ConnectionDialog(owner);
            dialog.setVisible(true);
            return dialog.result;
        }

        private void build() {
            JPanel root = new DialogRootPanel();
            root.setLayout(new BorderLayout());
            root.setBackground(CHAT_BACKGROUND);
            root.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));

            JPanel header = new JPanel(new BorderLayout(12, 0));
            header.setOpaque(false);

            JLabel avatar = new CircleLabel("K5", GROUP_AVATAR, Color.WHITE);
            avatar.setFont(appFont(Font.BOLD, 14));
            avatar.setPreferredSize(new Dimension(50, 50));

            JPanel titlePanel = new JPanel();
            titlePanel.setOpaque(false);
            titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

            JLabel title = new JLabel("Join Kelompok 5");
            title.setForeground(TEXT_LIGHT);
            title.setFont(appFont(Font.BOLD, 21));

            JLabel subtitle = new JLabel("Masukkan username untuk masuk ke chat");
            subtitle.setForeground(MUTED_TEXT);
            subtitle.setFont(appFont(Font.PLAIN, 13));

            titlePanel.add(title);
            titlePanel.add(Box.createVerticalStrut(3));
            titlePanel.add(subtitle);
            header.add(avatar, BorderLayout.WEST);
            header.add(titlePanel, BorderLayout.CENTER);

            JPanel form = new JPanel(new GridBagLayout());
            form.setOpaque(false);
            form.setBorder(BorderFactory.createEmptyBorder(26, 0, 20, 0));

            styleDialogField(usernameField, "Student");

            addFormRow(form, 0, "Username", usernameField);

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            buttons.setOpaque(false);

            JButton cancelButton = new DialogButton("Cancel", false);
            JButton joinButton = new DialogButton("Join", true);
            buttons.add(cancelButton);
            buttons.add(joinButton);

            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    result = null;
                    dispose();
                }
            });

            joinButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    submit();
                }
            });

            ActionListener submitAction = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    submit();
                }
            };
            usernameField.addActionListener(submitAction);

            root.add(header, BorderLayout.NORTH);
            root.add(form, BorderLayout.CENTER);
            root.add(buttons, BorderLayout.SOUTH);

            setContentPane(root);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setSize(440, 286);
            setResizable(false);
            setLocationRelativeTo(getOwner());
        }

        private void styleDialogField(JTextField field, String value) {
            field.setText(value);
            field.setFont(appFont(Font.PLAIN, 14));
            field.setForeground(TEXT_LIGHT);
            field.setCaretColor(TEXT_LIGHT);
            field.setSelectionColor(new Color(0, 128, 105));
            field.setSelectedTextColor(Color.WHITE);
            field.setBorder(BorderFactory.createEmptyBorder(11, 16, 11, 16));
        }

        private void addFormRow(JPanel form, int row, String labelText, JTextField field) {
            JLabel label = new JLabel(labelText);
            label.setForeground(MUTED_TEXT);
            label.setFont(appFont(Font.BOLD, 12));

            GridBagConstraints labelConstraints = new GridBagConstraints();
            labelConstraints.gridx = 0;
            labelConstraints.gridy = row;
            labelConstraints.anchor = GridBagConstraints.WEST;
            labelConstraints.insets = new Insets(0, 0, 8, 16);
            form.add(label, labelConstraints);

            GridBagConstraints fieldConstraints = new GridBagConstraints();
            fieldConstraints.gridx = 1;
            fieldConstraints.gridy = row;
            fieldConstraints.weightx = 1.0;
            fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
            fieldConstraints.insets = new Insets(0, 0, 8, 0);
            form.add(field, fieldConstraints);
        }

        private void submit() {
            String username = usernameField.getText().trim();

            if (username.isEmpty()) {
                username = "Student";
            }

            result = new ConnectionInfo("localhost", ChatServer.DEFAULT_PORT, username);
            dispose();
        }
    }

    private static class DialogButton extends JButton {
        private static final long serialVersionUID = 1L;

        private final boolean primary;

        DialogButton(String text, boolean primary) {
            super(text);
            this.primary = primary;
            setForeground(primary ? Color.WHITE : TEXT_LIGHT);
            setFont(appFont(Font.BOLD, 13));
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setContentAreaFilled(false);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(11, 22, 11, 22));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color color = primary ? WHATSAPP_GREEN : INPUT_FIELD;
            if (getModel().isPressed()) {
                color = primary ? new Color(0, 135, 108) : new Color(49, 67, 77);
            }
            g2.setColor(color);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class DialogRootPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(CHAT_BACKGROUND);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(new Color(47, 62, 72));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            g2.dispose();
        }
    }

    private static class HeaderPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(HEADER_DARK);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(new Color(20, 30, 36));
            g2.fillRect(0, getHeight() - 1, getWidth(), 1);
            g2.dispose();
        }
    }

    private static class CircleLabel extends JLabel {
        private static final long serialVersionUID = 1L;

        private final Color background;
        private final Color foreground;

        CircleLabel(String text, Color background, Color foreground) {
            super(text, JLabel.CENTER);
            this.background = background;
            this.foreground = foreground;
            setOpaque(false);
            setForeground(foreground);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int size = Math.min(getWidth(), getHeight());
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;
            g2.setColor(background);
            g2.fillOval(x, y, size, size);
            g2.setColor(new Color(255, 255, 255, 80));
            g2.drawOval(x, y, size - 1, size - 1);
            g2.dispose();
            setForeground(foreground);
            super.paintComponent(g);
        }
    }

    private static class CircleButton extends JButton {
        private static final long serialVersionUID = 1L;

        CircleButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder());
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isPressed() ? WHATSAPP_DARK : getBackground());
            g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class RoundedSendButton extends JButton {
        private static final long serialVersionUID = 1L;

        RoundedSendButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isPressed() ? new Color(0, 135, 108) : getBackground());
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class RoundedTextField extends JTextField {
        private static final long serialVersionUID = 1L;

        private final String hint;

        RoundedTextField(String hint) {
            this.hint = hint;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(INPUT_FIELD);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            g2.setColor(new Color(64, 84, 95));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
            g2.dispose();
            super.paintComponent(g);

            if (getText().isEmpty() && !isFocusOwner()) {
                Graphics2D hintGraphics = (Graphics2D) g.create();
                hintGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                hintGraphics.setColor(MUTED_TEXT);
                hintGraphics.setFont(getFont());
                Insets insets = getInsets();
                int y = (getHeight() + hintGraphics.getFontMetrics().getAscent()
                        - hintGraphics.getFontMetrics().getDescent()) / 2;
                hintGraphics.drawString(hint, insets.left, y);
                hintGraphics.dispose();
            }
        }
    }

    private static class RoundedPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        private final Color background;
        private final int radius;
        private final boolean shadow;

        RoundedPanel(Color background, int radius, boolean shadow) {
            this.background = background;
            this.radius = radius;
            this.shadow = shadow;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (shadow) {
                g2.setColor(new Color(0, 0, 0, 70));
                g2.fillRoundRect(2, 3, getWidth() - 4, getHeight() - 4, radius, radius);
            }
            g2.setColor(background);
            g2.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 4, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
