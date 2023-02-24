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
parser.add_argument('-l', nargs='+', dest="l_list", default=[], help="list of l values (x-axis)")
parser.add_argument('-k', action='store', dest="k_to_plot", default=[], help="the value k for which we plot TT(k)")
parser.add_argument('-o', action="store", dest="outFileName", default="out", help="Name of output file")
parser.add_argument('-i', action="store", dest="inFileName", default="out", help="Name of input file")
parser.add_argument('-t', action="store", dest="title", default="", help="Title of figure")


arg_results = parser.parse_args()
algorithms = arg_results.alg_list
inFileName = arg_results.inFileName
outFileName = arg_results.outFileName
title = arg_results.title
l_list = [int(l) for l in arg_results.l_list]
k_to_plot = int(arg_results.k_to_plot)

algorithm_labels = {}
algorithm_labels["Batch"] = "Batch(No sort)"
algorithm_labels["BatchSorting"] = "Batch(Sort) Lower Bound"
algorithm_labels["BatchHeap"] = "Batch(Heap) Lower Bound"
algorithm_labels["QEq_Lazy"] = "QuadEqui Lower Bound"
algorithm_labels["Lazy"] = "Factorized Any-k"
algorithm_labels["psql"] = "PSQL"
algorithm_labels["sysx"] = "System X"

# Initialize plot
# marker_list = ["1", "2", "3", "x"]
plt.rcParams.update({'font.size': 19})
fig, ax = plt.subplots()

markers=['x', '^', '*', 'd', 'o', '', '']
markersizes=[11, 10, 14, 9, 13, 0, 0]
fillstyles=['full', 'full', 'full', 'full', 'none', 'full', 'full']
linewidths=[1.5, 1.5, 1.5, 1.5, 1.5, 2, 2]
alphas=[1, 1, 0.9, 1, 1, 1, 1]

times = {}			# times is a dictionary that for each algorithm contains a list of runtimes (one for each n)
times_std = {}		# times is a dictionary that for each algorithm contains a list of runtimes (one for each n)

for i in range(len(algorithms)):
	alg = algorithms[i]
	times[alg] = []
	times_std[alg] = []

	for l in l_list:
		times_aux = []		# times_aux contains a list runtimes for a given n (one for each repetition)
		prev_k = -1			# records the last k we read so that we can find the maximal k for each repetition


		if (alg.startswith("QEq_")) and l == 2:
			## QuadEqui is the same as Batch for binary joins
			times[alg].append(None)
			times_std[alg].append(None)
			continue
		if (alg == "psql"):
			# Read file
			try:
				fp1 = open(inFileName + "_l" + str(l) + "_k" + str(k_to_plot) + "_" + alg + ".out")
			except IOError:
				times[alg].append(None)
				times_std[alg].append(None)
				continue

			line = fp1.readline()
			while line:

				if line.startswith(" Execution time"):
					tokens = line.split()
					runtime = float(tokens[2]) / 1000.0
					times_aux.append(runtime)

				line = fp1.readline()
			fp1.close()
		elif (alg == "sysx"):
			# Read file
			try:
				fp1 = open(inFileName + "_l" + str(l) + "_k" + str(k_to_plot) + "_" + alg + ".out")
			except IOError:
				times[alg].append(None)
				times_std[alg].append(None)
				continue

			line = fp1.readline()
			while line:

				if line.startswith("   CPU time ="):
					tokens = line.split()
					runtime = float(tokens[8]) / 1000.0
					times_aux.append(runtime)

				line = fp1.readline()
			fp1.close()
		else:
			# Read file
			try:
				fp1 = open(inFileName + "_l" + str(l) + "_" + alg + ".out")
			except IOError:
				times[alg].append(None)
				times_std[alg].append(None)
				continue

			line = fp1.readline()
			while line:

				if line.startswith("k="):
					tokens = line.split()
					k = int(tokens[1])
					runtime = float(tokens[3])
					if (k == k_to_plot):
						times_aux.append(runtime)

					prev_runtime = runtime

				line = fp1.readline()
			fp1.close()

		# We now have a list of runtimes for l gathered in times_aux
		# Take the median and append to times
		if len(times_aux):
			median_runtime = np.median(times_aux)
			std = np.std(times_aux)
			times[alg].append(median_runtime)
			times_std[alg].append(std)
		else:
			times[alg].append(None)
			times_std[alg].append(None)

# Find the result count for each l length
counts = []
for l in l_list:
	counts_aux = [] 	# holds one value per instance
	# Read file
	fp1 = open(inFileName + "_l" + str(l) + "_Count.out")
	line = fp1.readline()
	while line:
		if line.startswith("Number_of_Results"):
			tokens = line.split()
			counts_aux.append(long(tokens[2]))
			break
		line = fp1.readline()
	fp1.close()
	counts.append(np.median(counts_aux))

# Plot the algorithms
for i in range(len(algorithms)):
	alg = algorithms[i]
	alg_label = algorithm_labels[alg]
	ax.plot(l_list, times[alg], label=alg_label, marker = markers[i], markersize = markersizes[i], fillstyle = fillstyles[i])
	#ax.errorbar(n_list, times[alg], yerr=times_std[alg], label=alg_label, marker = markers[i], markersize = markersizes[i], capsize=5, capthick=1)
	# marker=marker_list[i], markersize=8

#plt.legend()


## ------------ Then the result counts ------------------
ax2 = ax.twinx()  # instantiate a second axes that shares the same x-axis
#suf_color = "rebeccapurple"
suf_color = (0.36, 0.38, 0.18, 1)
ax2.set_ylabel("Total Output size", color=suf_color)
ax2.yaxis.label.set_color(suf_color)
ax2.tick_params(axis='y', colors=suf_color)
ax2.plot(l_list, counts, linestyle="dashdot", color=suf_color)
ax2.set_yscale('log', basey=10)
#ax2.ticklabel_format(axis='y', style='sci', scilimits=(0,0))
#ax2.get_yaxis().get_offset_text().set_position((1.06,1))

## Add text for OOM exceptions
cmap = plt.get_cmap("tab10")
for i in range(len(algorithms)):
	alg = algorithms[i]
	# Find the index where we first get a None if it exists
	found_None = False
	for idx in range(len(times[alg])):
		if times[alg][idx] is None:
			found_None = True
			break
	if idx != 0 and found_None and alg != "psql" and alg != "sysx":
		if alg == "QEq_Lazy" and title.endswith("QR1"):
			continue
		elif alg == "BatchHeap" and title.endswith("QR3"):
			ax.text(l_list[idx-1] - 0.2, times[alg][idx - 1] + 12, "OOM", color = cmap(i), size=17)
		else:
			ax.text((l_list[idx] + l_list[idx-1]) / 2.0, times[alg][idx - 1], "OOM", color = cmap(i), size=17)


ax.set_yscale('log', basey=10)
ax.set_ylim([None, 1000])

if k_to_plot == 1000:
	ax.set(xlabel="$\ell$", ylabel="TT($10^3$) sec")
	ax.xaxis.label.set_size(22)
else:
	ax.set(xlabel="$\ell$", ylabel="TT(" + str(k_to_plot) + ") sec")


#plt.title(title)
ax.grid()
plt.savefig(outFileName + ".pdf", format="pdf", bbox_inches="tight")
plt.savefig(outFileName + ".png", format="png", bbox_inches="tight")
plt.savefig(outFileName + ".svg", format="svg", bbox_inches="tight")