#!/usr/bin/env python

import sys 
import os
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt

## -- Read input
import argparse
parser = argparse.ArgumentParser(description='Plotting script')

parser.add_argument('-a', nargs='+', dest="alg_list", default=[], help="list of algorithms")
parser.add_argument('-n', nargs='+', dest="n_exp_list", default=[], help="list of n values as exponents of 2 (x-axis)")
parser.add_argument('-k', action='store', dest="k_to_plot", default=[], help="the value k for which we plot TT(k)")
parser.add_argument('-o', action="store", dest="outFileName", default="out", help="Name of output file")
parser.add_argument('-i', action="store", dest="inFileName", default="out", help="Name of input file")
parser.add_argument('-t', action="store", dest="title", default="", help="Title of figure")


arg_results = parser.parse_args()
algorithms = arg_results.alg_list
inFileName = arg_results.inFileName
outFileName = arg_results.outFileName
title = arg_results.title
n_exp_list = arg_results.n_exp_list
n_list = [2**int(exp) for exp in n_exp_list]
k_to_plot = int(arg_results.k_to_plot)

algorithm_labels = {}
algorithm_labels["shared_ranges"] = "Shared Ranges"
algorithm_labels["binary_part"] = "Binary Partitioning"
algorithm_labels["multi_part"] = "Multiway Partitioning"

# Initialize plot
# marker_list = ["1", "2", "3", "x"]
plt.rcParams.update({'font.size': 19})
fig, ax = plt.subplots()

markers=['x', '^', '*', 'd', 'o', '', '']
markersizes=[11, 10, 14, 9, 11, 0, 0]
fillstyles=['full', 'full', 'full', 'full', 'none', 'full', 'full']
linewidths=[1.5, 1.5, 1.5, 1.5, 1.5, 2, 2]
alphas=[1, 1, 0.9, 1, 1, 1, 1]
lns = []

mems = {}

for i in range(len(algorithms)):
	alg = algorithms[i]
	mems[alg] = []
	for n_exp in n_exp_list:
		n = 2**int(n_exp)
		temp_list = []
		# Read file
		fp1 = open(inFileName + "_n2" + n_exp + "_" + alg + "_mem.out")
		line = fp1.readline()
		while line:
			if line.startswith("Graph_size ="):
				tokens = line.split()
				temp_list.append(float(tokens[2]))
			line = fp1.readline()
		fp1.close()
		# Take the median
		mems[alg].append(np.median(temp_list))

# Plot the algorithms
for i in range(len(algorithms)):
	alg = algorithms[i]
	alg_label = algorithm_labels[alg]
	ax.plot(n_list, mems[alg], label=alg_label, marker = markers[i], markersize = markersizes[i])

#plt.legend()

ax.set_yscale('log', basey=10)
ax.set_xscale('log', basex=2)


ax.set(xlabel="n", ylabel="Representation size")
ax.xaxis.label.set_size(20)


#plt.title(title)
ax.grid()


plt.savefig(outFileName + ".pdf", format="pdf", bbox_inches="tight")
plt.savefig(outFileName + ".png", format="png", bbox_inches="tight")
