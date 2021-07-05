#!/usr/bin/env python

import sys 
import os
import numpy as np

queries = ["3path", "4path", "6path", "3star", "4star", "6star", "4cycle", "6cycle"]

for q in queries:
    ## For each query append all its times to a list
    times = []
    fp = open("outputs/" + q + ".out")
    ## Parse the file
    line = fp.readline()
    while line:
        if line.startswith(" Execution"):
            tokens = line.split()
            times.append(float(tokens[2]))
        line = fp.readline()
    fp.close()
    ## Compute the median
    median_runtime = np.median(times)
    print q + " : " + ('%.2f' % (median_runtime / 1000.0)) + " sec"