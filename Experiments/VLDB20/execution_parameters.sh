#!/bin/bash

# This controls the memory allocated to the Java VM. 
# Modify according to the RAM of the machine. 
MEM="-Xms100g -Xmx100g"

# These parameters change the number of iterations that each experiments is repeated.
# The settings below are expected to produce the same results that are reported in the paper.
#ITERS_PATHS_ALL=200
#ITERS_PATHS_FEW=700
#ITERS_STARS_ALL=200
#ITERS_STARS_FEW=500
#ITERS_CYCLES_ALL=200
#ITERS_CYCLES_FEW=2000
#ITERS_PSQL_PATHS=200
#ITERS_PSQL_STARS=200
#ITERS_PSQL_CYCLES=200
#ITERS_BITCOIN_PATH=400
#ITERS_BITCOIN_STAR=400
#ITERS_BITCOIN_CYCLE=400
#ITERS_TWITTER_PATH=400
#ITERS_TWITTER_STAR=400
#ITERS_TWITTER_CYCLE=400

# To run the experiments more quickly to see some preliminary results, uncomment the lines below.
# These are expected to finish overnight.
# Note that due to variance, the results will not necessarily be similar to those reported in the paper.
ITERS_PATHS_ALL=3
ITERS_PATHS_FEW=3
ITERS_STARS_ALL=3
ITERS_STARS_FEW=3
ITERS_CYCLES_ALL=3
ITERS_CYCLES_FEW=3
ITERS_PSQL_PATHS=3
ITERS_PSQL_STARS=3
ITERS_PSQL_CYCLES=3
ITERS_BITCOIN_PATH=3
ITERS_BITCOIN_STAR=3
ITERS_BITCOIN_CYCLE=3
ITERS_TWITTER_PATH=3
ITERS_TWITTER_STAR=3
ITERS_TWITTER_CYCLE=3