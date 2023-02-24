#!/usr/bin/env python

import sys 
import os
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import matplotlib.collections as mcol
from matplotlib.legend_handler import HandlerLineCollection, HandlerTuple

## -- Read input
import argparse
parser = argparse.ArgumentParser(description='Plotting script')

parser.add_argument('-a', nargs='+', dest="alg_list", default=[], help="list of algorithms")
parser.add_argument('-o', action="store", dest="outFileName", default="out", help="Name of output file")
parser.add_argument('-nh', action='store_true', dest="no_hatches", default=False, help="Disable patterns in the bars")


arg_results = parser.parse_args()
algorithms = arg_results.alg_list
outFileName = arg_results.outFileName
no_hatches = arg_results.no_hatches


algorithm_labels = {}
algorithm_labels["Batch"] = "Batch(No sort)"
algorithm_labels["BatchSorting"] = "Batch(Sort) Lower Bound"
algorithm_labels["BatchHeap"] = "Batch Lower Bound"
algorithm_labels["QEq_Lazy"] = "QuadEqui Lower Bound"
algorithm_labels["Lazy"] = "Factorized"
algorithm_labels["psql"] = "PSQL"
algorithm_labels["sysx"] = "System X"

algorithm_labels["shared_ranges"] = "Shared Ranges"
algorithm_labels["binary_part"] = "Binary Partitioning"
algorithm_labels["multi_part"] = "Multiway Partitioning"
algorithm_labels["shared_ranges_nolazy"] = "Shared Ranges"
algorithm_labels["binary_part_nolazy"] = "Binary Partitioning"
algorithm_labels["multi_part_nolazy"] = "Multiway Partitioning"

# Parameters for lines
markers=['x', '^', '*', 'd', 'o', '', '']
markersizes=[11, 10, 14, 9, 13, 0, 0]
fillstyles=['full', 'full', 'full', 'full', 'none', 'full', 'full']
linewidths=[1.5, 1.5, 1.5, 1.5, 1.5, 2, 2]
alphas=[1, 1, 0.9, 1, 1, 1, 1]

# Parameters for bars
if no_hatches:
	hatches = ['', '', '', '', '']
else:
	hatches = ['', '/', '\\', 'x', '--']


fig, ax = plt.subplots()
handle_1_list = []
handle_2_list = []
for i in range(len(algorithms)):
	alg = algorithms[i]

	x = [1, 2]
	y = [3, 4]

	# Plot lines
	alg_label = algorithm_labels[alg]
	handle_1, = ax.plot(x, y, label=alg_label, marker = markers[i], markersize = markersizes[i], fillstyle = fillstyles[i])
	handle_1_list.append(handle_1)

for i in range(len(algorithms)):
	alg = algorithms[i]

	x = [1, 2]
	y = [3, 4]

	# Plot bars
	barWidth = 0.25
	x_axis = np.arange(len(algorithms))
	handle_2 = ax.bar(x_axis[i], y[0], width=barWidth, edgecolor='black', label=alg_label, hatch=hatches[i])
	handle_2_list.append(handle_2)


## Legend
plt.rc('text')#, usetex=True)  
plt.rc('font', family='serif', size=20) 
figlegend = plt.figure()
figlegend = plt.figure(figsize=(4 * len(algorithms), 0.5))
ax_leg = figlegend.add_subplot(111)

label_list = []
for alg in algorithms:
	label_list.append(algorithm_labels[alg])

handle_list = []
for i in range(len(algorithms)):
	handle_list.append((handle_1_list[i], handle_2_list[i]))

ax_leg.legend(handle_list, label_list, handler_map={tuple: HandlerTuple(ndivide=None)}, 
	loc='center', ncol=len(algorithms), fancybox=True, shadow=True, prop={'size':30}, markerscale=2, handlelength=4)
ax_leg.axis('off')
figlegend.savefig(outFileName + "_wbars.pdf", format="pdf", bbox_inches="tight")

# Do the same without the bars
figlegend = plt.figure()
figlegend = plt.figure(figsize=(4 * len(algorithms), 0.5))
ax_leg = figlegend.add_subplot(111)

ax_leg.legend(handle_1_list, label_list, handler_map={tuple: HandlerTuple(ndivide=None)}, 
	loc='center', ncol=len(algorithms), fancybox=True, shadow=True, prop={'size':30}, markerscale=2)
ax_leg.axis('off')
figlegend.savefig(outFileName + ".pdf", format="pdf", bbox_inches="tight")
figlegend.savefig(outFileName + ".svg", format="svg", bbox_inches="tight")