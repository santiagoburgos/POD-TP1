package ar.edu.itba.pod.client;

import ar.edu.itba.pod.api.interfaces.AdminService;
import ar.edu.itba.pod.api.model.ReorderStatus;
import ar.edu.itba.pod.api.model.RunwayType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.Optional;

public class AdminClient {

    private static Logger logger = LoggerFactory.getLogger(AdminClient.class);


    public static void main(final String[] args) {

        try {

            String serverAddress = Optional.ofNullable(System.getProperty("serverAddress")).orElseThrow(() -> new IllegalArgumentException("'serverAddress' argument needed."));
            ActionType action = ActionType.getEnumOf(System.getProperty("action")).orElseThrow(() -> new IllegalArgumentException("Bad action."));
            String runwayName = System.getProperty("runway");
            RunwayType category = System.getProperty("category")==null?null:(RunwayType.valueOf(System.getProperty("category")));

            if (action == ActionType.ADD) {
                if (category == null || runwayName == null) {
                    throw new IllegalArgumentException("One or more arguments missing.");
                }
            } else if (action == ActionType.OPEN || action == ActionType.CLOSE || action == ActionType.STATUS) {
                if (runwayName == null) {
                    throw new IllegalArgumentException("'runway' argument needed.");
                }
            }

            logger.info("tpe1-g6 Client Starting ...");

            String[] address = serverAddress.split(":");
            String host = address[0];
            String port = address[1];

            final Registry registry = LocateRegistry.getRegistry(host, Integer.parseInt(port));
            final AdminService adminService = (AdminService) registry.lookup("admin");

            logger.info("client Started ...");

            switch (action) {
                case ADD:
                    adminService.addRunway(runwayName, category);
                    logger.info("Runway " + runwayName + " is open.");
                    break;
                case OPEN:
                    adminService.openRunway(runwayName);
                    logger.info("Runway " + runwayName + " is open.");
                    break;
                case CLOSE:
                    adminService.closeRunway(runwayName);
                    logger.info("Runway " + runwayName + " is closed.");
                    break;
                case STATUS:
                    boolean isOpen = adminService.isOpen(runwayName);
                    logger.info("Runway " + runwayName + " is "+ (isOpen?"open.":"closed.") );
                    break;
                case TAKEOFF:
                    adminService.takeOffOrder();
                    logger.info("OK.");
                    break;
                case REORDER:
                    ReorderStatus  rStatus = adminService.reorderRunways();
                    rStatus.getFailed().stream().forEach(f ->  {logger.info("Cannot assign Flight " + f.getFlightId() + ".");});
                    logger.info( rStatus.getAssigned() + " flights assigned.");
                    break;
            }

        }
        catch (Exception e){
            logger.error(e.toString());
        }


    }

}

enum ActionType {
    ADD("add"),
    OPEN("open"),
    CLOSE("close"),
    STATUS("status"),
    TAKEOFF("takeOff"),
    REORDER("reorder");

    public final String value;

    ActionType(final String value) {
        this.value = value;
    }

    public static Optional<ActionType> getEnumOf(String value) {
        return Arrays.stream(values()).filter(act -> act.value.equals(value)).findFirst();
    }

}
