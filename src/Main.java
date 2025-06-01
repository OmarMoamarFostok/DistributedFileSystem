import client.ClientApp;
import shared.CoordinatorInterface;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws Exception {
        Registry registry = LocateRegistry.getRegistry("localhost",1099);
        CoordinatorInterface coordinator = (CoordinatorInterface) registry.lookup("CoordinatorServer");
        ClientApp client = new ClientApp(coordinator);
        String token = coordinator.login("omar","123");
        System.out.println(token);
//        client.downloadFile(token,"firstUploadedfile","D:\\rubbish\\downladedFile.txt");
//        coordinator.listFiles(token,"dev");
        byte[] fileData = Files.readAllBytes(Paths.get("C:/Users/Omar Fostok/Desktop/django.txt"));
//        if(client.uploadFile(token,"dev","BRANDNEW2",fileData))
//        System.out.println("all modifications done!!!!");
//        else
//            System.out.println("something wrong happened");
//        System.out.println(coordinator.deleteFile(token,"dev","pool-1-thread-4"));
//        System.out.println(coordinator.register("omar2","123","qa"));
        client.listFiles();


    }
}