package ar.edu.itba.pod.client;

import ar.edu.itba.pod.api.exceptions.QueryNotAllowedException;
import ar.edu.itba.pod.api.interfaces.ConsultService;
import ar.edu.itba.pod.api.model.Flight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Optional;

public class RunQueryClient {
    private static Logger logger = LoggerFactory.getLogger(RunQueryClient.class);

    public static void main(String[] args) {
        // Params
        String fileName = Optional.ofNullable(System.getProperty("outPath")).orElseThrow(IllegalArgumentException::new);
        String serverAddress = Optional.ofNullable(System.getProperty("serverAddress")).orElseThrow(IllegalArgumentException::new);

        // Nullable params
        String airlineName = System.getProperty("airline");
        String runwayName = System.getProperty("runway");

        logger.info("tpe1-g6 Run Query Client Starting ...");

        // When both airline and runway are specified
        if (airlineName != null && runwayName != null) {
            logger.error("QueryNotAllowedException: Both airline and runway were provided, specify only either of them");
            return;
        }

        String[] address = serverAddress.split(":");
        String host = address[0];
        String port = address[1];

        final ConsultService consultService;
        final Registry registry;

        try {
            registry = LocateRegistry.getRegistry(host, Integer.parseInt(port));
            consultService = (ConsultService) registry.lookup("consult");
        } catch (Exception e) {
            logger.error(e.toString());
            return;
        }

        List<Flight> flightList;
        try {
            flightList = consultService.getDepartures(runwayName, airlineName);

            // only write to file if list is not empty
            if(!flightList.isEmpty())
                writeFlightFile(fileName, flightList);
            else
                logger.info("No flights matched query conditions");
        } catch (QueryNotAllowedException qnae) {
            // should not fall here but specified for completeness
            logger.error("QueryNotAllowedException: Both airline and runway were provided, specify only either of them");
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    private static void writeFlightFile(String fileName, List<Flight> flightList) throws IOException {

        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));

        //header
        writer.write("TakeOffOrders;RunwayName;FlightCode;DestinyAirport;AirlineName\n");

        for (Flight flight : flightList) {
            writer.append(
                    String.format("%d;%s;%d;%s;%s\n",
                            flight.getWaitTime(),
                            flight.getAssignedRunway(),
                            flight.getFlightId(),
                            flight.getDestCode(),
                            flight.getAirline()
                    ));
        }

        writer.close();
    }
}
