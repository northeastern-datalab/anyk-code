#!/usr/bin/env python

import sys 
import os
import numpy as np

in_file = sys.argv[1]

# Read file
fp = open(in_file)
line = fp.readline()
schema = False
while line:
    if line.startswith("Relation "):
        tokens = line.split()
        relation_name = tokens[1]
        fp_out = open("inputs/" + relation_name + ".csv", "w")
        schema = True

    elif line.startswith("End of "):
        fp_out.close()

    elif schema:
        fp_out.write(line.rstrip() + " Weight\n")
        schema = False

    else:
        # Write the numbers as integers if possible
        tokens = line.split()
        for j in range(0, len(tokens)):
            t = tokens[j]
            if t.endswith(".0"):
                fp_out.write(t[:-2])
            else:
                fp_out.write(t)
            if j != len(tokens) - 1:
                fp_out.write(" ")
        fp_out.write("\n")

    line = fp.readline()
fp.close()