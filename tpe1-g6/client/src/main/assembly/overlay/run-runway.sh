#!/bin/bash

#usage ./run-runway -DserverAddress=xx.xx.xx.xx:yyyy -DinPath=fileName
java -cp 'lib/jars/*' "ar.edu.itba.pod.client.RunRunwayClient" $*

