package server;

import common.GameInterface;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class GameServer {
    public static void main(String[] args) {
        try {
            // Set security manager
            
            System.setProperty("java.security.policy", "security/SecurityPolicy.policy");

            // Create RMI registry programmatically
            Registry registry = LocateRegistry.createRegistry(1098);

            // Create factory
            GameFactory factory = new GameFactoryImpl();
            GameFactory stubFactory = (GameFactory) UnicastRemoteObject.exportObject(factory, 0);
            registry.rebind("GameFactory", stubFactory);

            System.out.println("Server started with GameFactory...");
        } catch (Exception e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}