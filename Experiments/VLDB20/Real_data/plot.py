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

parser.add_argument('-a', nargs='+', dest="alg_list", default=[], help="list of algorithms")
parser.add_argument('-o', action="store", dest="outFileName", default="out", help="Name of output file")
parser.add_argument('-i', action="store", dest="inFileName", default="out", help="Name of input file")
parser.add_argument('-t', action="store", dest="title", default="", help="Title of figure")
parser.add_argument('-l', action="store", dest="l", help="l")
parser.add_argument('-c', action="store", dest="cutoff", default=sys.maxint, help="Use to stop plotting after some k")


arg_results = parser.parse_args()
algorithms = arg_results.alg_list
inFileName = arg_results.inFileName
outFileName = arg_results.outFileName
title = arg_results.title
l = int(arg_results.l)
cutoff = int(arg_results.cutoff)

algorithm_labels = {}
algorithm_labels["Batch"] = "Batch(No sort)"
algorithm_labels["BatchSorting"] = "Batch"
algorithm_labels["Eager"] = "Eager"
algorithm_labels["All"] = "All"
algorithm_labels["Take2"] = "Take2"
algorithm_labels["Lazy"] = "Lazy"
algorithm_labels["Recursive"] = "Recursive"
algorithm_labels["NPRR"] = "Batch"

linestyles = {}
for alg in algorithms:
	linestyles[alg] = 'solid'
linestyles["Recursive"] = (0, (5, 1))
linestyles["Batch"] = 'dashdot'
linestyles["BatchSorting"] = 'dashdot'

# Initialize plot
# marker_list = ["1", "2", "3", "x"]
plt.rcParams.update({'font.size': 17})
fig, ax = plt.subplots()

markers=['x', '^', '*', 'd', 'o', '', '']
markersizes=[11, 10, 14, 9, 11, 0, 0]
fillstyles=['full', 'full', 'full', 'full', 'none', 'full', 'full']
linewidths=[1.5, 1.5, 1.5, 1.5, 1.5, 2, 2]
alphas=[1, 1, 0.9, 1, 1, 1, 1]
lns = []
times = {}	# times[alg] contains a list of runtimes (one for each k)

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

	# Print the number of instances (only once)
	if (i == 0):
		print str(instances_no) + " instances of " + title

	# Now build one list by taking the median
	times[alg] = []
	index = 0
	for k in k_list:
		runtimes = times_aux[index]
		median_runtime = np.median(runtimes)
		times[alg].append(median_runtime)
		index += 1

	# Plot	
	alg_label = algorithm_labels[alg]
	mark_frequency = (len(k_list) - 1) / 5
	lns += ax.plot(times[alg], k_list, label=alg_label, marker = markers[i], markersize = markersizes[i], markevery = mark_frequency,
					linestyle = linestyles[alg], linewidth=linewidths[i], fillstyle=fillstyles[i], alpha=alphas[i])	

#ax.set_xscale('log')
ax.grid()

plt.ticklabel_format(style='sci', axis='y', scilimits=(0,0))

if ("Twitter" in title and "Star" in title and l == 4):
	plt.xlim(left=0.9 * min([times[alg][0] for alg in algorithms]), right=times["Eager"][-1] * 1.2)
if ("Twitter" in title and "Cycle" in title and l == 6):
	plt.xlim(left=0.9 * min([times[alg][0] for alg in algorithms]), right=times["Eager"][-1] * 1.05)
if ("Twitter" in title and "Star" in title and l == 6):
	plt.xlim(left=0.9 * min([times[alg][0] for alg in algorithms]), right=times["Eager"][-1] * 1.2)

ax.set(xlabel="Time (sec)", ylabel="#Results")
#plt.legend()
#plt.title(title)
plt.savefig(outFileName + ".pdf", format="pdf", bbox_inches="tight")
plt.savefig(outFileName + ".png", format="png", bbox_inches="tight")