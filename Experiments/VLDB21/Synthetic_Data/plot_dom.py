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
parser.add_argument('-d', nargs='+', dest="d_exp_list", default=[], help="list of d (domain) values as exponents of 2 (x-axis)")
parser.add_argument('-n', action="store", dest="n", help="n")
parser.add_argument('-l', action="store", dest="l", help="l")
parser.add_argument('-k', action='store', dest="k_to_plot", default="1", help="the value k for which we plot TT(k)")
parser.add_argument('-o', action="store", dest="outFileName", default="out", help="Name of output file")
parser.add_argument('-i', action="store", dest="inFileName", default="out", help="Name of input file")
parser.add_argument('-t', action="store", dest="title", default="", help="Title of figure")

arg_results = parser.parse_args()
algorithms = arg_results.alg_list
n = arg_results.n
l = arg_results.l
k = arg_results.k_to_plot
d_exp_list = arg_results.d_exp_list
d_list = [2**int(exp) for exp in d_exp_list]
inFileName = arg_results.inFileName
outFileName = arg_results.outFileName
title = arg_results.title

algorithm_labels = {}
algorithm_labels["shared_ranges"] = "Shared Ranges"
algorithm_labels["binary_part"] = "Binary Partitioning"
algorithm_labels["multi_part"] = "Multiway Partitioning"

# Initialize plot
plt.rcParams.update({'font.size': 19})
fig, ax = plt.subplots()

markers=['x', '^', '*', 'd', 'o', '', '']
markersizes=[11, 10, 14, 9, 11, 0, 0]
fillstyles=['full', 'full', 'full', 'full', 'none', 'full', 'full']
linewidths=[1.5, 1.5, 1.5, 1.5, 1.5, 2, 2]
alphas=[1, 1, 0.9, 1, 1, 1, 1]
lns = []

times = {}		# times is a dictionary that for each algorithm contains a list of runtimes (one for each d)
mems = {}		# mems is a dictionary that for each algorithm contains a list of memories (one for each d)

for i in range(len(algorithms)):
	alg = algorithms[i]
	times[alg] = []
	mems[alg] = []

	# Read file for time
	for dexp in d_exp_list:
		temp_list = []
		fp1 = open(inFileName + "_d2" + str(dexp) + "_" + alg + ".out")
		line = fp1.readline()
		while line:
			if line.startswith("k= " + k):
				tokens = line.split()
				temp_list.append(float(tokens[3]))
			line = fp1.readline()
		fp1.close()
		# Take the median
		times[alg].append(np.median(temp_list))

	# Read file for mem
	for dexp in d_exp_list:
		temp_list = []
		fp1 = open(inFileName + "_d2" + str(dexp) + "_" + alg + "_mem.out")
		line = fp1.readline()
		while line:
			if line.startswith("Graph_size ="):
				tokens = line.split()
				temp_list.append(float(tokens[2]))
			line = fp1.readline()
		fp1.close()
		# Take the median
		mems[alg].append(np.median(temp_list))

# Plot times
for i in range(len(algorithms)):
	alg = algorithms[i]
	alg_label = algorithm_labels[alg]
	ax.plot(d_list, times[alg], label=alg_label + " Time", marker = markers[i], markersize = markersizes[i])
plt.gca().set_prop_cycle(None)


ax.set(xlabel="|domain|", ylabel="TT($1$) sec")
ax.xaxis.label.set_size(20)
ax.set_xscale('log', basex=2)

ax.grid()


plt.savefig(outFileName + ".pdf", format="pdf", bbox_inches="tight")
plt.savefig(outFileName + ".png", format="png", bbox_inches="tight")


## Legend
#plt.rc('text')#, usetex=True)  
#plt.rc('font', family='serif', size=20) 
#
#h, l = ax.get_legend_handles_labels()
##h2, l2 = ax.get_legend_handles_labels()
#figlegend = plt.figure(figsize=(4 * len(algorithms), 0.5))
#ax_leg = figlegend.add_subplot(111)
#ax_leg.legend(h, l, loc='center', ncol=len(algorithms), fancybox=True, shadow=True, prop={'size':30}, markerscale=2)
#ax_leg.axis('off')
#figlegend.savefig("plots/legend_dom.pdf", format="pdf", bbox_inches="tight")