package sudoku.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

import common.GameInterface;

public interface GameFactory extends Remote {
    GameInterface createGame() throws RemoteException;
}