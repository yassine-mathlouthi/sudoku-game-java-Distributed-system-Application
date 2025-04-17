package server;

import common.GameInterface;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class GameFactoryImpl implements GameFactory {
    @Override
    public GameInterface createGame() throws RemoteException {
        GameServerImpl game = new GameServerImpl();
        return (GameInterface) UnicastRemoteObject.exportObject(game, 0);
    }
}