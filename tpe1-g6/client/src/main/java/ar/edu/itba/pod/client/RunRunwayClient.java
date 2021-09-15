package ar.edu.itba.pod.client;

import ar.edu.itba.pod.api.exceptions.RunwayNotAssignedException;
import ar.edu.itba.pod.api.interfaces.RunwayService;
import ar.edu.itba.pod.api.model.RunwayType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ar.edu.itba.pod.api.model.Flight;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

public class RunRunwayClient {
    private static Logger logger = LoggerFactory.getLogger(RunRunwayClient.class);

    public static void main(String[] args) {

        // Params
        String fileName = Optional.ofNullable(System.getProperty("inPath")).orElseThrow(IllegalArgumentException::new);
        String serverAddress = Optional.ofNullable(System.getProperty("serverAddress")).orElseThrow(IllegalArgumentException::new);

        logger.info("tpe1-g6 Run Runway Client Starting ...");

        final RunwayService runwayService;
        final Registry registry;
        try {
            registry = LocateRegistry.getRegistry(serverAddress);
            runwayService = (RunwayService) Naming.lookup("runway");
        } catch (Exception e) {
            logger.error(e.getMessage());
            return;
        }

        // Local Variables
        int assignedFlights = 0;


        List<Flight> flightList = readFlightFile(fileName);

        for (Flight flight : flightList) {
            try {
                runwayService.requestRunway(
                        flight.getFlightId(),
                        flight.getDestCode(),
                        flight.getAirline(),
                        flight.getMinType()
                );
                assignedFlights++;
            } catch (RunwayNotAssignedException rnae) {
                // print error
                logger.info(String.format("Cannot assign Flight %d.", flight.getFlightId()));
            } catch (RemoteException re) {
                logger.error(re.getMessage());
            }
        }
        logger.info(String.format("%d flights assigned.", assignedFlights));
    }

    private static List<Flight> readFlightFile(String fileName) {
        List<Flight> flightList = new ArrayList<>();
        Path pathToFile = Paths.get(fileName);

        try {
            BufferedReader br = Files.newBufferedReader(pathToFile, StandardCharsets.UTF_8);

            // to check first line and skip because it is header
            String line = br.readLine();

            while ((line= br.readLine()) != null) {
                Flight flight = stringToFlight(line);
                flightList.add(flight);
            }


        } catch (IOException ioe) {
            // Do sth
            logger.error(ioe.getMessage());
        } catch (RuntimeException re) {
            // other types of exception
            logger.error(re.getMessage());
        }

        return flightList;
    }

    private static Flight stringToFlight(String line) {
        String[] flightStrings = line.split(";");
        Flight flight = new Flight(
                Integer.parseInt(flightStrings[0].trim()),
                flightStrings[1].trim(),
                flightStrings[2].trim(),
                RunwayType.valueOf(flightStrings[3].trim())
        );

        return flight;
    }
}

