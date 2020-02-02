package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.function.Consumer;

public class NRChartSender {
    private ServerSocket serverSocket;
    private final int SERVER_PORT = 8888;
    private final LinkedList<Client> clients = new LinkedList<Client>();

    private NRChartSender() {

    }

    public void init() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (serverSocket != null && !serverSocket.isClosed())
                        close();

                    serverSocket = new ServerSocket(SERVER_PORT);
                    log("Server is open");
                    while(!serverSocket.isClosed()) {
                        try {
                            Client newClient = new Client(serverSocket.accept());
                            System.out.println("Adding new client");
                            synchronized (clients) {
                                clients.add(newClient);
                            }
                            startClientDisconnectDetector(newClient);
                        } catch(SocketException e) {
                            log("Server is closing");
                        } catch(IOException e) {
                            error("Error accepting new client", e);
                        }
                    }
                } catch (IOException e) {
                    error("Couldn't acquire start server on port " + SERVER_PORT, e);
                }
            }
        }).start();
    }

    public void close() {
        try {
            serverSocket.close();
            synchronized (clients) {
                clients.forEach(client -> {
                    try {
                        client.getSocket().close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                clients.clear();
            }
        } catch (IOException e) {
            error("Couldn't close server", e);
        }
    }

    private final String CLIENT_DELIMITER = "~";
    private final String TYPE_NUM = "NUM";

    public void putNumber(String key, int num) {
        sendToClients(key, TYPE_NUM, String.valueOf(num));
    }

    public void putNumber(String key, double num) {
        sendToClients(key, TYPE_NUM, String.valueOf(num));
    }

    public void putNumber(String key, float num) {
        sendToClients(key, TYPE_NUM, String.valueOf(num));
    }

    private void sendToClients(String key, String datatype, String value) {
        if (key.contains("~")) {
            error("keys cannot contain '~'");
        }
        else if(key.strip().equals("")) {
            error("keys cannot be empty");
        }
        else {
            forEachClient(client -> {
                client.getOut().println(key + CLIENT_DELIMITER + datatype + CLIENT_DELIMITER + System.currentTimeMillis() + CLIENT_DELIMITER + value);
            });
        }
    }

    private void startClientDisconnectDetector(Client c) {
        Thread t = new Thread(() -> {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(c.getSocket().getInputStream()));
                while (in.readLine() != null) {
                    Thread.sleep(5000);
                }
            } catch (IOException | InterruptedException e) {
            }
            System.out.println("Client disconnected");
            synchronized (clients) {
                clients.remove(c);
            }
        });
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }

    private void forEachClient(Consumer<Client> action) {
        synchronized (clients) {
            for (int i = 0; i < clients.size(); i++) {
                if(clients.get(i).getSocket().isClosed() || clients.get(i).getSocket().isInputShutdown()) {
                    System.out.println("Removing client");
                    clients.remove(i);
                    i--;
                } else {
                    action.accept(clients.get(i));
                }
            }
        }
    }

    private void log(String message) {
        _writeMessage(message, System.out);
    }

    private void error(String message) {
        _writeMessage(message, System.err);
    }

    private void error(String message, Exception e) {
        _writeMessage(message, System.err);
        e.printStackTrace(System.err);
    }

    private void _writeMessage(String message, PrintStream out) {
        out.println("NRChartSender - " + message);
    }

    private static NRChartSender instance;
    public static NRChartSender getInstance() {
        if(instance == null) {
            instance = new NRChartSender();
        }
        return instance;
    }
}

class Client {
    private Socket socket;
    private PrintWriter out;
    public Client(Socket socket) throws IOException {
        this.socket = socket;
        this.out = new PrintWriter(this.socket.getOutputStream(), true);
    }
    public PrintWriter getOut() {
        return out;
    }
    public Socket getSocket() {
        return socket;
    }
}