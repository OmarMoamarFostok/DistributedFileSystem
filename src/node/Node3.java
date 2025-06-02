package node;

import shared.CoordinatorInterface;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Node3 {
    public static void main(String[] args) throws Exception {
        Registry registry = LocateRegistry.getRegistry("localhost", 1099);
        CoordinatorInterface coordinator = (CoordinatorInterface) registry.lookup("CoordinatorServer");
        NodeServer node = new NodeServer("node3", coordinator); // Change dir for node2, node3...
        coordinator.registerNode(node);
        node.scheduleDailySyncToCentral("localhost", 5000);
    }
}
