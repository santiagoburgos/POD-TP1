package ar.edu.itba.pod.server.servants;

import ar.edu.itba.pod.api.callbacks.FlightEventCallback;
import ar.edu.itba.pod.api.exceptions.*;
import ar.edu.itba.pod.api.interfaces.AdminService;
import ar.edu.itba.pod.api.interfaces.ConsultService;
import ar.edu.itba.pod.api.interfaces.RunwayService;
import ar.edu.itba.pod.api.interfaces.TrackingService;
import ar.edu.itba.pod.api.model.Flight;
import ar.edu.itba.pod.api.model.ReorderStatus;
import ar.edu.itba.pod.api.model.RunwayType;
import ar.edu.itba.pod.server.model.Airport;
import ar.edu.itba.pod.server.model.Runway;

import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class Servant implements AdminService, ConsultService, RunwayService, TrackingService {
    private final Airport airport;

    private final Map<Integer, List<FlightEventCallback>> trackers;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    private final ReentrantReadWriteLock reentrantLock = new ReentrantReadWriteLock(true);
    private final Lock readLock = reentrantLock.readLock();
    private final Lock writeLock = reentrantLock.writeLock();

    public Servant() {
        this.airport = new Airport();
        this.trackers = new HashMap<>();
    }

    public boolean awaitTermination() throws InterruptedException {
        executor.shutdown();
        return executor.awaitTermination(30, TimeUnit.MINUTES);
    }

    @Override
    public void addRunway(String name, RunwayType type) throws RemoteException, RunwayAlreadyExistsException {
        // We check if exists first (Has higher chance of quickly checking if exists without calling writeLock and stopping all other reads)
        readLock.lock();
        try {
            if(airport.runwayExists(name))
                throw new RunwayAlreadyExistsException();
        } finally { readLock.unlock(); }

        Runway newRunway = new Runway(name, type);
        writeLock.lock();
        try {
            // We need to revalidate that it does not exist, so we take readLock again
            if (airport.runwayExists(name))
                throw new RunwayAlreadyExistsException();
            airport.addRunway(newRunway);
        } finally { writeLock.unlock(); }
    }

    @Override
    public boolean isOpen(String name) throws RemoteException, RunwayNotFoundException {
        readLock.lock();
        try {
            return airport.getRunway(name).orElseThrow(RunwayNotFoundException::new).isOpen();
        } finally { readLock.unlock(); }
    }

    @Override
    public void openRunway(String name) throws RemoteException, RunwayNotFoundException, InvalidRunwayOperationException {
        // We try with readLock first to avoid blocking other reads
        readLock.lock();
        try {
            if (airport.getRunway(name).orElseThrow(RunwayNotFoundException::new).isOpen())
                throw new InvalidRunwayOperationException();
        } finally { readLock.unlock(); }

        writeLock.lock();
        try {
            Runway r = airport.getRunway(name).orElseThrow(RunwayNotFoundException::new);
            if (r.isOpen())
                throw new InvalidRunwayOperationException();
            r.openRunway();
        } finally { writeLock.unlock(); }
    }

    @Override
    public void closeRunway(String name) throws RemoteException, RunwayNotFoundException, InvalidRunwayOperationException {
        // We try with readLock first to avoid blocking other reads
        readLock.lock();
        try {
            if (!airport.getRunway(name).orElseThrow(RunwayNotFoundException::new).isOpen())
                throw new InvalidRunwayOperationException();
        } finally { readLock.unlock(); }

        writeLock.lock();
        try {
            Runway r = airport.getRunway(name).orElseThrow(RunwayNotFoundException::new);
            if (!r.isOpen())
                throw new InvalidRunwayOperationException();
            r.closeRunway();
        } finally { writeLock.unlock(); }
    }

    @Override
    public void takeOffOrder() throws RemoteException {
        List<Flight> departed = new ArrayList<>();
        writeLock.lock();
        try {
            airport.getRunways().stream().map(Runway::makeDeparture).filter(Optional::isPresent).map(Optional::get).forEach(departed::add);
            // Lock downgrading to avoid issues
            readLock.lock();
        } finally { writeLock.unlock(); }
        List<Flight> updated;
        try {
            updated = airport.getRunways().stream().filter(Runway::isOpen).map(Runway::getQueued).flatMap(Collection::stream).collect(Collectors.toList());
        } finally { readLock.unlock(); }

        departed.forEach(f -> {
            notifyFlightDeparted(f.getFlightId(), f.getDestCode(), f.getAssignedRunway());
        });

        updated.forEach(f -> {
            notifyFlightUpdated(f.getFlightId(), f.getDestCode(), f.getAssignedRunway(), f.getAhead());
        });
    }

    @Override
    public ReorderStatus reorderRunways() throws RemoteException {
        final List<Flight> flights = new ArrayList<>();
        final List<Flight> assigned = new ArrayList<>();
        writeLock.lock();
        try {
            List<Runway> runways = airport.getRunways();
            while (runways.stream().anyMatch(Runway::notEmpty)) {
                runways.stream().map(Runway::removeFlight).filter(Optional::isPresent).map(Optional::get).forEach(flights::add);
            }
            flights.forEach(f -> {
                Optional<Runway> availableRunway = runways.stream().filter(Runway::isOpen).filter(r -> f.getMinType().value.compareTo(r.getType().value) <= 0)
                        .min(Comparator.naturalOrder());
                //flights.remove(f);
                availableRunway.flatMap(r -> r.addFlightToQueue(f)).ifPresent(assigned::add);
            });
        } finally { writeLock.unlock(); }

        // Notifying the assignments
        assigned.forEach(f -> {
            notifyRunwayAssigned(f.getFlightId(), f.getDestCode(), f.getAssignedRunway(), f.getAhead());
        });

        flights.removeAll(assigned);
        return new ReorderStatus(flights, assigned.size());
    }

    @Override
    public List<Flight> getDepartures(String runway, String airline) throws RemoteException, QueryNotAllowedException {
        if (runway != null && airline != null)
            throw new QueryNotAllowedException();

        if (runway != null) {
            readLock.lock();
            try {
                Optional<Runway> r = airport.getRunway(runway);
                if (!r.isPresent())
                    return new ArrayList<>();
                return r.get().getDeparted(null);
            } finally { readLock.unlock(); }
        }

        readLock.lock();
        try {
            return airport.getRunways().stream().map(r -> r.getDeparted(airline)).flatMap(Collection::stream).collect(Collectors.toList());
        } finally { readLock.unlock(); }
    }

    @Override
    public void requestRunway(int flightId, String destCode, String airline, RunwayType minType) throws RemoteException, RunwayNotAssignedException {
        final Flight f = new Flight(flightId, destCode, airline, minType);
        Flight assigned;
        writeLock.lock();
        try {
            assigned = airport.getRunways().stream().filter(r -> f.getMinType().value.compareTo(r.getType().value) <= 0)
                    .min(Comparator.naturalOrder()).orElseThrow(RunwayNotAssignedException::new).addFlightToQueue(f).orElseThrow(RunwayNotAssignedException::new);
        } finally { writeLock.unlock(); }

        notifyRunwayAssigned(assigned.getFlightId(), assigned.getDestCode(), assigned.getAssignedRunway(), assigned.getAhead());
    }

    @Override
    public void register(int flightId, String airline, FlightEventCallback callback) throws RemoteException, TrackingNotAllowedException, FlightNotFoundException {
        // We try with readLock first to avoid blocking other reads
        readLock.lock();
        try {
            Flight f = airport.getRunways().stream().map(r -> r.findFlight(flightId))
                    .filter(Optional::isPresent).findFirst().orElseThrow(FlightNotFoundException::new).orElseThrow(FlightNotFoundException::new);
            if (!f.getAirline().equals(airline))
                throw new TrackingNotAllowedException();
        } finally { readLock.unlock(); }

        if (callback == null)
            return;
        writeLock.lock();
        try {
            // We must make sure flight has not departed while we waited for the lock
            if (airport.getRunways().stream().noneMatch(r -> r.hasFlight(flightId)))
                throw new FlightNotFoundException();
            List<FlightEventCallback> callbacks = trackers.computeIfAbsent(flightId, k -> new ArrayList<>());
            callbacks.add(callback);
        } finally { writeLock.unlock(); }
    }

    public Optional<Runway> getRunwayByName(String name) {
        return airport.getRunway(name);
    }

    // Notification handlers
    private void notifyRunwayAssigned(int flightId, String destCode, String runwayName, int ahead) {
        // Notification part (TODO: make the calls in threads)
        readLock.lock();
        List<FlightEventCallback> toNotify;
        try {
            toNotify = trackers.getOrDefault(flightId, new ArrayList<>());
        } finally { readLock.unlock(); }
        for (FlightEventCallback c : toNotify) {
            executor.submit(() -> {
                try {
                    c.flightAssigned(flightId, destCode, runwayName, ahead);
                } catch (RemoteException ignored) {

                }
            });
        }
    }

    private void notifyFlightUpdated(int flightId, String destCode, String runwayName, int ahead) {
        readLock.lock();
        List<FlightEventCallback> toNotify;
        try {
            toNotify = trackers.getOrDefault(flightId, new ArrayList<>());
        } finally { readLock.unlock(); }
        for (FlightEventCallback c: toNotify) {
            executor.submit(() -> {
                try {
                    c.flightUpdated(flightId, destCode, runwayName, ahead);
                } catch (RemoteException ignored) {

                }
            });
        }
    }

    private void notifyFlightDeparted(int flightId, String destCode, String runwayName) {
        readLock.lock();
        List<FlightEventCallback> toNotify;
        try {
            toNotify = trackers.getOrDefault(flightId, new ArrayList<>());
        } finally { readLock.unlock(); }
        for (FlightEventCallback c: toNotify) {
            executor.submit(() -> {
                try {
                    c.flightDeparted(flightId, destCode, runwayName);
                } catch (RemoteException ignored) {

                }
            });
        }
    }
}
