#!/bin/bash

# default argument values
DEFAULT_THRESHOLD=10;
DEFAULT_TTL_MILLIS=60000

# check if args exist, if one at least one lacks, run with different proposed values
if [ ! -n "$1" ] | [ ! -n "$2" ]
then
  echo "Running with default configuration: (Threshold = $DEFAULT_THRESHOLD, millisecondLimit = $DEFAULT_TTL_MILLIS)"
  java -jar ./target/RateLimitService-0.0.1-SNAPSHOT.jar $DEFAULT_THRESHOLD $DEFAULT_TTL_MILLIS
else
   echo "Run configuration: (Threshold = $1, millisecondLimit = $2)"
  java -jar ./target/RateLimitService-0.0.1-SNAPSHOT.jar $1 $2
fi
