package coordinator;

import shared.*;

import Storage.UserStorageService;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CoordinatorServer extends UnicastRemoteObject implements CoordinatorInterface {

//    private Map<String, User> users = new HashMap<>();
//    private Map<String, String> tokenMap = new HashMap<>();
//    private List<String> nodes = new ArrayList<>();
//    private int roundRobinIndex = 0;
//    public CoordinatorServer() throws RemoteException {
//    }
//    private Map<String,String> users = new HashMap<>();


    //the name of the file and the latest version
    //todo: make it map of map for departments


    private final Map<String, Instruction> instructionTokens = new ConcurrentHashMap<>();



    private final Map<String, Object> fileLocks = new ConcurrentHashMap<>();
    private final UserStorageService userStorage = UserStorageService.getInstance();
    private final Map<String,String> userTokens = new ConcurrentHashMap<>();
    private final Map<String,String> userDepartments = new ConcurrentHashMap<>();
    public final List<NodeInterface> nodes = new ArrayList<>();
    private int roundRobinIndex = 0;
    private final Map<String, Set<String>> filesByDepartment = new ConcurrentHashMap<>();

    public CoordinatorServer() throws Exception {
        super();
        // userDepartments.put("alice","QA");
        // userDepartments.put("bob","development");
    }
    private Object getFileLock(String filePath) {
        return fileLocks.computeIfAbsent(filePath, k -> new Object());
    }

    @Override
    public void notifyFileUploaded(String department, String filename, String nodeName) throws RemoteException {
        filesByDepartment
                .computeIfAbsent(department, dept -> ConcurrentHashMap.newKeySet())
                .add(filename);
        System.out.println("Node " + nodeName + " uploaded file " + filename + " to department " + department);
    }
    @Override
    public synchronized void notifyFileDeleted(String department, String filename, String nodeName) throws RemoteException {
        Set<String> files = filesByDepartment.get(department);
        if(files!=null) {
            boolean removed = files.remove(filename);
            if (removed) {
                System.out.println("Node " + nodeName + " deleted file " + filename + " from department " + department);
            }
        }
    }
    @Override
    public boolean register(String username, String password, String department) throws RemoteException{
        try {
            User user = new User(username, password, department);
            userStorage.store(user);
            System.out.println("New user has arrived: " + username);
            return true;
        }
        catch (Exception e){
            System.err.println("Error registering user " + username + ": " + e.getMessage());
            return false;
        }
    }
    @Override
    public String login(String username, String password) {
        try {
            List<User> allUsers = userStorage.getAll();
            for (User user : allUsers) {
                if (user.username.equals(username) && user.password.equals(password)) {
                    String token = UUID.randomUUID().toString();
                    userTokens.put(token, username);
                    userDepartments.put(username,user.department);
                    System.out.println("User logged in: " + username + " with token: " + token);
                    return token;
                }
            }
        } catch (Exception e) {
            System.err.println("Error during login for user " + username + ": " + e.getMessage());
        }
        return null;
    }
    @Override
    public synchronized Instruction consumeToken(String token, NodeInterface callerNode) {
        Instruction instr = instructionTokens.get(token);
        if(instr!= null && !instr.isExpired() && instr.targetNode.equals(callerNode)) {
            instructionTokens.remove(token);
            return instr;
        }
        return null;

    }
    @Override
    public Instruction uploadFile(String token, String department, String filename) throws RemoteException {
        String user = userTokens.get(token);
        if(user==null || !userDepartments.get(user).equals(department)) return null;
        NodeInterface mxNode = null;
        int mx = -1;
        for(NodeInterface node: nodes) {
            int ver =  node.getFileVersion(department,filename);
            if(ver>mx) {
                mx = node.getFileVersion(department,filename);
                mxNode = node;
            }
        }
        int cntMxNodes = 0;
        for(NodeInterface node: nodes)
            if(node.getFileVersion(department,filename)==mx)
                cntMxNodes++;
        if(cntMxNodes==nodes.size())
            mxNode = nodes.get(roundRobinIndex++ % nodes.size());
        String uploadToken = UUID.randomUUID().toString();
        instructionTokens.put(uploadToken,new UploadInstruction(department,filename,mxNode,uploadToken));
        return instructionTokens.get(uploadToken);

    }
    @Override
    public Instruction downloadFile(String department, String filename) throws RemoteException {
        NodeInterface mxNode = null;
        int mx = -1;
        for(NodeInterface node: nodes) {
            int ver =  node.getFileVersion(department,filename);
            if(ver>mx) {
                mx = node.getFileVersion(department,filename);
                mxNode = node;
            }
        }
        int cntMxNodes = 0;
        for(NodeInterface node: nodes)
            if(node.getFileVersion(department,filename)==mx)
                cntMxNodes++;
        if(cntMxNodes==nodes.size())
            mxNode = nodes.get(roundRobinIndex++ % nodes.size());
        String downloadToken = UUID.randomUUID().toString();
        instructionTokens.put(downloadToken,new DownloadInstruction(department,filename,mxNode,downloadToken));
        return instructionTokens.get(downloadToken);
    }
    @Override
    public boolean deleteFile(String token, String department, String filename) throws RemoteException {
        String user = userTokens.get(token);
        if(user==null || !userDepartments.get(user).equals(department))return false;
        String fileKey = department + "/" + filename;
        Object lock = getFileLock(fileKey);
        synchronized (lock) {
            NodeInterface mxNode = null;
            int mx = -1;
            for(NodeInterface node: nodes) {
                int ver =  node.getFileVersion(department,filename);
                if(ver>mx) {
                    mx = node.getFileVersion(department,filename);
                    mxNode = node;
                }
            }
            int cntMxNodes = 0;
            for(NodeInterface node: nodes)
                if(node.getFileVersion(department,filename)==mx)
                    cntMxNodes++;
            if(cntMxNodes==nodes.size())
                mxNode = nodes.get(roundRobinIndex++ % nodes.size());

            if(mxNode!=null)
                return mxNode.delete(department,filename);
            return false;
        }

    }
    @Override
    public Map<String, List<String>> listFiles() {

        System.out.println("Files in my system are returned");
        Map<String, List<String>> result = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : filesByDepartment.entrySet()) {
            result.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return result;
    }
    @Override
    public void registerNode(NodeInterface node) {
        nodes.add(node);
        System.out.println("Registered node: success");
    }

    public static void main(String[] args) throws Exception{
        CoordinatorServer server = new CoordinatorServer();
        Registry registry = LocateRegistry.createRegistry(1099);
        registry.rebind("CoordinatorServer",server);
        System.out.println("Coordinator ready..");


    }

}
