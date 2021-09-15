package ar.edu.itba.pod.server.model;

import ar.edu.itba.pod.api.model.Flight;
import ar.edu.itba.pod.api.model.RunwayType;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class Runway implements Serializable, Comparable<Runway> {
    private final String name;
    private final RunwayType type;
    private boolean isOpen;
    private final Queue<Flight> flightsQueue;
    private final List<Flight> departures;

    public Runway(final String name, final RunwayType type) {
        this.name = name;
        this.type = type;
        // Runways are open by default
        this.isOpen = true;
        this.flightsQueue = new LinkedList<>();
        this.departures = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public RunwayType getType() {
        return type;
    }

    public boolean notEmpty() {
        return !flightsQueue.isEmpty();
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void openRunway() {
        this.isOpen = true;
    }

    public void closeRunway() {
        this.isOpen = false;
    }

    // Returns copy of the updated flight if successful
    public Optional<Flight> addFlightToQueue(Flight flight) {
        if (flight == null) return Optional.empty();
        Flight toAdd = flight.copy();
        toAdd.assignRunway(name, flightsQueue.size());
        return flightsQueue.offer(toAdd) ? Optional.of(toAdd.copy()) : Optional.empty();
    }

    // Returns copy of departed flight
    public Optional<Flight> makeDeparture() {
        flightsQueue.forEach(f -> {
            f.increaseWaitTime();
            if (isOpen)
                f.decreaseAhead();
        });
        Optional<Flight> toDepart = isOpen ? Optional.ofNullable(flightsQueue.poll()) : Optional.empty();
        toDepart.ifPresent(departures::add);
        return toDepart.map(Flight::copy);
    }

    // We return copies since once departed the data should be final
    public List<Flight> getDeparted(String airline) {
        return departures.stream().filter(f -> airline == null || f.getAirline().equals(airline)).map(Flight::copy).collect(Collectors.toList());
    }

    // Method returns a copy
    public Optional<Flight> findFlight(int flightId) {
        return flightsQueue.stream().filter(f -> f.getFlightId() == flightId).findFirst().map(Flight::copy);
    }

    // Method returns the reference since we are removing it
    public Optional<Flight> removeFlight() {
        Optional<Flight> removed = Optional.ofNullable(flightsQueue.poll());
        removed.ifPresent(Flight::clearAssignedRunway);
        return removed;
    }

    public boolean hasFlight(int flightId) {
        return flightsQueue.stream().anyMatch(f -> f.getFlightId() == flightId);
    }

    // Flight are copy
    public List<Flight> getQueued() {
        return flightsQueue.stream().map(Flight::copy).collect(Collectors.toList());
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

    @Override
    public int compareTo(Runway o) {
        int compQueue = flightsQueue.size() - o.flightsQueue.size();
        int compType = type.value.compareTo(o.type.value);
        int compName = name.compareTo(o.name);
        return compQueue == 0 ? (compType == 0 ? compName : compType) : compQueue;
    }
}
