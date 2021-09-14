package ar.edu.itba.pod.api.interfaces;

import ar.edu.itba.pod.api.exceptions.InvalidRunwayOperationException;
import ar.edu.itba.pod.api.exceptions.RunwayAlreadyExistsException;
import ar.edu.itba.pod.api.exceptions.RunwayNotFoundException;
import ar.edu.itba.pod.api.model.ReorderStatus;
import ar.edu.itba.pod.api.model.RunwayType;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AdminService extends Remote {
    void addRunway(String name, RunwayType type) throws RemoteException, RunwayAlreadyExistsException;
    boolean isOpen(String name) throws RemoteException, RunwayNotFoundException;
    void openRunway(String name) throws RemoteException, RunwayNotFoundException, InvalidRunwayOperationException;
    void closeRunway(String name) throws RemoteException, RunwayNotFoundException, InvalidRunwayOperationException;
    void takeOffOrder() throws RemoteException;
    ReorderStatus reorderRunways() throws RemoteException;
}
