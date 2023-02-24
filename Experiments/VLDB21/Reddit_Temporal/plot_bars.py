#!/usr/bin/env python

import sys 
import os
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.patches import Ellipse, Polygon

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
algorithm_labels["BatchSorting"] = "Batch(Sort) Lower Bound"
algorithm_labels["BatchHeap"] = "Batch(Heap) Lower Bound"
algorithm_labels["QEq_Lazy"] = "QuadEqui Lower Bound"
algorithm_labels["Lazy"] = "Factorized Any-k"
algorithm_labels["psql"] = "PSQL"
algorithm_labels["sysx"] = "System X"

#linestyles["Recursive"] = (0, (5, 1))
#linestyles["Batch"] = 'dashdot'
#linestyles["BatchSorting"] = 'dashdot'

hatches = ['', '/', '\\', 'x', '--']

# Initialize plot
# marker_list = ["1", "2", "3", "x"]
plt.rcParams.update({'font.size': 19})
fig, ax = plt.subplots()


times = {}	# times[alg] contains a list of runtimes (one for each k)

k_list = [1, 1000, 1000000]

for i in range(len(algorithms)):
	alg = algorithms[i]

	times_aux = []		# times_aux contains a list of lists of runtimes (one list for each k contains all the runtimes for that k)
	times_aux.append([]) 	# k = 1
	times_aux.append([]) 	# k = 1000
	times_aux.append([])	# k = 1000000

	if (alg.startswith("QEq_")) and "2-Path" in title:
		## QuadEqui is the same as Batch for binary joins
		for j in range(len(k_list)):
			times_aux[j].append(0)
	elif (alg == "psql"):
		for j in range(len(k_list)):
			k = k_list[j]
			# Read file
			try:
				fp1 = open(inFileName + "_k" + str(k) + "_" + alg + ".out")
				line = fp1.readline()
				while line:
					if line.startswith(" Execution time"):
						tokens = line.split()
						runtime = float(tokens[2]) / 1000.0
						if runtime <= 7200:
							times_aux[j].append(runtime)
						else:
							times_aux[j].append(0)
					line = fp1.readline()
				fp1.close()
			except IOError:
				times_aux[j].append(0)
			
	elif (alg == "sysx"):
		for j in range(len(k_list)):
			k = k_list[j]
			# Read file
			try:
				fp1 = open(inFileName + "_k" + str(k) + "_" + alg + ".out")
				line = fp1.readline()
				while line:
					if line.startswith("   CPU time ="):
						tokens = line.split()
						runtime = float(tokens[8]) / 1000.0
						if runtime <= 7200:
							times_aux[j].append(runtime)
						else:
							times_aux[j].append(0)
					line = fp1.readline()
				fp1.close()
			except IOError:
				times_aux[j].append(0)
	else:
		# Read file
		try:
			fp1 = open(inFileName + "_" + alg + ".out")
			line = fp1.readline()
			while line:

				if line.startswith("k="):
					tokens = line.split()
					k = int(tokens[1])
					if (k == 1): 
						times_aux[0].append(float(tokens[3]))
					elif (k == 1000):
						times_aux[1].append(float(tokens[3]))
					elif (k == 1000000):
						times_aux[2].append(float(tokens[3]))

				line = fp1.readline()
			fp1.close()

		except IOError:
			times_aux[j].append(0)
		

	# Print the number of instances (only once)
	if (i == 0):
		print str(len(times_aux[0])) + " instances of " + title

	# Now build one list by taking the median
	if len(times_aux[0]):
		times[alg] = []
		for j in range(3):
			runtimes = times_aux[j]
			if 0 in runtimes or runtimes == []:
				times[alg].append(0)
			else:
				median_runtime = np.median(runtimes)
				times[alg].append(median_runtime)

#  =====   Plot	  =====
# set width of bar
barWidth = 1.0 / (len(algorithms) + 1)
# Set position of bar on X axis
x_axis = np.arange(3)
# Make the plot
cmap = plt.get_cmap("tab10")
for i in range(len(algorithms)):
	alg = algorithms[i]
	alg_label = algorithm_labels[alg]
	if (alg in times):
		plt.bar(x_axis, times[alg], width=barWidth, color=cmap(i), edgecolor='black', label=alg_label, hatch=hatches[i], alpha=0.8)
	# For the next algorithm
	x_axis = [x + barWidth for x in x_axis]
 
# Add xticks on the middle of the group bars
plt.xticks([r + ((len(algorithms) - 1) / 2.0) * barWidth for r in range(len(k_list))], ['$10^0$', '$10^3$', '$10^6$'])
#plt.xlim(None, len(algorithms) - 1 - barWidth)

#ax.set_xscale('log')
ax.grid(axis='y')

#plt.ticklabel_format(style='sci', axis='y', scilimits=(0,0))
ax.set_yscale('log', basey=10)



ax.set(xlabel="k", ylabel="TT(k) secs")
ax.xaxis.label.set_size(20)
#plt.legend()
#plt.title(title)
plt.savefig(outFileName + ".pdf", format="pdf", bbox_inches="tight")
plt.savefig(outFileName + ".png", format="png", bbox_inches="tight")