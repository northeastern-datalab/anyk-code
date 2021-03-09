#!/usr/bin/env python

import os

def sample(dir, max_id, fout_name):
		## Read the file and keep only ids lower than max_id
		graphfile = os.path.join(dir, "twitter" + ".csv")
		f_out = open(os.path.join(dir, fout_name), "w")
		with open(graphfile) as f:
			lines = f.readlines()
		for line in lines:
			node_fro = int(line.split(',')[0])
			node_to = int(line.split(',')[1])
			if node_fro <= max_id and node_to <= max_id:
				f_out.write(line)
		f_out.close()

if __name__ == "__main__":
	sample(".", 8000, "twitter_small.csv")
	sample(".", 80000, "twitter_large.csv")
