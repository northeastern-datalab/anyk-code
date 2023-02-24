#!/usr/bin/env python

import sys 
import os
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.patches import Ellipse, Polygon

## -- Read input
import argparse
parser = argparse.ArgumentParser(description='Plotting script')

parser.add_argument('-a', nargs='+', dest="alg_list", default=[], help="list of algorithms")
parser.add_argument('-o', action="store", dest="outFileName", default="out", help="Name of output file")
parser.add_argument('-i', action="store", dest="inFileName", default="out", help="Name of input file")
parser.add_argument('-t', action="store", dest="title", default="", help="Title of figure")

arg_results = parser.parse_args()
algorithms = arg_results.alg_list
inFileName = arg_results.inFileName
outFileName = arg_results.outFileName
title = arg_results.title

algorithm_labels = {}
algorithm_labels["shared_ranges"] = "Shared Ranges"
algorithm_labels["binary_part"] = "Binary Partitioning"
algorithm_labels["multi_part"] = "Multiway Partitioning"
algorithm_labels["shared_ranges_nolazy"] = "Shared Ranges"
algorithm_labels["binary_part_nolazy"] = "Binary Partitioning"
algorithm_labels["multi_part_nolazy"] = "Multiway Partitioning"

#linestyles["Recursive"] = (0, (5, 1))
#linestyles["Batch"] = 'dashdot'
#linestyles["BatchSorting"] = 'dashdot'

hatches = ['', '/', '\\', 'x', '--']

# Initialize plot
# marker_list = ["1", "2", "3", "x"]
plt.rcParams.update({'font.size': 19})
fig, ax = plt.subplots()


times = {}	# times[alg] contains a list of runtimes (one for each k)

#k_list = [1, 1000, 1000000]
k_list = [1, 10000, 20000, 30000]

for i in range(len(algorithms)):
	alg = algorithms[i]

	times_aux = []		# times_aux contains a list of lists of runtimes (one list for each k contains all the runtimes for that k)

	times_aux.append([]) 	# k = 1
	times_aux.append([]) 	# k = 10000
	times_aux.append([])	# k = 20000
	times_aux.append([])	# k = 30000

	# Read file
	try:
		fp1 = open(inFileName + "_" + alg + ".out")
	except IOError:
		continue
	
	line = fp1.readline()
	while line:

		if line.startswith("k="):
			tokens = line.split()
			k = int(tokens[1])
			if k in k_list:
				k_idx = k_list.index(k)
				times_aux[k_idx].append(float(tokens[3]))

		line = fp1.readline()
	fp1.close()

	# Print the number of instances (only once)
	if (i == 0):
		print str(len(times_aux[0])) + " instances of " + title

	# Now build one list by taking the median
	if len(times_aux[0]):
		times[alg] = []
		for j in range(len(k_list)):
			runtimes = times_aux[j]
			median_runtime = np.median(runtimes)
			times[alg].append(median_runtime)

#  =====   Plot	  =====
# set width of bar
barWidth = 0.7
# Set position of bar on X axis
x_axis = np.arange(3)
# Make the plot
cmap = plt.get_cmap("tab10")
for i in range(len(algorithms)):
	alg = algorithms[i]
	alg_label = algorithm_labels[alg]
	if (alg in times):
		lw = 0.5
		plt.bar([x_axis[i]], times[alg][0], width=barWidth, color=cmap(i), edgecolor='black', label=alg_label, hatch=hatches[0], alpha=0.8, linewidth = lw)
		plt.bar([x_axis[i]], times[alg][1] - times[alg][0], bottom=times[alg][0], width=barWidth, color=cmap(i), edgecolor='black', label=alg_label, hatch=hatches[1], alpha=0.8, linewidth = lw)
		plt.bar([x_axis[i]], times[alg][2] - times[alg][1], bottom=times[alg][1], width=barWidth, color=cmap(i), edgecolor='black', label=alg_label, hatch=hatches[2], alpha=0.8, linewidth = lw)
		plt.bar([x_axis[i]], times[alg][3] - times[alg][2], bottom=times[alg][2], width=barWidth, color=cmap(i), edgecolor='black', label=alg_label, hatch=hatches[1], alpha=0.8, linewidth = lw)

# Add xticks on the middle of the group bars
label_list = [algorithm_labels[algorithms[i]].replace(" ", "\n") for i in range(len(algorithms))]
plt.xticks(x_axis, label_list)
#plt.xlim(None, len(algorithms) - 1 - barWidth)

#ax.set_xscale('log')
ax.grid(axis='y')

#plt.ticklabel_format(style='sci', axis='y', scilimits=(0,0))
#ax.set_yscale('log', basey=10)
ax.set_ylim(bottom=0.3)


ax.set(ylabel="TT(k) sec")
ax.xaxis.label.set_size(18)
#plt.legend()
#plt.title(title)
plt.savefig(outFileName + ".pdf", format="pdf", bbox_inches="tight")
plt.savefig(outFileName + ".png", format="png", bbox_inches="tight")