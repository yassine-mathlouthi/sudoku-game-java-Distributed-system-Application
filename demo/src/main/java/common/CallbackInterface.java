package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CallbackInterface extends Remote {
    void showMessage(String message) throws RemoteException;
    void showGrid(String grid) throws RemoteException;
}