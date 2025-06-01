package shared;

import node.NodeServer;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface CoordinatorInterface extends Remote {

    void notifyFileDeleted(String department, String filename, String nodeName) throws RemoteException;
    Instruction consumeToken(String token,NodeInterface callerNode) throws RemoteException;
    boolean register(String username, String password, String department) throws RemoteException;
    String login(String username, String password) throws RemoteException;
//    String generateToken(String username) throws RemoteException;
    NodeInterface editFile(String token, String department, String filename, byte[] data) throws RemoteException;
    Instruction uploadFile(String token, String department, String filename) throws RemoteException;
    byte[] viewFile(String token, String filename) throws RemoteException;
    boolean deleteFile(String token, String department, String filename) throws RemoteException;
    void notifyFileUploaded(String department, String filename, String nodeName) throws RemoteException;
//    boolean updateFile(String token, String filename, byte[] newData) throws RemoteException;

    Map<String, List<String>> listFiles() throws RemoteException;
    void registerNode(NodeInterface node) throws RemoteException;
}
