package sudoku.server;

import java.rmi.RemoteException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import common.CallbackInterface;
import common.GameInterface;

public class GameServerImpl implements GameInterface {
    private Grid grid;
    private CallbackInterface client;
    private GameFactoryImpl factory;
    private boolean clientRegistered = false;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    public GameServerImpl(GameFactoryImpl factory) throws RemoteException {
        this.factory = factory;
        grid = new Grid();
        displayGrid();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                factory.updateClientActivity(this);
            } catch (Exception e) {
                System.err.println("Error updating client activity: " + e.getMessage());
            }
        }, 10, 10, TimeUnit.SECONDS);
    }
    

    private void displayGrid() {
        System.out.println("Client grid:\n" + grid.toString());
    }

    @Override
    public synchronized String getCurrentGrid(CallbackInterface client) throws RemoteException {
        if (!clientRegistered) {
            throw new RemoteException("Client not registered with this game instance.");
        }
        System.out.println("Client requested current grid:\n" + grid.toString());
        return grid.toString();
    }

    @Override
    public synchronized void registerClient(CallbackInterface client) throws RemoteException {
        if (clientRegistered) {
            client.showMessage("ERROR: Game instance already in use.");
            System.out.println("Client rejected: Game instance already in use.");
            return;
        }
        this.client = client;
        clientRegistered = true;
        System.out.println("Client registered with game instance.");
        client.showMessage("Welcome! Registered as player.");
        displayGrid();
    }

    @Override
    public synchronized String makeMove(CallbackInterface client, int row, int col, String value) throws RemoteException {
        if (!clientRegistered) {
            return "ERROR: Client not registered with this game instance.";
        }
        if (grid.isValidMove(row, col, value)) {
            grid.setCell(row, col, value);
            broadcastGridToClient();
            return "SUCCESS: Move accepted.";
        } else {
            return "ERROR: Invalid move. Please try again.";
        }
    }

    @Override
    public synchronized boolean isGameOver(CallbackInterface client) throws RemoteException {
        if (!clientRegistered) {
            throw new RemoteException("Client not registered with this game instance.");
        }
        return grid.isComplete();
    }

    @Override
    public synchronized void resetGame(CallbackInterface client) throws RemoteException {
        if (!clientRegistered) {
            throw new RemoteException("Client not registered with this game instance.");
        }
        grid = new Grid();
        displayGrid();
        broadcastGridToClient();
        broadcastMessageToClient("Game reset! New puzzle started.");
    }

    private void broadcastGridToClient() throws RemoteException {
        try {
            client.showGrid(grid.toString());
        } catch (RemoteException e) {
            System.out.println("Client failed to receive grid update: " + e.getMessage());
            disconnectClient();
        }
    }

    private void broadcastMessageToClient(String message) throws RemoteException {
        try {
            client.showMessage(message);
        } catch (RemoteException e) {
            System.out.println("Client failed to receive message: " + e.getMessage());
            disconnectClient();
        }
    }

    private void disconnectClient() {
        try {
            clientRegistered = false;
            client = null;
            factory.clientDisconnected(this); // Call with this instance
            scheduler.shutdownNow();
            System.out.println("Client disconnected from game instance.");
        } catch (Exception e) {
            System.err.println("Error notifying factory of disconnection: " + e.getMessage());
        }
    }
    @Override
    public synchronized void disconnect(CallbackInterface client) throws RemoteException {
        
            disconnectClient();
        
    }
}