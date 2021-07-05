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
n = int(arg_results.n)
l = int(arg_results.l)
d = int(arg_results.d)
cutoff = int(arg_results.cutoff)

# algorithms = ["BatchSorting", "MLE", "MLM", "MLH", "MLL", "REA"]
# algorithms = ["MLE", "MLM", "MLH", "MLL", "REA"]

algorithm_labels = {}
algorithm_labels["Batch"] = "Batch(No sort)"
algorithm_labels["BatchSorting"] = "Batch"
algorithm_labels["Eager"] = "Eager"
algorithm_labels["All"] = "All"
algorithm_labels["Take2"] = "Take2"
algorithm_labels["Lazy"] = "Lazy"
algorithm_labels["Recursive"] = "Recursive"
algorithm_labels["NPRR"] = "Batch(No sort)"
algorithm_labels["NPRR_Sort"] = "Batch"

linestyles = {}
for alg in algorithms:
	linestyles[alg] = 'solid'
linestyles["Recursive"] = (0, (5, 1))
linestyles["Batch"] = 'dashdot'
linestyles["BatchSorting"] = 'dashdot'
linestyles["NPRR"] = 'dashdot'
linestyles["NPRR_Sort"] = 'dashdot'

# Initialize plot
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
	if not (alg == "All" and l == 6 and n == 2000):			
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
	# marker=marker_list[i], markersize=8

#ax.set_xscale('log')
ax.grid()

plt.ticklabel_format(style='sci', axis='y', scilimits=(0,0))

cmap = plt.get_cmap("tab10")

## Annotate TTF-TTL for some of the plots that show all results
## 4-Path
if (n == 10000 and l == 4 and title.startswith("Path")):
	last_k = k_list[-1]
	first_k = k_list[0]
	batch_first = times["BatchSorting"][0]
	batch_last = times["BatchSorting"][-1]
	rea_last = times["Recursive"][-1]
	batch_nosort_first = times["Batch"][0]
	batch_nosort_last = times["Batch"][-1]
	ax.annotate(('%.1f' % batch_last),xy = (batch_last, last_k), size=15, color = cmap(5))
	ax.annotate(('%.1f' % batch_first),xy = (batch_first, first_k), size=15, color = cmap(5))
	ax.annotate(('%.1f' % rea_last),xy = (rea_last, last_k), size=15, color = cmap(0), textcoords="offset points", xytext=(-25,0))
	ax.annotate(('%.1f' % batch_nosort_last),xy = (batch_nosort_last, last_k), size=15, color = cmap(6))
	ax.annotate(('%.1f' % batch_nosort_first),xy = (batch_nosort_first, first_k), size=15, color = cmap(6))

## 6-Path
if (n == 100 and l == 6 and title.startswith("Path")):
	last_k = k_list[-1]
	first_k = k_list[0]
	batch_first = times["BatchSorting"][0]
	batch_last = times["BatchSorting"][-1]
	rea_last = times["Recursive"][-1]
	batch_nosort_first = times["Batch"][0]
	batch_nosort_last = times["Batch"][-1]
	
	ax.annotate(('%.1f' % batch_last),xy = (batch_last, last_k), size=15, color = cmap(5))
	ax.annotate(('%.1f' % batch_first),xy = (batch_first, first_k), size=15, color = cmap(5))
	ax.annotate(('%.1f' % rea_last),xy = (rea_last, last_k), size=15, color = cmap(0))

	ax.annotate(('%.1f' % batch_nosort_last),xy = (batch_nosort_last, last_k), size=15, color = cmap(6))
	ax.annotate(('%.1f' % batch_nosort_first),xy = (batch_nosort_first, first_k), size=15, color = cmap(6))

if (n == 5000 and l == 4):
	last_k = k_list[-1]
	first_k = k_list[0]
	batch_first = times["NPRR_Sort"][0]
	batch_last = times["NPRR_Sort"][-1]
	rea_last = times["Recursive"][-1]
	
	ax.annotate(('%.1f' % batch_last),xy = (batch_last, last_k), size=15, color = cmap(5))
	ax.annotate(('%.1f' % batch_first),xy = (batch_first, first_k), size=15, color = cmap(5))
	ax.annotate(('%.1f' % rea_last),xy = (rea_last, last_k), size=15, color = cmap(0), textcoords="offset points", xytext=(-25,0))

if (n == 2000 and l == 6):
	plt.xlim(1.2, 2.1)

if ("Cycle" in title and l == 4 and n == 100000):
	plt.xlim(1.12, 1.4)
if ("Cycle" in title and l == 6 and n == 100000):
	plt.xlim(2.3, 3.0)

ax.set(xlabel="Time (sec)", ylabel="#Results")
#plt.legend()
#plt.title(title)
plt.savefig(outFileName + ".pdf", format="pdf", bbox_inches="tight")
plt.savefig(outFileName + ".png", format="png", bbox_inches="tight")


## Legend
#plt.rc('text', usetex=True)  
plt.rc('font', family='serif', size=20) 

h, l = ax.get_legend_handles_labels()
figlegend = plt.figure(figsize=(4 * len(algorithms), 0.5))
ax_leg = figlegend.add_subplot(111)
ax_leg.legend(h, l, loc='center', ncol=len(algorithms), fancybox=True, shadow=True, prop={'size':30}, markerscale=2)
ax_leg.axis('off')
figlegend.savefig("plots/legend.pdf", format="pdf", bbox_inches="tight")