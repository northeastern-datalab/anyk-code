#!/usr/bin/env python

import sys 
sys.path.append('../')

input_dir = sys.argv[1]
l = int(sys.argv[2])
k = int(sys.argv[3])
dbms = sys.argv[4]

schema = "GRAPHS"
table = "REDDIT"

if dbms == "psql":
    from psql_defs import *
elif dbms == "sysx":
    from sysx_defs import *


fp = open("sql_queries/QR1_l" + str(l) + "_k" + str(k) + "_" + dbms + ".sql", "w")
proc_name = "QR1l" + str(l) + "k" + str(k)


start_statements(fp, schema, table)

create_reddit_table(fp, input_dir)


q = "SELECT"
for j in range(1, l):
    q += " R" + str(j) + ".Source, R" + str(j) + ".Target, R" + str(j) + ".Timest, R" + str(j) + ".Sentiment, R" + str(j) + ".Len, R" + str(j) + ".InverseReadability,"
for j in range(1, l):
    q += " R" + str(j) + ".Sentiment +"
q += " R" + str(l) + ".Sentiment as Weight\n"
q += "FROM "
for j in range(1, l):
    q += "GRAPHS.REDDIT R" + str(j) + ", "
q += "GRAPHS.REDDIT R" + str(l) + "\n"
q += "WHERE "
for j in range(1, l - 1):
    q += "R" + str(j) + ".Target = R" + str(j + 1) + ".Source AND R" + str(j) + ".Timest < R" + str(j + 1) + ".Timest AND "
q += "R" + str(l - 1) + ".Target = R" + str(l) + ".Source AND R" + str(l - 1) + ".Timest < R" + str(l) + ".Timest\n"
q += "ORDER BY Weight ASC"

q = apply_limit_k(q, k)
q += ";\n\n"

end_statements(fp, q, schema, table, proc_name, k, l)


fp.close()