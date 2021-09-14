#!/bin/bash

#usage ./run-airline -DserverAddress=xx.xx.xx.xx:yyyy -Dairline=airlineName -DflightCode=flightCode
java  $* -cp 'lib/jars/*' "ar.edu.itba.pod.client.FlightTrackingClient"

