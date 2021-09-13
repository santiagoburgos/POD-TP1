package ar.edu.itba.pod.api.model;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Runway {
    private final String name;
    private final RunwayType type;
    private boolean isOpen;
    private final Queue<Flight> flightsQueue;
    private final List<Flight> departures;

    private final ReentrantReadWriteLock reentrantLock = new ReentrantReadWriteLock(true);
    private final Lock readLock = reentrantLock.readLock();
    private final Lock writeLock = reentrantLock.writeLock();

    public Runway(final String name, final RunwayType type) {
        this.name = name;
        this.type = type;
        this.isOpen = false;
        this.flightsQueue = new LinkedList<>();
        this.departures = new LinkedList<>();
    }

    public String getName() {
        return name;
    }

    public RunwayType getType() {
        return type;
    }

    public boolean isOpen() {
        readLock.lock();
        final boolean returnStatus = this.isOpen;
        readLock.unlock();

        return returnStatus;
    }

    public void openRunway() {
        writeLock.lock();
        this.isOpen = true;
        writeLock.unlock();
    }

    public void closeRunway() {
        writeLock.lock();
        this.isOpen = false;
        writeLock.unlock();
    }

    public void addFlightToQueue(Flight flight) {
        Flight copyFlight = flight.copy();

        writeLock.lock();
        flightsQueue.add(copyFlight);
        writeLock.unlock();
    }

    public void makeDeparture() {
        writeLock.lock();
        Optional<Flight> flight = Optional.ofNullable(flightsQueue.poll());
        flight.ifPresent(departures::add);
        writeLock.unlock();
    }

    public Queue<Flight> getQueue() {
        readLock.lock();
        Queue<Flight> queue = new LinkedList<>(flightsQueue);
        readLock.unlock();

        return queue;
    }

    public void emptyQueue() {
        writeLock.lock();
        flightsQueue.clear();
        writeLock.unlock();
    }

    public List<Flight> getDepartures() {
        readLock.lock();
        List<Flight> flights = new LinkedList<>(departures);
        readLock.unlock();

        return flights;
    }

    public int flightsInQueue() {
        readLock.lock();
        int size = flightsQueue.size();
        readLock.unlock();

        return size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Runway runway = (Runway) o;
        return name.equals(runway.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
