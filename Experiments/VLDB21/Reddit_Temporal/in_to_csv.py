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
#        fp_out.write(line.rstrip() + " Weight\n")
        schema = False

    else:
        fp_out.write(line.rstrip().replace(" ", ",") + "\n")

    line = fp.readline()
fp.close()