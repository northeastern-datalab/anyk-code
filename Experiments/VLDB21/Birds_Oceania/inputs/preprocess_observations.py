#!/usr/bin/env python

import csv
import sys

def isFloat(value):
	try:
		float(value)
		return True
	except ValueError:
		return False

def isInt(value):
	try:
		int(value)
		return True
	except ValueError:
		return False

def preprocess(inFile, outFile, sampleRate = 1):

	fout = open(outFile, 'w+')
	fout.write("Relation BirdObs\n")
	fout.write("ID Latitude Longitude NegIndividualCount\n")

	total = 0
	write = 0
	csv.field_size_limit(sys.maxsize)
	with open(inFile) as csvfile:
		reader = csv.DictReader(csvfile, delimiter='\t')
		for row in reader:
			if isFloat(row['decimalLatitude']) and isFloat(row['decimalLongitude']) and isInt(row['individualCount']):
				if total % sampleRate == 0:
					fout.write(row['gbifID'] + " " + row['decimalLatitude'] + " " + row['decimalLongitude'] + " " + str(-1.0 * int(row['individualCount'])) + "\n")
					write += 1
			total += 1
	fout.write("End of BirdObs" + '\n')
	fout.close()

	print "Total records = " + str(total) + " -  Kept records = " + str(write)


if __name__ == "__main__":
	preprocess("0113354-200613084148143.csv", "birdOceania.in")
	#preprocess("0113354-200613084148143.csv", "birdSmall.in", 100)