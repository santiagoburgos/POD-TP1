#!/bin/bash

#usage ./run-query -DserverAddress=xx.xx.xx.xx:yyyy [ -Dairline=airlineName | -Drunway=runwayName ] -DoutPath=fileName
java "$@" -cp 'lib/jars/*' "ar.edu.itba.pod.client.RunQueryClient"