#!/usr/bin/env python

import sys 
import os
import numpy as np

input_dir = sys.argv[1]
l = int(sys.argv[2])

fp = open("sql_code/" + str(l) + "star.sql", "w")

fp.write("SET client_min_messages TO WARNING;\n\n")

for i in range(1, l + 1):
    fp.write("DROP TABLE IF EXISTS R" + str(i) + ";\n")
fp.write("\n")

attr_counter = 1
for i in range(1, l + 1):
    fp.write("CREATE TABLE R" + str(i) + " (\n")
    fp.write("\tA1 integer,\n")
    attr_counter += 1
#    fp.write("\tA" + str(attr_counter) + " integer,\n")
#    attr_counter += 1
    fp.write("\tA" + str(attr_counter) + " integer,\n")
    fp.write("\tWeight" + str(i) + " numeric\n")
    fp.write(");\n\n")

for i in range(1, l + 1):
    fp.write("COPY R" + str(i) + "\n")
    fp.write("FROM '" + input_dir + "R" + str(i) + ".csv'\n")
    fp.write("DELIMITER ' ' CSV HEADER;\n\n")


sel = "SELECT "
for j in range(1, attr_counter):
    sel += "A" + str(j) + ", "
sel += "A" + str(attr_counter) + "\n"
fr = "FROM "
for i in range(1, l):
    fr += "R" + str(i) + " NATURAL JOIN "
fr += "R" + str(l) + "\n"
order = "ORDER BY "
for i in range(1, l):
    order += "Weight" + str(i) + " + "
order += "Weight" + str(l) + ";\n"

fp.write("BEGIN TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;\n\n")

fp.write(sel)
fp.write(fr)
fp.write(order)
fp.write("\n")

fp.write("COMMIT;\n\n")

fp.write("BEGIN TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;\n\n")

fp.write(sel)
fp.write(fr)
fp.write(order)
fp.write("\n")

fp.write("COMMIT;\n\n")

fp.write("BEGIN TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;\n\n")

sel2 = "EXPLAIN ANALYZE " + sel

fp.write(sel2)
fp.write(fr)
fp.write(order)
fp.write("\n\n")

fp.write("COMMIT;\n\n")

for i in range(1, l + 1):
    fp.write("DROP TABLE R" + str(i) + ";")

fp.close()