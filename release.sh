#!/bin/bash
gradle clean -Dprofile=releaseVersion && gradle releaseVersion -Dprofile=releaseVersion
