package sudoku.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import common.GameInterface;

public class GameFactoryImpl implements GameFactory {
    private GameInterface gameServer;

    public GameFactoryImpl() throws RemoteException {
        GameServerImpl game = new GameServerImpl();
        gameServer = (GameInterface) UnicastRemoteObject.exportObject(game, 0);
        System.out.println("GameFactoryImpl created with GameServerImpl instance: " + game);
    }

    @Override
    public GameInterface createGame() throws RemoteException {
        System.out.println("Returning existing GameServerImpl instance: " + gameServer);
        return gameServer;
    }
}