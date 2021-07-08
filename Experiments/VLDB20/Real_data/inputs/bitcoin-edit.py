#!/usr/bin/env python

import networkx as nx
import sys

with open('soc-sign-bitcoinotc.csv', 'r') as fin:
	with open('bitcoinotc.in', 'a') as fout:
		G = nx.DiGraph()
		fout.write("Relation Edges" + '\n')
		fout.write("From To" + '\n')
		for line in fin:
			n1, n2, rating, time = line.split(',')
			fout.write(str(n1) + ' ' + str(n2) + ' ' + str(10 - int(rating)) + '\n')
			G.add_edge(int(n1), int(n2))
		fout.write("End of Edges" + '\n')

		print "===== BitcoinOTC ===="
		print "Number of nodes = " + str(nx.number_of_nodes(G))
		print "Number of edges = " + str(nx.number_of_edges(G))
		degrees = nx.degree(G)
		degree_list = [d for (_, d) in degrees]
		print "Max degree = " + str(max(degree_list))
		avg_degree = sum(degree_list) * 1.0 / len(degree_list)
		print "Average degree = " + str(avg_degree)
