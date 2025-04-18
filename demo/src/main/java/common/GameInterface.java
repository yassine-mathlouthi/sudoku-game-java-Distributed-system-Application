package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface GameInterface extends Remote {
    void registerClient(CallbackInterface client) throws RemoteException;
    String makeMove(CallbackInterface client, int row, int col, String value) throws RemoteException;
    boolean isGameOver(CallbackInterface client) throws RemoteException;
    void resetGame(CallbackInterface client) throws RemoteException;
    String getCurrentGrid(CallbackInterface client) throws RemoteException;
}