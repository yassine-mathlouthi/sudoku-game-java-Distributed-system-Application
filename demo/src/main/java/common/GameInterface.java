package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface GameInterface extends Remote {
    void registerClient(CallbackInterface client) throws RemoteException;
    String makeMove(int row, int col, String value) throws RemoteException;
    boolean isGameOver() throws RemoteException;
    void resetGame() throws RemoteException;
    String getCurrentGrid() throws RemoteException; // New method to request grid
}