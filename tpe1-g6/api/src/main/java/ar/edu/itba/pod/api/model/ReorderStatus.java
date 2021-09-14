package ar.edu.itba.pod.api.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ReorderStatus implements Serializable {
    private final List<Flight> failed;
    private final int assigned;

    public ReorderStatus(final List<Flight> failed, final int assigned) {
        this.failed = new ArrayList<>(failed);
        this.assigned = assigned;
    }

    public int getAssigned() {
        return assigned;
    }

    public List<Flight> getFailed() {
        return new ArrayList<>(failed);
    }
}
