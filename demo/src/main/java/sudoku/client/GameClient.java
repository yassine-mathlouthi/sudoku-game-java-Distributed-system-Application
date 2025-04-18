// GameClient.java
package sudoku.client;

import common.CallbackInterface;
import common.GameInterface;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import javafx.application.Application;
import javafx.stage.Stage;
import sudoku.server.GameFactory;

public class GameClient extends Application implements CallbackInterface {
    private GameInterface server;
    private SudokuGUI gui;

    @Override
    public void init() throws Exception {
        try {
            System.out.println("Connecting to RMI registry on localhost:1098...");
            Registry registry = LocateRegistry.getRegistry("localhost", 1098);
            System.out.println("Looking up GameFactory...");
            GameFactory factory = (GameFactory) registry.lookup("GameFactory");
            System.out.println("Creating game instance...");
            server = factory.createGame();
            System.out.println("Game instance created.");

            System.out.println("Exporting callback object...");
            CallbackInterface callback = (CallbackInterface) UnicastRemoteObject.exportObject(this, 0);
            System.out.println("Registering client with server...");
            server.registerClient(callback);
            System.out.println("Client registered successfully.");

            System.out.println("Initializing GUI...");
            gui = new SudokuGUI();
            System.out.println("Setting GUI dependencies...");
            gui.setDependencies(server, this);
            System.out.println("GUI initialized.");
        } catch (Exception e) {
            System.err.println("Failed to initialize GameClient: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void start(Stage primaryStage) {
        System.out.println("Launching JavaFX application...");
        gui.start(primaryStage);
    }

    @Override
    public void showMessage(String message) throws RemoteException {
        System.out.println("Received message from server: " + message);
        if (gui != null) {
            gui.showMessage(message);
        } else {
            System.err.println("GUI is null, cannot show message: " + message);
        }
    }

    @Override
    public void showGrid(String grid) throws RemoteException {
        System.out.println("Received grid from server:\n" + grid);
        if (gui != null) {
            gui.updateGrid(grid);
            System.out.println("Grid passed to GUI for rendering.");
        } else {
            System.err.println("GUI is null, cannot update grid.");
        }
    }

    public static void main(String[] args) {
        try {
            System.setProperty("java.security.policy", "security/SecurityPolicy.policy");
            System.out.println("Launching JavaFX application...");
            Application.launch(GameClient.class, args);
        } catch (Exception e) {
            System.err.println("Client error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
