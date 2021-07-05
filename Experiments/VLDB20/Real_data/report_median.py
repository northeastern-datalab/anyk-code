#!/usr/bin/env python

import sys 
import os
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt

# For debugging
def print_list(times, k_list):
	for i in range(len(k_list)):
		print "k = " + str(k_list[i]) + " : " + str(times[i])

## -- Read input
import argparse
parser = argparse.ArgumentParser(description='Plotting script')

parser.add_argument('-i', action="store", dest="inFileName", default="out", help="Name of input file")
parser.add_argument('-k', action="store", dest="k_to_report", default="-1", help="TTK to report")

arg_results = parser.parse_args()
inFileName = arg_results.inFileName
k_to_report = int(arg_results.k_to_report)

# Read file
times = []
times_aux = []		# times_aux contains a list of lists of runtimes (one list for each k contains all the runtimes for that k)
k_list = []
max_k = 0
fp1 = open(inFileName)
line = fp1.readline()
while line:

    if line.startswith("k="):
        tokens = line.split()
        k = int(tokens[1])
        if (k == 1): index = 0	# The index tells us which position in the list corresponds to the k we read
        else: index += 1

        if (k > max_k): 
            max_k = k
            k_list.append(k)
            times_aux.append([])
        times_aux[index].append(float(tokens[3]))
    line = fp1.readline()
fp1.close()

# If some instances contained more data points than others, cut them off
instances_no = len(times_aux[0])
while (len(times_aux[-1]) < instances_no):
    times_aux = times_aux[:-1]
    k_list = k_list[:-1]

# Now build one list by taking the median
times = []
index = 0
for k in k_list:
    runtimes = times_aux[index]
    median_runtime = np.median(runtimes)
    times.append(median_runtime)
    index += 1

print ('%.2f' % times[k_to_report])
