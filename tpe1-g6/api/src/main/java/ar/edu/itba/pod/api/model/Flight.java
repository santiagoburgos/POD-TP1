package ar.edu.itba.pod.api.model;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Flight {
    private final String flightId;
    private final String destCode;
    private final String airline;
    private final RunwayType minType;

    public Flight(String flightId, String destCode, String airline, RunwayType minType) {
        this.flightId = flightId;
        this.destCode = destCode;
        this.airline = airline;
        this.minType = minType;
    }

    public String getFlightId() {
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

    public Flight copy() {
        return new Flight(flightId, destCode, airline, minType);
    }
}
