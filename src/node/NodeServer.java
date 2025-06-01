package node;

import shared.*;

import java.io.*;
import java.net.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NodeServer extends UnicastRemoteObject implements NodeInterface {
    private final CoordinatorInterface coordinator;
    private final File baseDir;
    private final Map<String, Object> fileLocks = new ConcurrentHashMap<>();
    private final Map<String, Integer> metaFiles = new ConcurrentHashMap<>();

    public NodeServer(String nodePath,CoordinatorInterface coordinator) throws Exception {
        super();
        this.baseDir = new File(nodePath);
        if (!baseDir.exists()) baseDir.mkdirs();
        this.coordinator = coordinator;
    }
    public int getFileVersion(String department,String filename) {
        return metaFiles.getOrDefault(department+"/"+filename,0);
    }


    public boolean upload(String token, byte[] data) throws RemoteException {
        Instruction instr = coordinator.consumeToken(token,this);
        if(!(instr instanceof UploadInstruction uploadInstr))
            return false;
        String key = uploadInstr.department + "/" + uploadInstr.filename;
        try {
            File folder = new File(baseDir, uploadInstr.department);
            if (!folder.exists() && !folder.mkdirs())
                return false;
            File file = new File(folder, uploadInstr.filename);
            synchronized (getFileLock(file.getAbsolutePath())) {
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(data);
                }
            }
            metaFiles.put(key, metaFiles.getOrDefault(key,0)+1);
            coordinator.notifyFileUploaded(uploadInstr.department, uploadInstr.filename, this.baseDir.toString());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public byte[] view(String filename) {
        for (File department : baseDir.listFiles()) {
            File file = new File(department, filename);
            if (file.exists()) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    return fis.readAllBytes();
                } catch (IOException e) {
                    return null;
                }
            }
        }
        return null;
    }


    public boolean delete(String department, String filename) throws RemoteException {
        File file = new File(new File(baseDir, department), filename);
        synchronized (getFileLock(file.getAbsolutePath())) {
            boolean exists = file.exists();
            boolean deleted = file.delete();
            System.out.println("Exists: " + exists + ", Deleted: " + deleted);
            String key = department + "/" + filename;
            metaFiles.put(key, metaFiles.getOrDefault(key,0)+1);
            coordinator.notifyFileDeleted(department,filename,baseDir.toString());
            return deleted;
        }
    }

    public List<String> list(String department) {
        File folder = new File(baseDir, department);
        if (folder.exists()) {
            return Arrays.asList(folder.list());

        }
        return new ArrayList<>();
    }

    private synchronized Object getFileLock(String path) {
        return fileLocks.computeIfAbsent(path, k -> new Object());

    }

    public static void main(String[] args) throws Exception {
//        NodeServer node = new NodeServer("node1");
        Registry registry = LocateRegistry.getRegistry("localhost", 1099);
        CoordinatorInterface coordinator = (CoordinatorInterface) registry.lookup("CoordinatorServer");
//        NodeInterface node = new NodeServer("node1",coordinator);
//        coordinator.registerNode(node);
//        System.out.println("Node running...");
        NodeInterface[] nodes = {new NodeServer("node1",coordinator),new NodeServer("node2",coordinator),new NodeServer("node3",coordinator)};
        for(int i=0;i<3;i++) {
            coordinator.registerNode(nodes[i]);
        }
        System.out.println("Nodes running...");


    }
}
