package ar.edu.itba.pod.api.interfaces;

import ar.edu.itba.pod.api.exceptions.RunwayNotAssignedException;
import ar.edu.itba.pod.api.model.RunwayType;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RunwayService extends Remote {
    // If it could not assign a runway to the flight it will throw RunwayNotAssignedException
    void requestRunway(int flightId, String destCode, String airline, RunwayType minType) throws RemoteException, RunwayNotAssignedException;
}
