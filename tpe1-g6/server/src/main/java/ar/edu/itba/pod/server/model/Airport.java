package ar.edu.itba.pod.server.model;

import java.io.Serializable;
import java.util.*;

public class Airport implements Serializable {
    private final List<Runway> runways;

    public Airport() {
        this.runways = new ArrayList<>();
    }
    
    // Runway is a reference, it's up to the user of the class to deal with making it thread safe
    public Optional<Runway> getRunway(String name) {
        return runways.stream().filter(r -> r.getName().equals(name)).findFirst();
    }

    // Returning a copy since we don't want to allow the removing of runways
    public List<Runway> getRunways() {
        return new LinkedList<>(runways);
    }

    public void addRunway(Runway r) {
        runways.add(r);
    }

    public boolean runwayExists(String name) {
        return runways.stream().anyMatch(r -> r.getName().equals(name));
    }
}
