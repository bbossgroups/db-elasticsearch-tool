#!/bin/sh
java -jar ${project}-${bboss_version}.jar stop --conf=resources/application.properties  --shutdownLevel=C
