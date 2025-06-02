package sync;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

public class NodeSocketServer {
    private static final int EXPECTED_NODES = 3;
    private final static CyclicBarrier barrier = new CyclicBarrier(EXPECTED_NODES);
    private static final Map<String, FileData> latestFiles = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(5000);
        System.out.println("Server started on port 5000...");

        while (true) {
            Socket clientSocket = serverSocket.accept(); // waits for a client
            System.out.println("Client connected: " + clientSocket.getInetAddress());

            new Thread(() -> {
                try (ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {

                    List<FileData> incoming = (List<FileData>) in.readObject();
                    for (FileData file : incoming) {
                        latestFiles.merge(file.fullPath, file, (oldVal, newVal) -> newVal.version > oldVal.version ? newVal : oldVal);
                    }
                    try {
                    barrier.await(10, TimeUnit.SECONDS);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }

                    ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                    out.writeObject(new ArrayList<>(latestFiles.values()));
                    out.flush();


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}



