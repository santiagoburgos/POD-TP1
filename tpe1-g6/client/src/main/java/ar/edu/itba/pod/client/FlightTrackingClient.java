package ar.edu.itba.pod.client;

import ar.edu.itba.pod.api.callbacks.FlightEventCallback;
import ar.edu.itba.pod.api.interfaces.TrackingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Optional;

public class FlightTrackingClient {

    private static Logger logger = LoggerFactory.getLogger(FlightTrackingClient.class);

    public static void main(final String[] args){

        try{
            String serverAddress = Optional.ofNullable(System.getProperty("serverAddress")).orElseThrow(() -> new IllegalArgumentException("'serverAddress' argument needed."));
            String airlineName = Optional.ofNullable(System.getProperty("airline")).orElseThrow(() -> new IllegalArgumentException("'airline' argument needed."));
            String fc = Optional.ofNullable(System.getProperty("flightCode")).orElseThrow(() -> new IllegalArgumentException("'flightCode' argument needed."));
            int flightCode = Integer.parseInt(fc);

            logger.info("tpe1-g6 Flight Tracking Client Starting ...");

            String[] address = serverAddress.split(":");
            String host = address[0];
            String port = address[1];


            final Registry registry = LocateRegistry.getRegistry(host, Integer.parseInt(port));
            final TrackingService trackingService = (TrackingService) registry.lookup("tracking");
            final FlightEventCallback flightEventCallback = new FlightEventCallbackImpl();

            UnicastRemoteObject.exportObject(flightEventCallback, 0);

            logger.info("client Started ...");

            trackingService.register(flightCode, airlineName, flightEventCallback);

        }
        catch (Exception e){
            logger.error(e.toString());
        }

    }


}


