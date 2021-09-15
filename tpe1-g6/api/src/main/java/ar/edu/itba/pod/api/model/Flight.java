package ar.edu.itba.pod.api.model;

import java.io.Serializable;
import java.util.Objects;

public class Flight implements Serializable, Comparable<Flight> {
    private final int flightId;
    private final String destCode;
    private final String airline;
    private final RunwayType minType;
    private int waitTime;
    private String assignedRunway;
    private int ahead;

    public Flight(final int flightId, final String destCode, final String airline, final RunwayType minType) {
        this.flightId = flightId;
        this.destCode = destCode;
        this.airline = airline;
        this.minType = minType;
        this.waitTime = 0;
        this.assignedRunway = null;
        this.ahead = 0;
    }

    private Flight(final int flightId, final String destCode, final String airline, final RunwayType minType, final int waitTime, final String assignedRunway, final int ahead) {
        this(flightId, destCode, airline, minType);
        this.waitTime = waitTime;
        this.assignedRunway = assignedRunway;
        this.ahead = ahead;
    }

    public int getFlightId() {
        return flightId;
    }

    public String getDestCode() {
        return destCode;
    }

    public String getAirline() {
        return airline;
    }

    public RunwayType getMinType() {
        return minType;
    }

    public int getWaitTime() {
        return waitTime;
    }

    public void increaseWaitTime() {
        waitTime++;
    }

    public String getAssignedRunway() {
        return assignedRunway;
    }

    public int getAhead() {
        return ahead;
    }

    public boolean isAssigned() {
        return assignedRunway != null;
    }

    public void assignRunway(final String name, final int ahead) {
        this.assignedRunway = name;
        this.ahead = name != null ? ahead : 0;
    }

    public void clearAssignedRunway() {
        this.assignedRunway = null;
        this.ahead = 0;
    }

    public void decreaseAhead() {
        ahead = Math.max(ahead - 1, 0);
    }

    // TODO not used
    public Flight copy() {
        return new Flight(flightId, destCode, airline, minType, waitTime, assignedRunway, ahead);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Flight flight = (Flight) o;
        return flightId == flight.flightId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(flightId);
    }

    @Override
    public int compareTo(Flight o) {
        int compType = minType.value.compareTo(o.minType.value);
        int compId = flightId - o.flightId;
        return compType == 0 ? compId : compType;
    }
}
