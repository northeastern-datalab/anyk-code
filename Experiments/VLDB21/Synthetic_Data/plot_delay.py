#!/usr/bin/env python

import sys 
import os
import math
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt

## -- Read input
import argparse
parser = argparse.ArgumentParser(description='Plotting script')

parser.add_argument('-a', nargs='+', dest="alg_list", default=[], help="list of algorithms")
parser.add_argument('-o', action="store", dest="outFileName", default="out", help="Name of output file")
parser.add_argument('-i', action="store", dest="inFileName", default="out", help="Name of input file")
parser.add_argument('-t', action="store", dest="title", default="", help="Title of figure")
parser.add_argument('-c', action="store", dest="cutoff", default=sys.maxint, help="Use to stop plotting after some k")


arg_results = parser.parse_args()
algorithms = arg_results.alg_list
inFileName = arg_results.inFileName
outFileName = arg_results.outFileName
title = arg_results.title
cutoff = int(arg_results.cutoff)


algorithm_labels = {}
algorithm_labels["shared_ranges"] = "Shared Ranges"
algorithm_labels["binary_part"] = "Binary Partitioning"
algorithm_labels["multi_part"] = "Multiway Partitioning"
algorithm_labels["shared_ranges_nolazy"] = "Shared Ranges"
algorithm_labels["binary_part_nolazy"] = "Binary Partitioning"
algorithm_labels["multi_part_nolazy"] = "Multiway Partitioning"


# Initialize plot
plt.rcParams.update({'font.size': 19})
fig, ax = plt.subplots()

markers=['x', '^', '*', 'd', 'o', '', '']
markersizes=[11, 10, 14, 9, 11, 0, 0]
fillstyles=['full', 'full', 'full', 'full', 'none', 'full', 'full']
linewidths=[1.5, 1.5, 1.5, 1.5, 1.5, 2, 2]
alphas=[1, 1, 0.9, 1, 1, 1, 1]
lns = []
times = {}	# times[alg] contains a list of runtimes (one for each k)
delays = {}

for i in range(len(algorithms)):
	alg = algorithms[i]
	times_aux = []		# times_aux contains a list of lists of runtimes (one list for each k contains all the runtimes for that k)
	k_list = []
	max_k = 0

	# Read file
	fp1 = open(inFileName + "_" + alg + ".out")
	line = fp1.readline()
	while line:

		if line.startswith("k="):
			tokens = line.split()
			k = int(tokens[1])
			if (k == 1): index = 0	# The index tells us which position in the list corresponds to the k we read
			else: index += 1
			if (k < cutoff):

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
	times[alg] = []
	index = 0
	for k in k_list:
		runtimes = times_aux[index]
		median_runtime = np.median(runtimes)
		times[alg].append(median_runtime)
		index += 1

	# Compute the delay instead of the TTK
	delays[alg] = []
	window = k_list[1] - k_list[0]
	prev = times[alg][0]
	for ttk in times[alg][1:]:
		delays[alg].append((ttk - prev) * 1.0 / window)
		prev = ttk


	# Plot
	alg_label = algorithm_labels[alg]

	mark_frequency = (len(k_list[1:])) / 7
	lns += ax.plot(k_list[1:], delays[alg], label=alg_label, marker = markers[i], markersize = markersizes[i], markevery = mark_frequency,
					linewidth=linewidths[i], fillstyle=fillstyles[i], alpha=alphas[i])

ax.set_yscale('log')
ax.grid()

plt.ticklabel_format(style='sci', axis='x', scilimits=(0,0))

cmap = plt.get_cmap("tab10")

ax.set(xlabel="Result No.", ylabel="Delay (sec)")
#plt.legend()
#plt.title(title)
plt.savefig(outFileName + ".pdf", format="pdf", bbox_inches="tight")
plt.savefig(outFileName + ".png", format="png", bbox_inches="tight")
