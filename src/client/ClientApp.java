package client;

import shared.CoordinatorInterface;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ClientApp {
    public static void main(String[] args) throws Exception {
//        CoordinatorInterface coordinator = (CoordinatorInterface) Naming.lookup("rmi://localhost/Coordinator");
//        String token = coordinator.generateToken("john");
        Registry registry = LocateRegistry.getRegistry("localhost",1099);
        CoordinatorInterface coordinator = (CoordinatorInterface) registry.lookup("CoordinatorServer");
//
//        System.out.println(coordinator.login("alice","123456789"));
        byte[] fileData = Files.readAllBytes(Paths.get("C:/Users/Omar Fostok/Desktop/docker.txt"));
        System.out.println(coordinator.uploadFile("1861aa1a-f710-456c-bd68-a6bc6d73ee38","QA","firstUploadedfile",fileData));

    }
}
