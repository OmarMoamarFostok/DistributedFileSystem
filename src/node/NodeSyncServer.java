package node;

import java.net.Socket;

public class NodeSyncServer implements Runnable{
    private Socket socket;
    public NodeSyncServer(Socket socket) {
        this.socket = socket;
    }
    public void run() {
        // Receive sync command, update files

    }
}
