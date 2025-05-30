package node;

import shared.CoordinatorInterface;
import shared.NodeInterface;

import java.io.*;
import java.net.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class NodeServer extends UnicastRemoteObject implements NodeInterface {

private final File baseDir;
private final Map<String,Object> fileLocks = new HashMap<>();
    public NodeServer(String nodePath) throws Exception {
        super();
        this.baseDir = new File(nodePath);
        if(!baseDir.exists()) baseDir.mkdirs();
    }
    public boolean upload(String department, String filename, byte[] data) {
        try {
            File folder = new File(baseDir,department);
            if(!folder.exists()) folder.mkdirs();
            File file = new File(folder,filename);
            synchronized (getFileLock(file.getAbsolutePath())) {
                try(FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(data);
                }
            }
            return true;
        } catch(IOException e) {
           return false;
        }
    }
    public byte[] view(String filename) {
        for(File department: baseDir.listFiles()) {
            File file = new File(department, filename);
            if(file.exists()) {
                try(FileInputStream fis = new FileInputStream(file)) {
                    return fis.readAllBytes();
                } catch(IOException e) {
                    return null;
                }
            }
        }
        return null;
    }


    public void delete(String department, String filename) {
        File file = new File(new File(baseDir,department),filename);
        synchronized (getFileLock(file.getAbsolutePath())) {
            if(file.exists()) file.delete();
        }
    }

    public List<String> list(String department) {
        File folder = new File(baseDir, department);
        if(folder.exists()) {
            return Arrays.asList(folder.list());

        }
        return new ArrayList<>();
    }
    private synchronized Object getFileLock(String path) {
        return fileLocks.computeIfAbsent(path, k -> new Object());

    }
    public static void main(String[] args) throws Exception {
//        NodeServer node = new NodeServer("node1");
            Registry registry = LocateRegistry.getRegistry("localhost",1099);
            CoordinatorInterface coordinator = (CoordinatorInterface) registry.lookup("CoordinatorServer");
            NodeInterface node = new NodeServer("node1");
            coordinator.registerNode(node);
        System.out.println("Node running...");


    }
}
