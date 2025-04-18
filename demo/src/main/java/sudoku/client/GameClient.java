package sudoku.client;

import common.CallbackInterface;
import common.GameInterface;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import sudoku.server.GameFactory;

public class GameClient extends Application implements CallbackInterface {
    private GameInterface server;
    private SudokuGUI gui;
    private boolean registrationFailed = false;
    private boolean registrationComplete = false;
    private String initialGrid; // Store the initial grid to update later

    @Override
    public void init() throws Exception {
        try {
            System.out.println("Connecting to RMI registry on localhost:1098...");
            Registry registry = LocateRegistry.getRegistry("localhost", 1098);
            System.out.println("Looking up GameFactory...");
            GameFactory factory = (GameFactory) registry.lookup("GameFactory");
            System.out.println("Creating game instance...");
            try {
                server = factory.createGame();
            } catch (RemoteException e) {
                System.err.println("Failed to create game instance: " + e.getMessage());
                registrationFailed = true;
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Connection Error");
                    alert.setHeaderText("Server Full");
                    alert.setContentText("The server is full. Please try again later.");
                    alert.showAndWait();
                    Platform.exit();
                });
                return;
            }
            System.out.println("Game instance created.");

            System.out.println("Exporting callback object...");
            CallbackInterface callback = (CallbackInterface) UnicastRemoteObject.exportObject(this, 0);
            System.out.println("Registering client with server...");
            server.registerClient(callback);
            System.out.println("Client registration initiated. Waiting for server response...");

            // Wait for registration response with a timeout
            long startTime = System.currentTimeMillis();
            synchronized (this) {
                while (!registrationComplete && !registrationFailed) {
                    try {
                        wait(100);
                        if (System.currentTimeMillis() - startTime > 5000) {
                            throw new Exception("Registration timed out.");
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new Exception("Registration interrupted.");
                    }
                }
            }

            if (registrationFailed) {
                throw new Exception("Client registration failed: Server is full or game instance in use.");
            }

            System.out.println("Registration successful. Proceeding with GUI setup...");
            System.out.println("Initializing GUI...");
            gui = new SudokuGUI();
            System.out.println("Setting GUI dependencies...");
            gui.setDependencies(server, this);
            System.out.println("GUI initialized.");

            // Fetch the initial grid but don't update yet
            System.out.println("Requesting initial grid...");
            initialGrid = server.getCurrentGrid(this);
            System.out.println("Received initial grid:\n" + initialGrid);
        } catch (Exception e) {
            System.err.println("Failed to initialize GameClient: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void start(Stage primaryStage) {
        if (registrationFailed) {
            System.err.println("Cannot start GUI: Client registration failed.");
            Platform.exit();
            return;
        }
        System.out.println("Launching JavaFX application...");
        gui.start(primaryStage);
        // Now that the GUI is fully initialized, update the grid
        if (initialGrid != null) {
            gui.updateGrid(initialGrid);
        } else {
            System.err.println("Initial grid is null, cannot update GUI.");
        }
    }

    @Override
    public void showMessage(String message) throws RemoteException {
        System.out.println("Received message from server: " + message);
        synchronized (this) {
            if (message.equals("ERROR: Server full. Try again later.") || message.equals("ERROR: Game instance already in use.")) {
                registrationFailed = true;
                registrationComplete = true;
                System.err.println("Registration failed: " + message);
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Connection Error");
                    alert.setHeaderText("Server Full");
                    alert.setContentText("The server is full or the game instance is already in use. Please try again later.");
                    alert.showAndWait();
                    Platform.exit();
                });
            } else if (message.equals("Welcome! Registered as player.")) {
                registrationComplete = true;
                System.out.println("Registration confirmed by server.");
                if (gui != null) {
                    gui.showMessage(message);
                } else {
                    System.out.println("GUI is null, message received: " + message);
                }
            } else {
                if (gui != null) {
                    gui.showMessage(message);
                } else {
                    System.out.println("GUI is null, message received: " + message);
                }
            }
            notifyAll();
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