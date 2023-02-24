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

# algorithms = ["BatchSorting", "MLE", "MLM", "MLH", "MLL", "REA"]
# algorithms = ["MLE", "MLM", "MLH", "MLL", "REA"]

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
	#if (alg == "All" and l == 6 and n == 2000):
	#	lns += ax.plot([1.26, 3.1090], [1, 200], label=alg_label, marker = markers[i], markersize = markersizes[i], markevery=1)
	#else:
	mark_frequency = (len(k_list) - 1) / 5
	lns += ax.plot(times[alg], k_list, label=alg_label, marker = markers[i], markersize = markersizes[i], markevery = mark_frequency,
					linewidth=linewidths[i], fillstyle=fillstyles[i], alpha=alphas[i])
	# marker=marker_list[i], markersize=8


## Annotate TT(1) and avg delay
cmap = plt.get_cmap("tab10")
if title == "Synthetic Query SynQ1, l=2, n=2^16 TT(k)":
	# Annotate TT(1)
	sranges_tt1 = times["shared_ranges"][0]
	mpart_tt1 = times["multi_part"][0]
	bpart_tt1 = times["binary_part"][0]
	plt.gcf().text(0.09, 0.17, '%.2f' % sranges_tt1, size=18, color = cmap(2))
	plt.gcf().text(0.14, 0.07, '%.2f' % mpart_tt1, size=18, color = cmap(1))
	plt.gcf().text(0.23, 0.13, '%.2f' % bpart_tt1, size=18, color = cmap(0))
	# Compute the avg delay
	delay = {}
	for i in range(len(algorithms)):
		alg = algorithms[i]
		delay[alg] = (times[alg][-1] - times[alg][0]) / (k_list[-1] - 1)
	# Annotate avg delay
	props = dict(boxstyle='round', facecolor='grey', alpha=0.2)
	txt = r"$\overline{\mathrm{delay}}$" + "=\n" + '%.1f' % (delay["shared_ranges"] * 10**6) + "$\mu$sec"
	ax.text(0.6, 0.5, txt, transform=ax.transAxes, fontsize=18, verticalalignment='top', bbox=props, color = cmap(2))
	txt = r"$\overline{\mathrm{delay}}$" + "=\n" + '%.1f' % (delay["multi_part"] * 10**6) + "$\mu$sec"
	ax.text(0.18, 0.91, txt, transform=ax.transAxes, fontsize=18, verticalalignment='top', bbox=props, color = cmap(1))
	txt = r"$\overline{\mathrm{delay}}$" + "=\n" + '%.1f' % (delay["binary_part"] * 10**6) + "$\mu$sec"
	ax.text(0.23, 0.65, txt, transform=ax.transAxes, fontsize=18, verticalalignment='top', bbox=props, color = cmap(0))

#ax.set_xscale('log')
ax.grid()

plt.ticklabel_format(style='sci', axis='y', scilimits=(0,0))

cmap = plt.get_cmap("tab10")

ax.set(xlabel="Time (sec)", ylabel="#Results")
#plt.legend()
#plt.title(title)
plt.savefig(outFileName + ".pdf", format="pdf", bbox_inches="tight")
plt.savefig(outFileName + ".png", format="png", bbox_inches="tight")


## Legend
#plt.rc('text')#, usetex=True)  
#plt.rc('font', family='serif', size=20) 
#
#h, l = ax.get_legend_handles_labels()
#figlegend = plt.figure(figsize=(4 * len(algorithms), 0.5))
#ax_leg = figlegend.add_subplot(111)
#ax_leg.legend(h, l, loc='center', ncol=len(algorithms), fancybox=True, shadow=True, prop={'size':30}, markerscale=2)
#ax_leg.axis('off')
#figlegend.savefig("plots/legend2.pdf", format="pdf", bbox_inches="tight")