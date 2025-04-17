package server;

import common.GameInterface;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface GameFactory extends Remote {
    GameInterface createGame() throws RemoteException;
}