package ar.edu.itba.pod.api.callbacks;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FlightEventCallback extends Remote, Serializable {
    // Called when the tracked flight is assigned to a runway (First time or during a reorder)
    void flightAssigned(int flightId, String destCode, String runway, int flightsAhead) throws RemoteException;
    // Called when a flight in front of the tracked flight departed, updating the position in the departure queue
    void flightUpdated(int flightId, String destCode, String runway, int flightsAhead) throws RemoteException;
    // Called when the tracked flight departed
    void flightDeparted(int flightId) throws RemoteException;
}
