package shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface NodeInterface extends Remote {
    boolean upload(String token, byte[] data) throws RemoteException;
    byte[] view(String filename) throws RemoteException;
    //delete needs token:
    boolean delete(String department, String filename) throws RemoteException;
    List<String> list(String department) throws RemoteException;
    int getFileVersion(String department,String filename) throws RemoteException;

}
