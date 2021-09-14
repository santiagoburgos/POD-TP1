package ar.edu.itba.pod.api.interfaces;

import ar.edu.itba.pod.api.exceptions.QueryNotAllowedException;
import ar.edu.itba.pod.api.model.Flight;
import com.sun.istack.internal.Nullable;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ConsultService extends Remote {
    // If both parameters are not null it will throw QueryNotAllowedException
    // (Technically the client should check before calling this, but for completeness we add the exception)
    List<Flight> getDepartures(@Nullable String runway, @Nullable String airline) throws RemoteException, QueryNotAllowedException;
}
