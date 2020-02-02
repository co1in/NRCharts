package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class NRClient {
    private Socket socket;

    interface ConnectionListener {
        public void onConnectionStateChanged(boolean connected);
    }

    private boolean shouldReconnect = true;

    private NRClient() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (shouldReconnect) {
                    connect();
                }
            }
        };

        Timer t = new Timer();
        t.schedule(task, 0, 1000);
    }

    private static NRClient instance;
    public static NRClient getInstance() {
        if(instance == null) {
            instance = new NRClient();
        }
        return instance;
    }

    public static void initialize() {
        getInstance();
    }

    private String serverAddress = null;

    public void setServerAddress(String address){
        this.serverAddress = address;
        this.shouldReconnect = true;
    }

    private LinkedList<ConnectionListener> listeners = new LinkedList<>();
    public void addConnectionListener(ConnectionListener listener) {
        listeners.add(listener);
    }

    private final String CLIENT_DELIMITER = "~";

    private void connect() {
        try {
            if(socket != null && !socket.isClosed()) {
                socket.close();
                notifyListener(false);
            }
            if(serverAddress == null || serverAddress.equals("")) {
                return;
            }

            shouldReconnect = false;
            socket = new Socket(serverAddress, 8888);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("Client Connected");

            notifyListener(true);
            new Thread(() -> {
                String serverMessage;
                try {
                    while ((serverMessage = in.readLine()) != null) {
                        String[] split = serverMessage.split(CLIENT_DELIMITER);
                        String key = split[0];
                        String dataType = split[1];
                        long timestamp = Long.parseLong(split[2]);
                        double value = Double.parseDouble(split[3]);
                        NRDataStore.getInstance().putData(key, timestamp, value);
                    }
                    notifyListener(false);
                    shouldReconnect = true;
                } catch(IOException e) {
                    System.out.println("Internal connection closed");
                    notifyListener(false);
                    shouldReconnect = true;
                }
            }).start();
        } catch (IOException e) {
            notifyListener(false);
            shouldReconnect = true;
        }
    }

    private void notifyListener(boolean value) {
        listeners.forEach(listener -> listener.onConnectionStateChanged(value));
    }
}
