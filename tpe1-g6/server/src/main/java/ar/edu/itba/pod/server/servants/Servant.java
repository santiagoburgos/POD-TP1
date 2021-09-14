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
        writeLock.lock();
        try {
            List<Runway> rl = airport.getRunways();
            rl.forEach(Runway::updateWaitTime);
            rl.stream().filter(Runway::isOpen).forEach(Runway::makeDeparture);
        } finally { writeLock.unlock(); }
    }

    private void notifyTakeOff() {
        // TODO
    }

    @Override
    public ReorderStatus reorderRunways() throws RemoteException {
        final List<Flight> flights = new ArrayList<>();
        final List<Flight> failed = new ArrayList<>();
        final Map<Integer, Runway> toNotify = new HashMap<>();
        writeLock.lock();
        try {
            List<Runway> runways = airport.getRunways();
            while (runways.stream().anyMatch(Runway::notEmpty)) {
                runways.stream().map(Runway::removeFlight).filter(Optional::isPresent).map(Optional::get).forEach(flights::add);
            }
            flights.forEach(f -> {
                Optional<Runway> availableRunway = runways.stream().filter(Runway::isOpen).filter(r -> f.getMinType().value.compareTo(r.getType().value) <= 0)
                        .min(Comparator.naturalOrder());
                if (!availableRunway.isPresent() || !availableRunway.get().addFlightToQueue(f))
                    failed.add(f);
                else
                    toNotify.put(f.getFlightId(), availableRunway.get());
            });
        } finally { writeLock.unlock(); }

        // Notifying the assignments
        flights.forEach(f -> {
            Runway r = toNotify.get(f.getFlightId());
            if (r != null)
                notifyRunwayAssigned(f.getFlightId(), f.getDestCode(), r.getName(), r.getAhead(f.getFlightId()));
        });

        return new ReorderStatus(failed, flights.size() - failed.size());
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
        Flight f = new Flight(flightId, destCode, airline, minType);
        Runway runway;
        writeLock.lock();
        try {
            runway = airport.getRunways().stream().filter(r -> f.getMinType().value.compareTo(r.getType().value) <= 0)
                    .min(Comparator.naturalOrder()).orElseThrow(RunwayNotAssignedException::new);
            if (!runway.addFlightToQueue(f))
                throw new RunwayNotAssignedException();
        } finally { writeLock.unlock(); }

        notifyRunwayAssigned(flightId, destCode, runway.getName(), runway.getAhead(flightId));
    }

    private void notifyRunwayAssigned(int flightId, String destCode, String runwayName, int ahead) {
        // Notification part (TODO: make the calls in threads)
        readLock.lock();
        List<FlightEventCallback> toNotify;
        try {
            toNotify = trackers.getOrDefault(flightId, new ArrayList<>());
        } finally { readLock.lock(); }
        for (FlightEventCallback c : toNotify) {
            executor.submit(() -> {
                try {
                    c.flightAssigned(flightId, destCode, runwayName, ahead);
                } catch (RemoteException ignored) {

                }
            });
        }
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
}
