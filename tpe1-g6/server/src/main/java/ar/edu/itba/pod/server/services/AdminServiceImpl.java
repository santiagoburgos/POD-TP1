package ar.edu.itba.pod.server.services;

import ar.edu.itba.pod.api.interfaces.AdminService;
import ar.edu.itba.pod.api.model.Airport;
import ar.edu.itba.pod.api.model.Flight;
import ar.edu.itba.pod.api.model.Runway;

import java.util.*;

import ar.edu.itba.pod.api.model.RunwayType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdminServiceImpl implements AdminService {
    private static final Logger logger = LoggerFactory.getLogger(AdminServiceImpl.class);
    private final Airport airport;

    public AdminServiceImpl(Airport airport) {
        this.airport = airport;
    }

    @Override
    public void addRunway(String name, RunwayType type){
        Runway runway = new Runway(name, type);
        Map<RunwayType, List<Runway>> runwaysMap = airport.getRunwaysByType();

        if(!runwaysMap.containsKey(type)) {
            List<Runway> runways = new LinkedList<>();
            runwaysMap.put(type, runways);
        }

        List<Runway> runways = runwaysMap.get(type);
        if(runways.contains(runway)){
            logger.error("Runway {} already exists", runway.getName());
        }

        logger.info("Runway {} with category {} added", runway.getName(), runway.getType());
        runways.add(runway);
    }

    @Override
    public boolean isOpen(String name) {
        Optional<Runway> runway = airport.getRunway(name);
        if(runway.isPresent()){
            return runway.get().isOpen();
        }
        logger.error("Runway {} does not exists", name);
        return false;
    }

    @Override
    public void openRunway(String name) {
        Optional<Runway> runway = airport.getRunway(name);
        if(runway.isPresent()){
            if(!runway.get().isOpen()){
                runway.get().openRunway();
            } else {
                logger.error("Runway {} is already opened", name);
            }
        } else {
            logger.error("Runway {} does not exists", name);
        }
    }

    @Override
    public void closeRunway(String name) {
        Optional<Runway> runway = airport.getRunway(name);
        if(runway.isPresent()){
            if(!runway.get().isOpen()){
                runway.get().closeRunway();
            } else {
                logger.error("Runway {} is already closed", name);
            }
        } else {
            logger.error("Runway {} does not exists", name);
        }
    }

    @Override
    public void takeOffOrder() {
        List<Runway> runways = airport.getAllRunways();

        runways.stream().filter(Runway::isOpen).forEach(Runway::makeDeparture);

    }

    @Override
    public void reorderRunways() {
        Map<RunwayType, List<Runway>> runwaysMap = airport.getRunwaysByType();

        List<Flight> allFlights = airport.getAllFlights();
        airport.emptyRunways();

        allFlights.sort((o1, o2) -> {
            int compType = o1.getMinType().compareTo(o2.getMinType());
            int compName = o1.getFlightId().compareTo(o2.getFlightId());
            if(compType == 0) return compName;
            return compType;
        });

        allFlights.forEach(flight -> {
            RunwayType minType = flight.getMinType();
            Runway newRunway = runwaysMap.values()
                                        .stream()
                                        .flatMap(List::stream)
                                        .filter(Runway::isOpen)
                                        .filter(runway -> runway.getType().compareTo(minType) >= 0)
                                        .min(Comparator.comparingInt(Runway::flightsInQueue))
                                        .get();
            newRunway.addFlightToQueue(flight);
        });
    }


}
