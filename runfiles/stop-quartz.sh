#!/bin/sh
java -jar ${project}-${bboss_version}.jar stop --conf=config-quartztask.properties  --shutdownLevel=C
