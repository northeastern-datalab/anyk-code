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
lns = []

times = {}			# times is a dictionary that for each algorithm contains a list of runtimes (one for each n)
times_std = {}		# times is a dictionary that for each algorithm contains a list of runtimes (one for each n)

for i in range(len(algorithms)):
	alg = algorithms[i]
	times[alg] = []
	times_std[alg] = []

	for n_exp in n_exp_list:
		n = 2**int(n_exp)
		times_aux = []		# times_aux contains a list runtimes for a given n (one for each repetition)
		prev_k = -1			# records the last k we read so that we can find the maximal k for each repetition

		# Read file
		try:
			fp1 = open(inFileName + "_n2" + n_exp + "_" + alg + ".out")
		except IOError:
			times[alg].append(None)
			times_std[alg].append(None)
			continue

		if (alg.startswith("QEq_")) and "l=2" in title:
			## QuadEqui is the same as Batch for binary joins
			times[alg].append(None)
			times_std[alg].append(None)
			continue
		if (alg == "psql"):
			line = fp1.readline()
			while line:

				if line.startswith(" Execution time"):
					tokens = line.split()
					runtime = float(tokens[2]) / 1000.0
					times_aux.append(runtime)

				line = fp1.readline()
			fp1.close()
		elif (alg == "sysx"):
			line = fp1.readline()
			while line:

				if line.startswith("   CPU time ="):
					tokens = line.split()
					runtime = float(tokens[8]) / 1000.0
					times_aux.append(runtime)

				line = fp1.readline()
			fp1.close()
		else:
			line = fp1.readline()
			while line:

				if line.startswith("k="):
					tokens = line.split()
					k = int(tokens[1])
					runtime = float(tokens[3])
					if (k == k_to_plot):
						times_aux.append(runtime)

					# Store for checking in the next iteration
					prev_k = k
					prev_runtime = runtime

				line = fp1.readline()
			fp1.close()

		# We now have a list of runtimes for n gathered in times_aux
		# Take the median and append to times
		if len(times_aux):
			median_runtime = np.median(times_aux)
			std = np.std(times_aux)
			times[alg].append(median_runtime)
			times_std[alg].append(std)
		else:
			times[alg].append(None)
			times_std[alg].append(None)

# Find the result count for each n size
counts = []
for n_exp in n_exp_list:
	n = 2**int(n_exp)
	counts_aux = [] 	# holds one value per instance
	# Read file
	fp1 = open(inFileName + "_n2" + n_exp + "_Count.out")
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
	ax.plot(n_list, times[alg], label=alg_label, marker = markers[i], markersize = markersizes[i], fillstyle=fillstyles[i])

#plt.legend()


## ------------ Then the result counts ------------------
ax2 = ax.twinx()  # instantiate a second axes that shares the same x-axis
#suf_color = "rebeccapurple"
suf_color = (0.36, 0.38, 0.18, 1)
ax2.set_ylabel("Total Output Size", color=suf_color)
ax2.yaxis.label.set_color(suf_color)
ax2.tick_params(axis='y', colors=suf_color)
ax2.plot(n_list, counts, linestyle="dashdot", color=suf_color)
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
		if "SynQ1, l=2" in title:
			ax.text(n_list[idx], times[alg][idx - 1] * 1.2, "OOM", color = cmap(i), size=17)
		else:
			ax.text((n_list[idx] + n_list[idx-1]) / 2.0, times[alg][idx - 1] * 2, "OOM", color = cmap(i), size=17)

ax.set_yscale('log', basey=10)
ax.set_xscale('log', basex=2)
ax.set_ylim([None, 1500])
## set x ticks
#x_major = matplotlib.ticker.LogLocator(base = 2, numticks = len(n_exp_list) / 2)
#ax.xaxis.set_major_locator(x_major)
x_minor = matplotlib.ticker.LogLocator(base = 2, subs = np.arange(1.0, 16.0) / 16.0, numticks = len(n_exp_list))
ax.xaxis.set_minor_locator(x_minor)
ax.xaxis.set_minor_formatter(matplotlib.ticker.NullFormatter())

if k_to_plot == 1000:
	ax.set(xlabel="n", ylabel="TT($10^3$) sec")
	ax.xaxis.label.set_size(20)
else:
	ax.set(xlabel="n", ylabel="TT(" + str(k_to_plot) + ") sec")

#plt.title(title)
ax.grid()


plt.savefig(outFileName + ".pdf", format="pdf", bbox_inches="tight")
plt.savefig(outFileName + ".png", format="png", bbox_inches="tight")
plt.savefig(outFileName + ".svg", format="svg", bbox_inches="tight")

## Legend
#plt.rc('text', usetex=True)  
#plt.rc('font', family='serif', size=20) 
#h, l = ax.get_legend_handles_labels()
#figlegend = plt.figure(figsize=(4 * len(algorithms), 0.5))
#ax_leg = figlegend.add_subplot(111)
#ax_leg.legend(h, l, loc='center', ncol=len(algorithms), fancybox=True, shadow=True, prop={'size':30}, markerscale=2)
#ax_leg.axis('off')
#figlegend.savefig("plots/legend.pdf", format="pdf", bbox_inches="tight")