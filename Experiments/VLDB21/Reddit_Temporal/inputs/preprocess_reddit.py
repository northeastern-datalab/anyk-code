#!/usr/bin/env python

import networkx as nx
import sys 
import os

def timestamp_to_int(timestamp_day, timestamp_time):
	return timestamp_day.replace('-', '') + timestamp_time.replace(':', '')


def preprocess(inFile, outFile):
	# Read once and map nodes to integers
	node_id = 1
	subreddits_to_ids = {}
	fp1 = open(inFile)
	line = fp1.readline() # The first line is the header
	line = fp1.readline()
	while line:
		if line != "":

			tokens = line.split()
			subr_1 = tokens[0]
			subr_2 = tokens[1]
			if subr_1 not in subreddits_to_ids:
				subreddits_to_ids[subr_1] = node_id
				node_id += 1
			if subr_2 not in subreddits_to_ids:
				subreddits_to_ids[subr_2] = node_id
				node_id += 1        

		line = fp1.readline()
	fp1.close()

	# Read again and create the graph
	G = nx.MultiDiGraph()
	fp1 = open(inFile)
	line = fp1.readline() # The first line is the header
	line = fp1.readline()
	while line:
		if line != "":

			tokens = line.split()
			properties = tokens[6].split(",")
			G.add_edge(subreddits_to_ids[tokens[0]], subreddits_to_ids[tokens[1]], \
				id=tokens[2], time=timestamp_to_int(tokens[3], tokens[4]), sentiment=properties[20], \
				length=properties[1], readability= -1.0 * float(properties[17]))     

		line = fp1.readline()
	fp1.close()

	print "===== " + inFile + " ===="
	print "Number of nodes = " + str(nx.number_of_nodes(G))
	print "Number of edges = " + str(nx.number_of_edges(G))
	degrees = nx.degree(G)
	degree_list = [d for (_, d) in degrees]
	print "Max degree = " + str(max(degree_list))
	avg_degree = sum(degree_list) * 1.0 / len(degree_list)
	print "Average degree = " + str(avg_degree)
	# print "Degree histogram: " + str(nx.degree_histogram(G))

	fout = open(outFile, 'w+')
	fout.write("Relation Reddit\n")
	fout.write("From To Timestamp Sentiment Length InverseReadability\n")
	#for fromNode, toNode, attrs in G.edges(data="attrs"):
	#fout.write(str(fromNode) + " " + str(toNode) + " " + str(attrs['time']) + " " + \
	#	str(attrs['sentiment']) + " " + str(attrs['length']) + " " + str(attrs['readability']) + "\n")
	for fromNode, toNode, data in G.edges(data=True):
		fout.write(str(fromNode) + " " + str(toNode) + " " + str(data['time']) + " " + \
			str(data['sentiment']) + " " + str(data['length']) + " " + str(data['readability']) + "\n")
	fout.write("End of Edges" + '\n')
	fout.close()

if __name__ == "__main__":
	#preprocess("soc-redditHyperlinks-body.tsv", "redditBody.in")
	preprocess("soc-redditHyperlinks-title.tsv", "redditTitle.in")