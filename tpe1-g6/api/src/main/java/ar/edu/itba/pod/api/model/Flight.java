package ar.edu.itba.pod.api.model;

import java.io.Serializable;

public class Flight implements Serializable {
    private final int flightId;
    private final String destCode;
    private final String airline;
    private final RunwayType minType;

    public Flight(final int flightId, final String destCode, final String airline, final RunwayType minType) {
        this.flightId = flightId;
        this.destCode = destCode;
        this.airline = airline;
        this.minType = minType;
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

    public Flight copy() {
        return new Flight(flightId, destCode, airline, minType);
    }
}
