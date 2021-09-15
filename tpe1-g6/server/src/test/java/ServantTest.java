import ar.edu.itba.pod.api.callbacks.FlightEventCallback;
import ar.edu.itba.pod.api.exceptions.*;
import ar.edu.itba.pod.api.model.Flight;
import ar.edu.itba.pod.api.model.RunwayType;
import ar.edu.itba.pod.server.model.Runway;
import ar.edu.itba.pod.server.servants.Servant;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Optional;

public class ServantTest {
    private Servant servant;
    private final String runwayName = "rName";
    private final String runwayName2 = "rName2";
    private final String runwayNameClosed = "rNameClosed";
    private final RunwayType runwayCategoryClosed = RunwayType.A;
    private final RunwayType runwayCategory = RunwayType.D;


    private final int flightId1 = 1;
    private final int flightId2 = 2;
    private final String flightCode1 = "DEST";
    private final String flightAirline1 = "Airline";
    private final String flightAirline2 = "Airline2";
    private final RunwayType flightMinCategory = RunwayType.B;

    @Before
    public void before() throws RunwayAlreadyExistsException, RemoteException, RunwayNotFoundException,
            InvalidRunwayOperationException, RunwayNotAssignedException {
        servant = new Servant();
        servant.addRunway(runwayName, runwayCategory);

        servant.requestRunway(flightId1, flightCode1, flightAirline1, flightMinCategory);

        servant.addRunway(runwayNameClosed, runwayCategoryClosed);
        servant.closeRunway(runwayNameClosed);
    }
    @Test
    public void addRunway() throws RunwayAlreadyExistsException, RemoteException {
        servant.addRunway(runwayName2, runwayCategory);

        Optional<Runway> r = servant.getRunwayByName(runwayName2);
        Assert.assertTrue(r.isPresent());
        Assert.assertEquals(new Runway(runwayName2, runwayCategory), r.get());
    }

    @Test(expected = RunwayAlreadyExistsException.class)
    public void addSameRunaway() throws RunwayAlreadyExistsException, RemoteException {
        servant.addRunway(runwayName, runwayCategory);
    }

    @Test(expected = RunwayAlreadyExistsException.class)
    public void addSameRunawayDiffType() throws RunwayAlreadyExistsException, RemoteException {
        servant.addRunway(runwayName, RunwayType.E);
    }

    @Test
    public void isOpen() throws RemoteException, RunwayNotFoundException {
        // When adding a runway it starts open
        Assert.assertTrue(servant.isOpen(runwayName));
    }

    @Test(expected = RunwayNotFoundException.class)
    public void isOpenNotAddedRunway() throws RemoteException, RunwayNotFoundException {
        Assert.assertTrue(servant.isOpen(runwayName2));
    }

    @Test
    public void openRunway() throws RemoteException, RunwayNotFoundException, InvalidRunwayOperationException {
        servant.openRunway(runwayNameClosed);
        Assert.assertTrue(servant.isOpen(runwayNameClosed));
    }

    @Test(expected = RunwayNotFoundException.class)
    public void openNotAddedRunway() throws RemoteException, RunwayNotFoundException, InvalidRunwayOperationException {
        servant.openRunway(runwayName2);
    }

    @Test(expected = InvalidRunwayOperationException.class)
    public void openOpenedRunway() throws RemoteException, RunwayNotFoundException, InvalidRunwayOperationException {
        servant.openRunway(runwayName);
    }

    @Test
    public void closeRunway() throws InvalidRunwayOperationException, RemoteException, RunwayNotFoundException {
        servant.closeRunway(runwayName);
        Assert.assertFalse(servant.isOpen(runwayName));
    }

    @Test(expected = RunwayNotFoundException.class)
    public void closeNotAddedRunway() throws InvalidRunwayOperationException, RemoteException, RunwayNotFoundException {
        servant.closeRunway(runwayName2);
    }

    @Test(expected = InvalidRunwayOperationException.class)
    public void closeClosedRunway() throws InvalidRunwayOperationException, RemoteException, RunwayNotFoundException {
        servant.closeRunway(runwayNameClosed);
    }

    @Test
    public void takeOffOrder() throws RemoteException {
        Assert.assertTrue(servant.getRunwayByName(runwayName).get().hasFlight(flightId1));
        servant.takeOffOrder();
        Assert.assertFalse(servant.getRunwayByName(runwayName).get().hasFlight(flightId1));
    }

    @Test
    public void reorderRunways() throws RemoteException, RunwayNotAssignedException,
            RunwayAlreadyExistsException {

        servant.requestRunway(flightId2, flightCode1, flightAirline1, flightMinCategory);
        servant.addRunway(runwayName2, runwayCategory);

        Assert.assertFalse(servant.getRunwayByName(runwayName2).get().notEmpty());

        servant.reorderRunways();

        Assert.assertTrue(servant.getRunwayByName(runwayName2).get().notEmpty());
    }

    @Test
    public void getAllDepartures() throws RemoteException, QueryNotAllowedException {
        servant.takeOffOrder();

        List<Flight> departures = servant.getDepartures(null, null);
        Assert.assertEquals(1, departures.size());
    }

    @Test
    public void getRunwayDepartures() throws RemoteException, QueryNotAllowedException {
        servant.takeOffOrder();

        List<Flight> departures = servant.getDepartures(null, flightAirline1);
        Assert.assertEquals(1, departures.size());
    }

    @Test
    public void getAirlineDepartures() throws RemoteException, QueryNotAllowedException {
        servant.takeOffOrder();

        List<Flight> departures = servant.getDepartures(runwayName, null);
        Assert.assertEquals(1, departures.size());
    }

    @Test(expected = QueryNotAllowedException.class)
    public void getAirlineDAndRunwayDepartures() throws RemoteException, QueryNotAllowedException {
        servant.takeOffOrder();
        servant.getDepartures(runwayName, flightAirline1);
    }

    @Test
    public void requestRunway() throws RemoteException, RunwayNotAssignedException {
        Assert.assertFalse(servant.getRunwayByName(runwayName).get().hasFlight(flightId2));
        servant.requestRunway(flightId2, flightCode1, flightAirline1, flightMinCategory);
        Assert.assertTrue(servant.getRunwayByName(runwayName).get().hasFlight(flightId2));
    }

    @Test(expected = RunwayNotAssignedException.class)
    public void requestRunwayNoAvailable() throws RemoteException, RunwayNotAssignedException {
        Assert.assertFalse(servant.getRunwayByName(runwayName).get().hasFlight(flightId2));
        servant.requestRunway(flightId2, flightCode1, flightAirline1, RunwayType.F);
    }

    @Test
    public void register() throws TrackingNotAllowedException, RemoteException, FlightNotFoundException {
        FlightEventCallback callback = new FlightEventCallback() {
            @Override
            public void flightAssigned(int flightId, String destCode, String runway, int flightsAhead) throws RemoteException {
            }

            @Override
            public void flightUpdated(int flightId, String destCode, String runway, int flightsAhead) throws RemoteException {
            }

            @Override
            public void flightDeparted(int flightId, String destCode, String runway) throws RemoteException {
            }
        };

        servant.register(flightId1, flightAirline1, callback);
    }

    @Test(expected = FlightNotFoundException.class)
    public void registerFlightNotFound() throws TrackingNotAllowedException, RemoteException, FlightNotFoundException {
        FlightEventCallback callback = new FlightEventCallback() {
            @Override
            public void flightAssigned(int flightId, String destCode, String runway, int flightsAhead) throws RemoteException {
            }

            @Override
            public void flightUpdated(int flightId, String destCode, String runway, int flightsAhead) throws RemoteException {
            }

            @Override
            public void flightDeparted(int flightId, String destCode, String runway) throws RemoteException {
            }
        };

        servant.register(flightId2, flightAirline1, callback);
    }

    @Test(expected = FlightNotFoundException.class)
    public void registerWrongAirline() throws TrackingNotAllowedException, RemoteException, FlightNotFoundException {
        FlightEventCallback callback = new FlightEventCallback() {
            @Override
            public void flightAssigned(int flightId, String destCode, String runway, int flightsAhead) throws RemoteException {
            }

            @Override
            public void flightUpdated(int flightId, String destCode, String runway, int flightsAhead) throws RemoteException {
            }

            @Override
            public void flightDeparted(int flightId, String destCode, String runway) throws RemoteException {
            }
        };

        servant.register(flightId2, flightAirline2, callback);
    }
}
