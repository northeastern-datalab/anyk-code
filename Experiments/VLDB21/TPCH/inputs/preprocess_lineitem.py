#!/usr/bin/env python

import networkx as nx
import sys 
import os

inFile = sys.argv[1]
outFile = sys.argv[2]

def date_to_int(date):
	return date.replace('-', '')


fout = open(outFile, 'w+')
fout.write("Relation Lineitem\n")
fout.write("OrderKey PartKey Suppkey LineNumber Quantity NegExtendedPrice ShipDate CommitDate ReceiptDate\n")

# Read in file and modify
fp1 = open(inFile)
line = fp1.readline()
while line:
	if line != "":
		tokens = line.split("|")

		orderkey = tokens[0]
		partkey = tokens[1]
		suppkey = tokens[2]
		linenumber = tokens[3]
		quantity = tokens[4]
		extendedprice = tokens[5]	
		shipdate = tokens[10]	
		commitdate = tokens[11]	
		receiptdate = tokens[12]	

		fout.write(orderkey + " " + partkey + " " + suppkey + " " + linenumber + " " + quantity + " ")
		fout.write("-" + extendedprice + " " + date_to_int(shipdate) + " ") 
		fout.write(date_to_int(commitdate) + " " + date_to_int(receiptdate) + " ")
		fout.write("\n")
	

	line = fp1.readline()
fp1.close()


fout.write("End of Lineitem" + '\n')
fout.close()
