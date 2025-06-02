package node;

import shared.*;
import sync.FileData;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
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

    public NodeServer(String nodePath, CoordinatorInterface coordinator) throws Exception {
        super(0);
        this.baseDir = new File(nodePath);
        if (!baseDir.exists()) baseDir.mkdirs();
        this.coordinator = coordinator;
    }

    private void deleteDirectoryContents(File dir) {
        if (!dir.exists() || !dir.isDirectory()) return;

        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectoryContents(file);
                    file.delete();
                } else {
                    file.delete();
                }
            }
        }
    }
    public void scheduleDailySyncToCentral(String serverAddress, int port) {
        Timer timer = new Timer();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 57);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTime().before(new Date())) {
            calendar.add(Calendar.DATE, 1);
        }
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                syncWithCentralServer(serverAddress, port);
            }
        }, calendar.getTime(), 24*60*60 * 1000);
    }
    public void syncWithCentralServer(String serverHost, int port) {
        try {
            System.out.println("syncing now!");





            // 3. Connect to central server
            Socket socket = new Socket(serverHost, port);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(getLocalFileData());
            out.flush();

            // 4. Receive updated files
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            List<FileData> updatedFiles = (List<FileData>) in.readObject();
            // 1. Delete all local files
            deleteDirectoryContents(baseDir);
            for(FileData file: updatedFiles) {
                if(file.content == null) continue;
                File diskFile = new File(baseDir,file.fullPath);
                File folder = diskFile.getParentFile();
                if(!folder.exists())folder.mkdirs();
                synchronized (getFileLock(diskFile.getAbsolutePath())) {
                    try (FileOutputStream fos = new FileOutputStream(diskFile)) {
                        fos.write(file.content);
                    }
                }
                System.out.println("[" + baseDir.getName() + "] Synced: " + file.fullPath + " v" + file.version);
            }
            // 2. Clear metadata map
            metaFiles.clear();
            socket.close();
            System.out.println("Ended Syncing");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<FileData> getLocalFileData() {
        List<FileData> files = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : metaFiles.entrySet()) {
            String path = entry.getKey(); // e.g., "sales/report.txt"
            int version = entry.getValue();

            File file = new File(baseDir, path); // baseDir/sales/report.txt
            if (file.exists()) {
                try {
                    byte[] content = Files.readAllBytes(file.toPath());
                    files.add(new FileData(path, version, content));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if(version>0){
                files.add(new FileData(path,version,null));
            }
        }
        return files;
    }

    @Override
    public int getFileVersion(String department, String filename) {
        return metaFiles.getOrDefault(department + "/" + filename, 0);
    }
    @Override
    public boolean upload(String token, byte[] data) throws RemoteException {
        Instruction instr = coordinator.consumeToken(token, this);
        if (!(instr instanceof UploadInstruction uploadInstr))
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
            metaFiles.put(key, metaFiles.getOrDefault(key, 0) + 1);
            coordinator.notifyFileUploaded(uploadInstr.department, uploadInstr.filename, this.baseDir.toString());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public byte[] download(String token) throws RemoteException {
        Instruction instr = coordinator.consumeToken(token, this);
        if (!(instr instanceof DownloadInstruction downloadInstr))
            return null;
        String key = downloadInstr.department + "/" + downloadInstr.filename;
        File file = new File(new File(baseDir, downloadInstr.department), downloadInstr.filename);

        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                return fis.readAllBytes();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;

    }
    @Override
    public boolean delete(String department, String filename) throws RemoteException {
        File file = new File(new File(baseDir, department), filename);
        synchronized (getFileLock(file.getAbsolutePath())) {
            boolean exists = file.exists();
            boolean deleted = file.delete();
            System.out.println("Exists: " + exists + ", Deleted: " + deleted);
            String key = department + "/" + filename;
            metaFiles.put(key, metaFiles.getOrDefault(key, 0) + 1);
            coordinator.notifyFileDeleted(department, filename, baseDir.toString());
            return deleted;
        }
    }


    private synchronized Object getFileLock(String path) {
        return fileLocks.computeIfAbsent(path, k -> new Object());

    }

    public static void main(String[] args) throws Exception {
        Registry registry = LocateRegistry.getRegistry("localhost", 1099);
        CoordinatorInterface coordinator = (CoordinatorInterface) registry.lookup("CoordinatorServer");

        NodeServer[] nodes = {new NodeServer("node1", coordinator), new NodeServer("node2", coordinator), new NodeServer("node3", coordinator)};
        for (int i = 0; i < 3; i++) {
            coordinator.registerNode(nodes[i]);
        }

        System.out.println("Nodes running...");


    }
}
