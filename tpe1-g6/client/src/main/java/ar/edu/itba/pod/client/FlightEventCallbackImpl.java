package ar.edu.itba.pod.client;

import ar.edu.itba.pod.api.callbacks.FlightEventCallback;

import java.rmi.RemoteException;

public class FlightEventCallbackImpl implements FlightEventCallback {
    @Override
    public void flightAssigned(int flightId, String destCode, String runway, int flightsAhead) throws RemoteException {
        System.out.println("Flight + "+flightId+" with destiny "+destCode+" was assigned to runway "+runway+" and there are "+flightsAhead+" flights waiting ahead." );
    }

    @Override
    public void flightUpdated(int flightId, String destCode, String runway, int flightsAhead) throws RemoteException {
        System.out.println("A flight departed from runway + "+runway+". Flight "+flightId+" waith destiny "+destCode+" has "+flightsAhead+" flights waiting ahead." );
    }

    @Override
    public void flightDeparted(int flightId, String destCode, String runway) throws RemoteException {
        System.out.println("Flight " + flightId +  " with destiny "+destCode+" departed on runway "+runway+"." );

    }
}
