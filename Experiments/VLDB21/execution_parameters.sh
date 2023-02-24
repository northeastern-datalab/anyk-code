#!/bin/bash

# This controls the memory allocated to the Java VM. 
# Modify according to the RAM of the machine. 
MEM="-Xms100g -Xmx100g"

OTHER_OPTS=""

# To run the experiments more quickly to see some preliminary results, simply uncomment the line below.
# This will run them for only few iterations and will finish in a couple of hours.
# Note that due to variance, the results will not necessarily be similar to those reported in the paper.

# QUICK=true

# These parameters change the number of iterations that each experiments is repeated.
# The settings below are expected to produce the same results that are reported in the paper.
ITERS=5
ITERS_MANY=10

if [ "$QUICK" = true ] ; then
    ITERS=1
    ITERS_MANY=1
fi
