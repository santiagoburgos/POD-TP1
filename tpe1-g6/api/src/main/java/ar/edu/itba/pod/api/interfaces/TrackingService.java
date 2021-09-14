package ar.edu.itba.pod.api.interfaces;

import ar.edu.itba.pod.api.callbacks.FlightEventCallback;
import ar.edu.itba.pod.api.exceptions.FlightNotFoundException;
import ar.edu.itba.pod.api.exceptions.TrackingNotAllowedException;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface TrackingService extends Remote {
    // If airline does not match the airline for the flight id then TrackingNotAllowedException will be thrown
    void register(int flightId, String airline, FlightEventCallback callback) throws RemoteException, TrackingNotAllowedException, FlightNotFoundException;
}
