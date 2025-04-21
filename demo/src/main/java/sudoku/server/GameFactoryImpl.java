package sudoku.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import common.GameInterface;

public class GameFactoryImpl implements GameFactory {
    private final int MAX_CLIENTS = 2; // Increased limit
    private final Map<GameServerImpl, Long> activeClients = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final long CLIENT_TIMEOUT_MS = 30_000; // 30 seconds

    public GameFactoryImpl() {
        scheduler.scheduleAtFixedRate(this::cleanupInactiveClients, 10, 10, TimeUnit.SECONDS);
        System.out.println("GameFactoryImpl initialized with MAX_CLIENTS=" + MAX_CLIENTS);
    }

    @Override
    public synchronized GameInterface createGame() throws RemoteException {
        if (activeClients.size() >= MAX_CLIENTS) {
            System.err.println("Server full. Active clients: " + activeClients.size());
            throw new RemoteException("ERROR: Server full. Try again later.");
        }
        GameServerImpl game = new GameServerImpl(this);
        activeClients.put(game, System.currentTimeMillis());
        System.out.println("Created new GameServerImpl instance. Active clients: " + activeClients.size());
        return (GameInterface) UnicastRemoteObject.exportObject(game, 0);
    }

    @Override
    public synchronized void clientDisconnected() {
        // Deprecated: Kept for backward compatibility, but should not be used
        System.err.println("Warning: Generic clientDisconnected() called; use clientDisconnected(GameServerImpl) instead");
    }

    public synchronized void clientDisconnected(GameServerImpl game) {
        if (activeClients.remove(game) != null) {
            System.out.println("Client disconnected successfully. Active clients: " + activeClients.size());
        } else {
            System.err.println("Client not found in activeClients: " + game);
        }
    }

    public synchronized void updateClientActivity(GameServerImpl game) {
        if (activeClients.containsKey(game)) {
            activeClients.put(game, System.currentTimeMillis());
            System.out.println("Updated activity for client: " + game);
        } else {
            System.err.println("Client not found for activity update: " + game);
        }
    }

    private void cleanupInactiveClients() {
        long currentTime = System.currentTimeMillis();
        activeClients.entrySet().removeIf(entry -> {
            boolean isInactive = (currentTime - entry.getValue()) > CLIENT_TIMEOUT_MS;
            if (isInactive) {
                System.out.println("Removing inactive client: " + entry.getKey());
            }
            return isInactive;
        });
        System.out.println("Cleanup completed. Active clients: " + activeClients.size());
    }

    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("GameFactoryImpl shutdown completed");
    }
}