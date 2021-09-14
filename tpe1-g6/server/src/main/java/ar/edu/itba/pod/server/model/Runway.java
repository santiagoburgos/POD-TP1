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

    public boolean addFlightToQueue(Flight flight) {
        return flightsQueue.offer(flight);
    }

    public void updateWaitTime() {
        flightsQueue.forEach(Flight::increaseWaitTime);
    }

    public void makeDeparture() {
        Optional.ofNullable(flightsQueue.poll()).ifPresent(departures::add);
    }

    public List<Flight> getDeparted(String airline) {
        return departures.stream().filter(f -> airline == null || f.getAirline().equals(airline)).collect(Collectors.toList());
    }

    public Optional<Flight> findFlight(int flightId) {
        return flightsQueue.stream().filter(f -> f.getFlightId() == flightId).findFirst();
    }

    public Optional<Flight> removeFlight() {
        return Optional.ofNullable(flightsQueue.poll());
    }

    public boolean hasFlight(int flightId) {
        return flightsQueue.stream().anyMatch(f -> f.getFlightId() == flightId);
    }

    // This is kinda ugly, see better alternatives
    public int getAhead(int flightId) {
        int ahead = 0;
        for (Flight f: flightsQueue) {
            if (f.getFlightId() == flightId)
                return ahead;
            ahead++;
        }
        return ahead;
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
