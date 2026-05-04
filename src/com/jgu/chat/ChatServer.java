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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Multi-client chat server using Java Socket Programming.
 *
 * The server accepts many clients at the same time. Every client is handled by
 * its own thread, and messages are broadcast to all other connected clients.
 */
public class ChatServer {
    public static final int DEFAULT_PORT = 5000;
    private static final String ROSTER_PREFIX = "__ROSTER__:";

    private static final Color WHATSAPP_GREEN = new Color(0, 168, 132);
    private static final Color WHATSAPP_DARK = new Color(32, 44, 51);
    private static final Color HEADER_DARK = new Color(24, 28, 31);
    private static final Color CHAT_BACKGROUND = new Color(11, 20, 26);
    private static final Color LOG_BACKGROUND = new Color(13, 24, 31);
    private static final Color INPUT_FIELD = new Color(42, 57, 66);
    private static final Color OTHER_BUBBLE = new Color(31, 36, 37);
    private static final Color SERVER_BUBBLE = new Color(28, 36, 41);
    private static final Color TEXT_LIGHT = new Color(233, 237, 239);
    private static final Color MUTED_TEXT = new Color(134, 150, 160);
    private static final Color GROUP_AVATAR = new Color(55, 48, 139);

    private final int port;
    private final Set<ClientHandler> clients = Collections.synchronizedSet(new LinkedHashSet<ClientHandler>());

    private JFrame frame;
    private JLabel statusLabel;
    private JLabel clientCountLabel;
    private JPanel logPanel;
    private JPanel clientsPanel;
    private JScrollPane logScrollPane;
    private JButton stopButton;
    private ServerSocket serverSocket;
    private volatile boolean running;

    public ChatServer(int port) {
        this.port = port;
    }

    public void showAndStart() {
        buildUserInterface();
        frame.setVisible(true);

        Thread serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                start();
            }
        }, "ChatServer-Main");
        serverThread.start();
    }

    public void start() {
        running = true;
        log("Chat server starting on port " + port + "...", true);
        updateStatus("Starting on port " + port);

        try (ServerSocket socket = new ServerSocket(port)) {
            serverSocket = socket;
            log("Server is ready. Waiting for clients...", true);
            updateStatus("Online on port " + port);

            while (running) {
                Socket clientSocket = socket.accept();
                ClientHandler handler = new ClientHandler(clientSocket);
                clients.add(handler);
                new Thread(handler, "ClientHandler-" + clientSocket.getPort()).start();
            }
        } catch (IOException e) {
            if (running) {
                log("Server error: " + e.getMessage(), false);
                updateStatus("Server error");
            }
        } finally {
            running = false;
            updateStatus("Offline");
        }
    }

    private void buildUserInterface() {
        UIManager.put("Button.select", WHATSAPP_DARK);

        frame = new JFrame("Kelompok 5 - Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(960, 710));
        frame.setMinimumSize(new Dimension(760, 560));
        frame.setLayout(new BorderLayout());

        JPanel headerWrapper = new JPanel(new BorderLayout());
        headerWrapper.setBackground(HEADER_DARK);

        JPanel headerPanel = new HeaderPanel();
        headerPanel.setLayout(new BorderLayout(14, 0));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftHeader.setOpaque(false);

        JLabel avatarLabel = new CircleLabel("SV", GROUP_AVATAR, Color.WHITE);
        avatarLabel.setFont(appFont(Font.BOLD, 13));
        avatarLabel.setPreferredSize(new Dimension(48, 48));
        leftHeader.add(avatarLabel);

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel nameLabel = new JLabel("Kelompok 5 Server");
        nameLabel.setForeground(TEXT_LIGHT);
        nameLabel.setFont(appFont(Font.BOLD, 22));

        statusLabel = new JLabel("Preparing server...");
        statusLabel.setForeground(MUTED_TEXT);
        statusLabel.setFont(appFont(Font.PLAIN, 13));

        titlePanel.add(nameLabel);
        titlePanel.add(Box.createVerticalStrut(2));
        titlePanel.add(statusLabel);

        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightHeader.setOpaque(false);

        clientCountLabel = new JLabel("0 online");
        clientCountLabel.setForeground(TEXT_LIGHT);
        clientCountLabel.setFont(appFont(Font.BOLD, 13));
        clientCountLabel.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        stopButton = new RoundedButton("Stop Server");
        stopButton.setFont(appFont(Font.BOLD, 13));
        stopButton.setForeground(Color.WHITE);
        stopButton.setBackground(WHATSAPP_GREEN);
        stopButton.setFocusPainted(false);
        stopButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        stopButton.addActionListener(e -> stopServer());

        rightHeader.add(clientCountLabel);
        rightHeader.add(stopButton);

        headerPanel.add(leftHeader, BorderLayout.WEST);
        headerPanel.add(titlePanel, BorderLayout.CENTER);
        headerPanel.add(rightHeader, BorderLayout.EAST);
        headerWrapper.add(headerPanel, BorderLayout.CENTER);

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(CHAT_BACKGROUND);
        root.setBorder(BorderFactory.createEmptyBorder(20, 22, 22, 22));

        clientsPanel = new JPanel();
        clientsPanel.setOpaque(false);
        clientsPanel.setLayout(new BoxLayout(clientsPanel, BoxLayout.Y_AXIS));
        clientsPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel clientsCard = createSection("Online Clients", clientsPanel);
        clientsCard.setPreferredSize(new Dimension(250, 500));

        logPanel = new JPanel();
        logPanel.setBackground(LOG_BACKGROUND);
        logPanel.setLayout(new BoxLayout(logPanel, BoxLayout.Y_AXIS));
        logPanel.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        logScrollPane = new JScrollPane(logPanel);
        logScrollPane.setBorder(BorderFactory.createEmptyBorder());
        logScrollPane.getViewport().setBackground(LOG_BACKGROUND);
        logScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        logScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        logScrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));

        JPanel logCard = createSection("Server Activity", logScrollPane);

        GridBagConstraints clientsConstraints = new GridBagConstraints();
        clientsConstraints.gridx = 0;
        clientsConstraints.gridy = 0;
        clientsConstraints.weightx = 0;
        clientsConstraints.weighty = 1.0;
        clientsConstraints.fill = GridBagConstraints.BOTH;
        clientsConstraints.insets = new Insets(0, 0, 0, 18);
        root.add(clientsCard, clientsConstraints);

        GridBagConstraints logConstraints = new GridBagConstraints();
        logConstraints.gridx = 1;
        logConstraints.gridy = 0;
        logConstraints.weightx = 1.0;
        logConstraints.weighty = 1.0;
        logConstraints.fill = GridBagConstraints.BOTH;
        root.add(logCard, logConstraints);

        frame.add(headerWrapper, BorderLayout.NORTH);
        frame.add(root, BorderLayout.CENTER);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stopServer();
            }
        });

        updateClientList();
        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    private JPanel createSection(String title, Component content) {
        JPanel section = new RoundedPanel(SERVER_BUBBLE, 16, true);
        section.setLayout(new BorderLayout());
        section.setBorder(BorderFactory.createEmptyBorder(16, 18, 18, 18));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(TEXT_LIGHT);
        titleLabel.setFont(appFont(Font.BOLD, 15));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));

        section.add(titleLabel, BorderLayout.NORTH);
        section.add(content, BorderLayout.CENTER);
        return section;
    }

    private void stopServer() {
        running = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException ignored) {
            // Server socket is already closed.
        }

        synchronized (clients) {
            for (ClientHandler client : clients) {
                client.close();
            }
        }
        log("Server stopped.", false);
        updateStatus("Offline");
    }

    private void broadcast(String message, ClientHandler sender) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client != sender) {
                    client.send(message);
                }
            }
        }
    }

    private void removeClient(ClientHandler client) {
        clients.remove(client);
        String username = client.getUsername();
        if (username != null && !username.trim().isEmpty()) {
            String leaveMessage = systemMessage(username + " left the chat.");
            log(leaveMessage, false);
            broadcast(leaveMessage, client);
        }
        printConnectedClients();
        broadcastRoster();
        updateClientList();
    }

    private void broadcastRoster() {
        StringBuilder roster = new StringBuilder(ROSTER_PREFIX);
        synchronized (clients) {
            boolean first = true;
            for (ClientHandler client : clients) {
                String username = client.getUsername();
                if (username == null || username.trim().isEmpty()) {
                    continue;
                }
                if (!first) {
                    roster.append(",");
                }
                roster.append(username.trim());
                first = false;
            }

            for (ClientHandler client : clients) {
                client.send(roster.toString());
            }
        }
    }

    private void printConnectedClients() {
        StringBuilder builder = new StringBuilder("Connected clients: ");
        synchronized (clients) {
            if (clients.isEmpty()) {
                builder.append("-");
            } else {
                boolean first = true;
                for (ClientHandler client : clients) {
                    if (client.getUsername() == null) {
                        continue;
                    }
                    if (!first) {
                        builder.append(", ");
                    }
                    builder.append(client.getUsername());
                    first = false;
                }
                if (first) {
                    builder.append("-");
                }
            }
        }
        log(builder.toString(), true);
    }

    private void updateStatus(final String text) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (statusLabel != null) {
                    statusLabel.setText(text);
                }
            }
        });
    }

    private void updateClientList() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (clientsPanel == null || clientCountLabel == null) {
                    return;
                }

                clientsPanel.removeAll();
                int count = 0;

                synchronized (clients) {
                    for (ClientHandler client : clients) {
                        String name = client.getUsername();
                        if (name == null || name.trim().isEmpty()) {
                            continue;
                        }
                        clientsPanel.add(createClientRow(name.trim()));
                        clientsPanel.add(Box.createVerticalStrut(8));
                        count++;
                    }
                }

                if (count == 0) {
                    JLabel emptyLabel = new JLabel("Waiting for clients...");
                    emptyLabel.setForeground(MUTED_TEXT);
                    emptyLabel.setFont(appFont(Font.PLAIN, 13));
                    emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                    clientsPanel.add(emptyLabel);
                }

                clientCountLabel.setText(count + (count == 1 ? " online" : " online"));
                clientsPanel.revalidate();
                clientsPanel.repaint();
            }
        });
    }

    private JPanel createClientRow(String username) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(8, 2, 8, 2));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));

        JLabel avatar = new CircleLabel(initials(username), senderColor(username), Color.WHITE);
        avatar.setFont(appFont(Font.BOLD, 11));
        avatar.setPreferredSize(new Dimension(34, 34));

        JLabel nameLabel = new JLabel(username);
        nameLabel.setForeground(TEXT_LIGHT);
        nameLabel.setFont(appFont(Font.BOLD, 13));

        JLabel onlineLabel = new JLabel("Connected");
        onlineLabel.setForeground(MUTED_TEXT);
        onlineLabel.setFont(appFont(Font.PLAIN, 11));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(nameLabel);
        text.add(Box.createVerticalStrut(1));
        text.add(onlineLabel);

        row.add(avatar, BorderLayout.WEST);
        row.add(text, BorderLayout.CENTER);
        return row;
    }

    private void log(final String message, final boolean neutral) {
        System.out.println(message);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (logPanel == null || logScrollPane == null) {
                    return;
                }

                JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                row.setOpaque(false);
                row.setAlignmentX(Component.LEFT_ALIGNMENT);
                row.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

                JLabel label = new JLabel("<html><body style='width: 560px; line-height: 1.25;'>"
                        + html(message) + "</body></html>");
                label.setFont(appFont(Font.PLAIN, 13));
                label.setForeground(neutral ? TEXT_LIGHT : MUTED_TEXT);
                label.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

                RoundedPanel bubble = new RoundedPanel(neutral ? OTHER_BUBBLE : INPUT_FIELD, 16, false);
                bubble.setLayout(new BorderLayout());
                bubble.add(label, BorderLayout.CENTER);
                bubble.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

                row.add(bubble);
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
                logPanel.add(row);
                logPanel.revalidate();
                logPanel.repaint();

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        logScrollPane.getVerticalScrollBar().setValue(logScrollPane.getVerticalScrollBar().getMaximum());
                    }
                });
            }
        });
    }

    private static Font appFont(int style, int size) {
        return new Font("Segoe UI", style, size);
    }

    private static String now() {
        return new SimpleDateFormat("HH:mm:ss").format(System.currentTimeMillis());
    }

    private static String systemMessage(String text) {
        return "[" + now() + "] [SERVER] " + text;
    }

    private static String html(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace(System.lineSeparator(), "<br>");
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

    private static String initials(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "CL";
        }
        String[] parts = value.trim().split("\\s+");
        String result = parts[0].substring(0, 1);
        if (parts.length > 1) {
            result += parts[1].substring(0, 1);
        }
        return result.toUpperCase();
    }

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port. Using default port " + DEFAULT_PORT + ".");
            }
        }

        final int serverPort = port;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ChatServer(serverPort).showAndStart();
            }
        });
    }

    private class ClientHandler implements Runnable {
        private final Socket socket;
        private BufferedReader reader;
        private BufferedWriter writer;
        private String username;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        String getUsername() {
            return username;
        }

        @Override
        public void run() {
            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

                username = reader.readLine();
                if (username == null || username.trim().isEmpty()) {
                    username = "Client-" + socket.getPort();
                }
                username = username.trim();

                String joinMessage = systemMessage(username + " joined the chat.");
                log(joinMessage, true);
                printConnectedClients();
                updateClientList();
                send(systemMessage("Welcome, " + username + "!"));
                broadcast(joinMessage, this);
                broadcastRoster();

                String message;
                while ((message = reader.readLine()) != null) {
                    message = message.trim();
                    if (message.isEmpty()) {
                        continue;
                    }

                    String formattedMessage = "[" + now() + "] " + username + ": " + message;
                    log(formattedMessage, true);
                    broadcast(formattedMessage, this);
                }
            } catch (IOException e) {
                if (running) {
                    log("Connection closed for " + (username == null ? socket.getRemoteSocketAddress() : username), false);
                }
            } finally {
                close();
                removeClient(this);
            }
        }

        void send(String message) {
            try {
                if (writer != null) {
                    writer.write(message);
                    writer.newLine();
                    writer.flush();
                }
            } catch (IOException e) {
                close();
            }
        }

        private void close() {
            try {
                socket.close();
            } catch (IOException ignored) {
                // Socket is already closed.
            }
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

    private static class RoundedButton extends JButton {
        private static final long serialVersionUID = 1L;

        RoundedButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isPressed() ? new Color(0, 135, 108) : getBackground());
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);
            g2.dispose();
            super.paintComponent(g);
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
