package ar.edu.itba.pod.client;

import ar.edu.itba.pod.api.callbacks.FlightEventCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;

public class FlightEventCallbackImpl implements FlightEventCallback {

    private static Logger logger = LoggerFactory.getLogger(FlightEventCallbackImpl.class);

    @Override
    public void flightAssigned(int flightId, String destCode, String runway, int flightsAhead) throws RemoteException {
        logger.info("Flight + "+flightId+" with destiny "+destCode+" was assigned to runway "+runway+" and there are "+flightsAhead+" flights waiting ahead." );
    }

    @Override
    public void flightUpdated(int flightId, String destCode, String runway, int flightsAhead) throws RemoteException {
        logger.info("A flight departed from runway + "+runway+". Flight "+flightId+" waith destiny "+destCode+" has "+flightsAhead+" flights waiting ahead." );
    }

    @Override
    public void flightDeparted(int flightId, String destCode, String runway) throws RemoteException {
        logger.info("Flight " + flightId +  " with destiny "+destCode+" departed on runway "+runway+"." );

    }
}
