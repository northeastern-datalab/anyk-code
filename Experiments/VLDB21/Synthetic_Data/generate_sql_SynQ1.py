#!/usr/bin/env python

import sys 
sys.path.append('../')


input_dir = sys.argv[1]
l = int(sys.argv[2])
k = int(sys.argv[3])
dbms = sys.argv[4]

schema = "SYN"
tables = []
for j in range(1, l + 1):
    tables.append("R" + str(j))

if dbms == "psql":
    from psql_defs import *
elif dbms == "sysx":
    from sysx_defs import *


fp = open("sql_queries/SynQ1_l" + str(l) + "_k" + str(k) + "_" + dbms + ".sql", "w")
proc_name = "SynQ1l" + str(l) + "k" + str(k)

start_statements(fp, schema, tables)

create_synthetic_tables(fp, input_dir, l)



q = "SELECT"
for j in range(1, l + 1):
    q += " A" + str(2 * j - 1) + ", A" + str(2 * j) + ", Weight" + str(j) + ","
for j in range(1, l):
    q += " Weight" + str(j) + " +"
q += " Weight" + str(l) + " as Weight\n"
q += "FROM "
for j in range(1, l):
    q += "SYN.R" + str(j) + ", "
q += "SYN.R" + str(l) + "\n"
q += "WHERE "
for j in range(1, l - 1):
    q += "A" + str(2 * j) + " < A" + str(2 * j + 1) + " AND "
q += "A" + str(2 * (l - 1)) + " < A" + str(2 * (l - 1) + 1) + "\n" 
q += "ORDER BY Weight ASC"

q = apply_limit_k(q, k)
q += ";\n\n"


end_statements(fp, q, schema, tables, proc_name, k, l)


fp.close()