package sudoku.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import common.GameInterface;

public class GameFactoryImpl implements GameFactory {
    private int currentClients = 0;
    private final int MAX_CLIENTS = 10;

    @Override
    public synchronized GameInterface createGame() throws RemoteException {
        if (currentClients >= MAX_CLIENTS) {
            throw new RemoteException("ERROR: Server full. Try again later.");
        }
        GameServerImpl game = new GameServerImpl(this);
        currentClients++;
        System.out.println("Created new GameServerImpl instance. Current clients: " + currentClients);
        return (GameInterface) UnicastRemoteObject.exportObject(game, 0);
    }

    public synchronized void clientDisconnected() {
        currentClients--;
        System.out.println("Client disconnected. Current clients: " + currentClients);
    }
}