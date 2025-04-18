package sudoku.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import common.GameInterface;

public class GameFactoryImpl implements GameFactory {
    @Override
    public GameInterface createGame() throws RemoteException {
        GameServerImpl game = new GameServerImpl();
        return (GameInterface) UnicastRemoteObject.exportObject(game, 0);
    }
}