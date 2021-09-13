package ar.edu.itba.pod.api.interfaces;

import ar.edu.itba.pod.api.model.Flight;

import java.util.List;

public interface ConsultService {
    public List<Flight> getAirportDepartures();
    public List<Flight> getRunwayDepartures(String name);
    public List<Flight> getAirlineDeparture(String airline);
}
