package com.jgu.chat;

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

/**
 * Multi-client chat server using Java Socket Programming.
 *
 * The server accepts many clients at the same time. Every client is handled by
 * its own thread, and messages are broadcast to all other connected clients.
 */
public class ChatServer {
    public static final int DEFAULT_PORT = 5000;
    private static final String ROSTER_PREFIX = "__ROSTER__:";

    private final int port;
    private final Set<ClientHandler> clients = Collections.synchronizedSet(new LinkedHashSet<ClientHandler>());

    public ChatServer(int port) {
        this.port = port;
    }

    public void start() {
        System.out.println("Chat server starting on port " + port + "...");

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is ready. Waiting for clients...");

            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket);
                clients.add(handler);
                new Thread(handler, "ClientHandler-" + socket.getPort()).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
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
            System.out.println(leaveMessage);
            broadcast(leaveMessage, client);
        }
        printConnectedClients();
        broadcastRoster();
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
        System.out.println(builder.toString());
    }

    private static String now() {
        return new SimpleDateFormat("HH:mm:ss").format(System.currentTimeMillis());
    }

    private static String systemMessage(String text) {
        return "[" + now() + "] [SERVER] " + text;
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

        new ChatServer(port).start();
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
                System.out.println(joinMessage);
                printConnectedClients();
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
                    System.out.println(formattedMessage);
                    broadcast(formattedMessage, this);
                }
            } catch (IOException e) {
                System.err.println("Connection closed for " + (username == null ? socket.getRemoteSocketAddress() : username));
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
}
