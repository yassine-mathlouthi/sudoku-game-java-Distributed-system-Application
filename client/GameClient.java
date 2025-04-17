package client;

import common.CallbackInterface;
import common.GameInterface;
import server.GameFactory;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class GameClient implements CallbackInterface {
    private GameInterface server;
    private Scanner scanner;

    public GameClient() throws Exception {
        scanner = new Scanner(System.in);

        // Connect to factory
        Registry registry = LocateRegistry.getRegistry("localhost", 1098);
        GameFactory factory = (GameFactory) registry.lookup("GameFactory");
        server = factory.createGame();

        // Export callback object
        CallbackInterface callback = (CallbackInterface) UnicastRemoteObject.exportObject(this, 0);
        server.registerClient(callback);

        // Start game loop
        playGame();
    }

    private void playGame() throws Exception {
        while (true) {
            System.out.println("Enter row (0-8), col (0-8), value (1-9), exmp (0 0 5) : ");
            try {
                int row = scanner.nextInt();
                int col = scanner.nextInt();
                String value = scanner.next();

                // Basic input validation
                if (row < 0 || row > 9 || col < 0 || col > 9) {
                    System.out.println("Invalid row or column. Try again.");
                    continue;
                }

                String response = server.makeMove(row, col, value);
                System.out.println(response);

                if (response.startsWith("ERROR")) {
                    continue; // Prompt for retry
                }

                if (server.isGameOver()) {
                    System.out.println("Congratulations! Puzzle completed.");
                    System.out.println("Play again? (y/n): ");
                    if (scanner.next().equalsIgnoreCase("y")) {
                        server.resetGame();
                    } else {
                        break;
                    }
                }
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter numbers for row, col, and value.");
                scanner.nextLine(); // Clear buffer
            }
        }
    }

    @Override
    public void showMessage(String message) {
        System.out.println("Server: " + message);
    }

    @Override
    public void showGrid(String grid) {
        System.out.println("Current puzzle:\n" + grid);
    }

    public static void main(String[] args) {
        try {
           
            System.setProperty("java.security.policy", "security/SecurityPolicy.policy");

            new GameClient();
        } catch (Exception e) {
            System.err.println("Client error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}