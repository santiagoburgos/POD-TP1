package ar.edu.itba.pod.api.model;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class Airport {
    private final List<Runway> runways;

    private final ReentrantReadWriteLock reentrantLock = new ReentrantReadWriteLock(true);
    private final Lock readLock = reentrantLock.readLock();
    private final Lock writeLock = reentrantLock.writeLock();

    public Airport() {
        this.runways = new LinkedList<>();
    }

    public Map<RunwayType, List<Runway>> getRunwaysByType(){
        readLock.lock();
        Map<RunwayType, List<Runway>> runwaysByType = runways.stream().collect(Collectors.groupingBy(Runway::getType));
        readLock.unlock();

        return runwaysByType;
    }

    public List<Flight> getAllFlights() {
        List<Flight> allFlights = new LinkedList<>();

        readLock.lock();
        runways.forEach(runway -> allFlights.addAll(runway.getQueue()));
        readLock.unlock();

        return allFlights;
    }

    public void emptyRunways() {
        writeLock.lock();
        runways.clear();
        writeLock.unlock();
    }

    public Optional<Runway> getRunway(String name) {
        readLock.lock();
        Optional<Runway> runway =  runways.stream().filter(r -> r.getName().equals(name)).findFirst();
        readLock.unlock();

        return runway;
    }

    public List<Runway> getAllRunways() {
        readLock.lock();
        List<Runway> copy = new LinkedList<>(runways);
        readLock.unlock();

        return copy;
    }
}
