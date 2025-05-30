package shared;

import node.NodeServer;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface CoordinatorInterface extends Remote {

    String login(String username, String password) throws RemoteException;
//    String generateToken(String username) throws RemoteException;

    boolean uploadFile(String token, String department, String filename, byte[] data) throws RemoteException;
    byte[] viewFile(String token, String filename) throws RemoteException;
    boolean deleteFile(String token, String department, String filename) throws RemoteException;
//    boolean updateFile(String token, String filename, byte[] newData) throws RemoteException;

    void listFiles(String token, String dept) throws RemoteException;
    void registerNode(NodeInterface node) throws RemoteException;
}
