package ar.edu.itba.pod.api.interfaces;

import ar.edu.itba.pod.api.model.Runway;
import ar.edu.itba.pod.api.model.RunwayType;

import javax.management.InstanceAlreadyExistsException;

public interface AdminService {
    public void addRunway(String name, RunwayType type);
    public boolean isOpen(String name);
    public void openRunway(String name);
    public void closeRunway(String name);
    public void takeOffOrder();
    public void reorderRunways();
}
