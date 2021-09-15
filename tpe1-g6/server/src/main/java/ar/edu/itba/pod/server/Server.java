package ar.edu.itba.pod.server;

import ar.edu.itba.pod.server.servants.Servant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) throws RemoteException {
        logger.info("tpe1-g6 Server Starting ...");

        final Servant servant = new Servant();
        final Remote remote = UnicastRemoteObject.exportObject(servant, 0);

        final Registry registry = LocateRegistry.getRegistry();

        registry.rebind("admin", remote);
        registry.rebind("consult", remote);
        registry.rebind("runway", remote);
        registry.rebind("tracking", remote);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (!servant.awaitTermination())
                    logger.error("Could not terminate executor successfully.");
            } catch (InterruptedException e) {
                e.printStackTrace();
                logger.error("Could not terminate executor successfully.");
            }
        }));
        logger.info("tpe1-g6 Server Started.");
    }
}
