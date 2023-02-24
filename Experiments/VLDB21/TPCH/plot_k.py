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
parser.add_argument('-n', action="store", dest="n", help="n")
parser.add_argument('-l', action="store", dest="l", help="l")
parser.add_argument('-d', action="store", dest="d", default="-1", help="d")
parser.add_argument('-c', action="store", dest="cutoff", default=sys.maxint, help="Use to stop plotting after some k")


arg_results = parser.parse_args()
algorithms = arg_results.alg_list
inFileName = arg_results.inFileName
outFileName = arg_results.outFileName
title = arg_results.title
cutoff = int(arg_results.cutoff)

algorithm_labels = {}
algorithm_labels["Lazy"] = r"$Q_{T1}$"
algorithm_labels["UnrankedEnum"] = r"$Q_{T1}$ Unranked"
algorithm_labels["Disjunction"] = r"$Q_{T1D}$"

# Initialize plot
plt.rcParams.update({'font.size': 19})
fig, ax = plt.subplots()
# Change color map to sequential
colors = [plt.cm.Blues(i) for i in np.linspace(0.5, 1.0, len(algorithms))]
ax.set_prop_cycle('color', colors)

markers=['x', 'x', 'x']
markersizes=[11, 11, 11]
fillstyles=['full', 'full', 'full', 'full', 'none', 'full', 'full']
linewidths=[1.5, 1.5, 1.5, 1.5, 1.5, 2, 2]
linestyles=['-', '--', '-.']
alphas=[1, 1, 1, 1, 1, 1, 1]
lns = []
times = {}	# times[alg] contains a list of runtimes (one for each k)

for i in range(len(algorithms)):
	alg = algorithms[i]
	times_aux = []		# times_aux contains a list of lists of runtimes (one list for each k contains all the runtimes for that k)
	k_list = []
	max_k = 0
	duplicates_list = []
	
	# Read file
	if alg == "Disjunction":
		tokens = inFileName.split("_")
		tokens[0] = tokens[0] + "D"
		fp1 = open('_'.join(tokens) + "_" + algorithms[0] + ".out")
	else:
		fp1 = open(inFileName + "_" + alg + ".out")
	line = fp1.readline()
	while line:

		if line.startswith("Duplicates filtered"):
			tokens = line.split()
			duplicates_list.append(int(tokens[3]))

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
    
	lns += ax.plot(times[alg], k_list, label=alg_label, linewidth=linewidths[i], linestyle=linestyles[i], \
        marker = markers[i], markersize = markersizes[i], markevery = mark_frequency,fillstyle=fillstyles[i], alpha=alphas[i])

# Report the number of duplicates for the queries with disjunction
if duplicates_list:
	props = dict(boxstyle='round', facecolor='grey', alpha=0.2)
	txt = "Duplicates\nFiltered:\n" + '%.2f' % (np.median(duplicates_list) / 10**6) + r"$\times 10^6$"
	ax.text(0.5, 0.95, txt, transform=ax.transAxes, fontsize=18, verticalalignment='top', bbox=props, color = colors[2])

# Add annotations for the queries
txt = r"$Q_{T}$"
ax.text(0.23, 0.9, txt, transform=ax.transAxes, fontsize=25, verticalalignment='top', color = colors[0])
if "msf=32" in title:
	txt = r"$Q_{T}^U$"
	ax.text(0.1, 0.9, txt, transform=ax.transAxes, fontsize=25, verticalalignment='top', color = colors[1])
	txt = r"$Q_{TD}$"
	ax.text(0.8, 0.7, txt, transform=ax.transAxes, fontsize=25, verticalalignment='top', color = colors[2])
else:
	txt = r"$Q_{T}^U$"
	ax.text(0.08, 0.9, txt, transform=ax.transAxes, fontsize=25, verticalalignment='top', color = colors[1])
	txt = r"$Q_{TD}$"
	ax.text(0.86, 0.7, txt, transform=ax.transAxes, fontsize=25, verticalalignment='top', color = colors[2])


#ax.set_xscale('log')
ax.grid()

plt.ticklabel_format(style='sci', axis='y', scilimits=(0,0))

cmap = plt.get_cmap("tab10")

ax.set(xlabel="Time (sec)", ylabel="#Results")

plt.savefig(outFileName + ".pdf", format="pdf", bbox_inches="tight")
plt.savefig(outFileName + ".png", format="png", bbox_inches="tight")

