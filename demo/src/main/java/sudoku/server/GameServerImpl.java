package sudoku.server;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.ArrayList;

import common.CallbackInterface;
import common.GameInterface;

public class GameServerImpl implements GameInterface {
    private Grid grid;
    private List<CallbackInterface> clients;
    private final int MAX_CLIENTS = 3;

    public GameServerImpl() throws RemoteException {
        grid = new Grid();
        clients = new ArrayList<>();
        displayGrid();
    }

    @Override
    public synchronized String getCurrentGrid() throws RemoteException {
        System.out.println("Client requested current grid:\n" + grid.toString());
        return grid.toString();
    }

    private void displayGrid() {
        System.out.println("Server grid:\n" + grid.toString());
    }

    @Override
    public synchronized void registerClient(CallbackInterface client) throws RemoteException {
        System.out.println("Registering client. Current client count: " + clients.size());
        if (clients.size() < MAX_CLIENTS) {
            clients.add(client);
            System.out.println("Client added. New client count: " + clients.size());
            client.showMessage("Welcome! Registered as player " + clients.size());
            System.out.println("Client registered. Total clients: " + clients.size());
        } else {
            client.showMessage("ERROR: Server full. Try again later.");
            System.out.println("Client rejected. Server full. Total clients: " + clients.size());
            
        }
    }

    @Override
    public synchronized String makeMove(int row, int col, String value) throws RemoteException {
        if (grid.isValidMove(row, col, value)) {
            grid.setCell(row, col, value);
            broadcastGrid();
            return "SUCCESS: Move accepted.";
        } else {
            return "ERROR: Invalid move. Please try again.";
        }
    }

    @Override
    public synchronized boolean isGameOver() throws RemoteException {
        return grid.isComplete();
    }

    @Override
    public synchronized void resetGame() throws RemoteException {
        grid = new Grid();
        displayGrid();
        broadcastGrid();
        broadcastMessage("Game reset! New puzzle started.");
    }

    private void broadcastGrid() throws RemoteException {
        List<CallbackInterface> toRemove = new ArrayList<>();
        for (CallbackInterface client : clients) {
            try {
                client.showGrid(grid.toString());
            } catch (RemoteException e) {
                System.out.println("Client failed to receive grid update: " + e.getMessage());
                toRemove.add(client);
            }
        }
        if (!toRemove.isEmpty()) {
            clients.removeAll(toRemove);
            System.out.println("Removed " + toRemove.size() + " disconnected clients. Total clients: " + clients.size());
        }
    }

    private void broadcastMessage(String message) throws RemoteException {
        List<CallbackInterface> toRemove = new ArrayList<>();
        for (CallbackInterface client : clients) {
            try {
                client.showMessage(message);
            } catch (RemoteException e) {
                System.out.println("Client failed to receive message: " + e.getMessage());
                toRemove.add(client);
            }
        }
        if (!toRemove.isEmpty()) {
            clients.removeAll(toRemove);
            System.out.println("Removed " + toRemove.size() + " disconnected clients. Total clients: " + clients.size());
        }
    }
}