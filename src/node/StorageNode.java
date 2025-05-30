package node;

import java.net.ServerSocket;
import java.net.Socket;

public class StorageNode {

    public static void main(String[] args) {
        int port = 9000; // sync port

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Node running...");
        boolean running = true;
            while (running) {
                try {
                    Socket socket = serverSocket.accept();
                    new Thread(new NodeSyncServer(socket)).start();
                } catch (Exception e) {
                    e.printStackTrace();
                    running = false; // break the loop and close the socket
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}