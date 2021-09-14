package ar.edu.itba.pod.api.model;

import java.io.Serializable;

public class Flight implements Serializable, Comparable<Flight> {
    private final int flightId;
    private final String destCode;
    private final String airline;
    private final RunwayType minType;
    private int waitTime;

    public Flight(final int flightId, final String destCode, final String airline, final RunwayType minType) {
        this.flightId = flightId;
        this.destCode = destCode;
        this.airline = airline;
        this.minType = minType;
        this.waitTime = 0;
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

    // TODO not used
    public Flight copy() {
        Flight copy = new Flight(flightId, destCode, airline, minType);
        copy.waitTime = waitTime;
        return copy;
    }

    @Override
    public int compareTo(Flight o) {
        int compType = minType.value.compareTo(o.minType.value);
        int compId = flightId - o.flightId;
        return compType == 0 ? compId : compType;
    }
}
