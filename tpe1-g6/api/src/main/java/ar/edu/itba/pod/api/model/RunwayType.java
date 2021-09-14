package ar.edu.itba.pod.api.model;

// Enums are by default serializable, no need to implement the interface
public enum RunwayType {
    A("A"),
    B("B"),
    C("C"),
    D("D"),
    E("E"),
    F("F");

    public final String value;

    RunwayType(final String value) {
        this.value = value;
    }
}
