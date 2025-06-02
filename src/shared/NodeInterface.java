package shared;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface NodeInterface extends Remote {
    boolean upload(String token, byte[] data) throws RemoteException;
    byte[] download(String token) throws RemoteException;
    boolean delete(String department, String filename) throws RemoteException;
    int getFileVersion(String department,String filename) throws RemoteException;

}
