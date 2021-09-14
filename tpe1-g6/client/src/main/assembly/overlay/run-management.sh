#!/bin/bash

#usage ./run-management -DserverAddress=xx.xx.xx.xx:yyyy -Daction=actionName [ -Drunway=runwayName | -Dcategory=minCategory ]
java  $* -cp 'lib/jars/*' "ar.edu.itba.pod.client.AdminClient"

