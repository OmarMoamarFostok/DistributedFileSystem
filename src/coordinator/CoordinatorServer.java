package coordinator;

import shared.CoordinatorInterface;
import shared.NodeInterface;
import shared.User;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

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
    private Map<String,String> metaFiles = new HashMap<>();
    private Map<String,String> userTokens = new HashMap<>();
    private Map<String,String> userDepartments = new HashMap<>();
    private List<NodeInterface> nodes = new ArrayList<>();
    private int roundRobinIndex = 0;
    public CoordinatorServer() throws Exception {
        super();
        userDepartments.put("alice","QA");
        userDepartments.put("bob","development");
    }

    public String login(String username, String password) {
        if(userDepartments.containsKey(username)){
            String token = UUID.randomUUID().toString();
            userTokens.put(token,username);
            return token;

        }
        return null;
    }


    public boolean uploadFile(String token, String department, String filename, byte[] data) throws RemoteException {
        String user = userTokens.get(token);
        if(user==null || !userDepartments.get(user).equals(department)) return false;
        NodeInterface node = nodes.get(roundRobinIndex++ % nodes.size());
        return node.upload(department,filename,data);

    }


    public byte[] viewFile(String token, String filename) throws RemoteException {

        for(NodeInterface node: nodes) {
            byte[] data = node.view(filename);
            if(data!=null) return data;
        }
        return null;
    }


    public boolean deleteFile(String token, String department, String filename) throws RemoteException {
        String user = userTokens.get(token);
        if(user==null || !userDepartments.get(user).equals(department))return false;
        for (NodeInterface node: nodes) {
            node.delete(department, filename);
        }
        return false;
    }

    public void listFiles(String token, String department) {
        String user = userTokens.get(token);
        if(user==null || !userDepartments.get(user).equals(department))return;
        System.out.println("Files in my system:");
        for(Map.Entry<String,String> filename : metaFiles.entrySet()) {
            System.out.println(filename.getKey());
        }

    }
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
