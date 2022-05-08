#!/bin/bash

# This controls the memory allocated to the Java VM. 
# Modify according to the RAM of the machine. 
MEM="-Xms100g -Xmx100g"

# The flags below control which queries will be run. Only the length-4 queries are required to replicate
# the figures of the VLDB20 paper
LENGTH4=true
LENGTH3=false
LENGTH6=false

# To run the experiments more quickly to see some preliminary results, simply uncomment the line below.
# This will run them for only few iterations and will finish in a couple of hours.
# Note that due to variance, the results will not necessarily be similar to those reported in the paper.

#QUICK=true

# These parameters change the number of iterations that each experiments is repeated.
# The settings below are expected to produce the same results that are reported in the paper.
ITERS_PATHS_ALL=200
ITERS_PATHS_FEW=700
ITERS_STARS_ALL=200
ITERS_STARS_FEW=500
ITERS_CYCLES_ALL=200
ITERS_CYCLES_FEW=2000
ITERS_PSQL_PATHS=200
ITERS_PSQL_STARS=200
ITERS_PSQL_CYCLES=200
ITERS_BITCOIN_PATH=400
ITERS_BITCOIN_STAR=400
ITERS_BITCOIN_CYCLE=400
ITERS_TWITTER_PATH=700
ITERS_TWITTER_STAR=400
ITERS_TWITTER_CYCLE=400

if [ "$QUICK" = true ] ; then
    ITERS_PATHS_ALL=5
    ITERS_PATHS_FEW=5
    ITERS_STARS_ALL=5
    ITERS_STARS_FEW=5
    ITERS_CYCLES_ALL=5
    ITERS_CYCLES_FEW=5
    ITERS_PSQL_PATHS=5
    ITERS_PSQL_STARS=5
    ITERS_PSQL_CYCLES=5
    ITERS_BITCOIN_PATH=5
    ITERS_BITCOIN_STAR=5
    ITERS_BITCOIN_CYCLE=5
    ITERS_TWITTER_PATH=5
    ITERS_TWITTER_STAR=5
    ITERS_TWITTER_CYCLE=5
fi
