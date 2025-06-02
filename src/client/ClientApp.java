package client;

import shared.CoordinatorInterface;
import shared.DownloadInstruction;
import shared.UploadInstruction;

import java.io.FileOutputStream;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public class ClientApp {

    CoordinatorInterface coordinator;
    public ClientApp(CoordinatorInterface coordinator) {
        this.coordinator = coordinator;
    }
    public void listFiles() throws RemoteException{
        Map<String, List<String>> files = coordinator.listFiles();
        for (Map.Entry<String, List<String>> entry : files.entrySet()) {
            String department = entry.getKey();
            List<String> fileList = entry.getValue();

            System.out.println("Department: " + department);
            for (String file : fileList) {
                System.out.println("  - " + file);
            }
        }
    }
    public boolean uploadFile(String token,String department, String filename,byte[] data) throws RemoteException {
        UploadInstruction uploadInstr = (UploadInstruction) coordinator.uploadFile(token,department,filename);
        if(uploadInstr==null)
            return false;
        uploadInstr.targetNode.upload(uploadInstr.token,data);
        return true;
    }
    public void downloadFile(String department ,String filename, String fileDestination) throws RemoteException {
        DownloadInstruction downloadInstr = (DownloadInstruction) coordinator.downloadFile(department,filename);
        if(downloadInstr==null)
            return;
        byte[] data = downloadInstr.targetNode.download(downloadInstr.token);
        if(data != null) {
            try(FileOutputStream fos = new FileOutputStream(fileDestination)) {
                fos.write(data);
                System.out.println("file downloaded successfully");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("File not found or error reading");
        }


    }
    public static void main(String[] args) throws Exception {
//        CoordinatorInterface coordinator = (CoordinatorInterface) Naming.lookup("rmi://localhost/Coordinator");
//        String token = coordinator.generateToken("john");


        System.out.println("Client app running...");
//        System.out.println(coordinator.register("omar","123","dev"));
//        System.out.println(coordinator.login("omar","123"));


//
//        System.out.println(coordinator.login("alice","123456789"));
//        byte[] fileData = Files.readAllBytes(Paths.get("C:/Users/Omar Fostok/Desktop/docker.txt"));
//        System.out.println(coordinator.uploadFile(coordinator.login("omar","123"),"dev","firstUploadedfile",fileData));
//        coordinator.viewFile(coordinator.login("omar","123"),"firstUploadedfile");


    }
}
