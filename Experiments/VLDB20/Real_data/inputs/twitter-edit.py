#!/usr/bin/env python

import networkx as nx
import sys 
import os

if len(sys.argv) != 2:
    print "One argument required: <inFile(.csv)>"
    sys.exit(1)

G = nx.DiGraph()

fp1 = open(sys.argv[1] + ".csv")
line = fp1.readline()
while line:

	if line != "":
		tokens = line.split(",")
		G.add_edge(int(tokens[0]), int(tokens[1]))

	line = fp1.readline()
fp1.close()

print "===== " + sys.argv[1] + " ===="
print "Number of nodes = " + str(nx.number_of_nodes(G))
print "Number of edges = " + str(nx.number_of_edges(G))
degrees = nx.degree(G)
degree_list = [d for (_, d) in degrees]
print "Max degree = " + str(max(degree_list))
avg_degree = sum(degree_list) * 1.0 / len(degree_list)
print "Average degree = " + str(avg_degree)
# print "Degree histogram: " + str(nx.degree_histogram(G))

## Compute edge weights with PageRank

score = nx.pagerank(G)

fout = open(sys.argv[1] + ".in", 'w+')
fout.write("Relation Edges\n")
fout.write("From To\n")
for (fromNode, toNode) in list(G.edges()):
	cost = (1.0 - score[fromNode]) + (1.0 - score[toNode])
	fromNode = int(fromNode)
	toNode = int(toNode)
	fout.write(str(fromNode) + " " + str(toNode) + " " + str(cost) + "\n")
fout.write("End of Edges" + '\n')

