#!/usr/bin/env python

import sys 
sys.path.append('../')


input_file = sys.argv[1]
l = int(sys.argv[2])
k = int(sys.argv[3])
dbms = sys.argv[4]

schema = "TPCH"
table = "LINEITEM"

if dbms == "psql":
    from psql_defs import *
elif dbms == "sysx":
    from sysx_defs import *

fp = open("sql_queries/QT1D_l" + str(l) + "_k" + str(k) + "_" + dbms + ".sql", "w")
proc_name = "QT1Dl" + str(l) + "k" + str(k)

start_statements(fp, schema, table)

create_lineitem_table(fp, input_file)


q = "SELECT"
for j in range(1, l):
    q += " L" + str(j) + ".OrderKey, L" + str(j) + ".LineNumber, L" + str(j) + ".Quantity,"
for j in range(1, l):
    q += " L" + str(j) + ".NegExtendedPrice +"
q += " L" + str(l) + ".NegExtendedPrice as Weight\n"
q += "FROM "
for j in range(1, l):
    q += "TPCH.LINEITEM L" + str(j) + ", "
q += "TPCH.LINEITEM L" + str(l) + "\n"
q += "WHERE "
for j in range(1, l - 1):
	q += "L" + str(j) + ".SuppKey = L" + str(j + 1) + ".SuppKey AND L" + str(j) + ".Quantity < L" + str(j + 1) + ".Quantity AND (" \
		+ "L" + str(j) + ".ShipDate < L" + str(j + 1) + ".ShipDate OR " \
		+ "L" + str(j) + ".CommitDate < L" + str(j + 1) + ".CommitDate OR " \
		+ "L" + str(j) + ".ReceiptDate < L" + str(j + 1) + ".ReceiptDate) AND "
q += "L" + str(l - 1) + ".SuppKey = L" + str(l) + ".SuppKey AND L" + str(l - 1) + ".Quantity < L" + str(l) + ".Quantity AND (" \
	+ "L" + str(l - 1) + ".ShipDate < L" + str(l) + ".ShipDate OR " \
	+ "L" + str(l - 1) + ".CommitDate < L" + str(l) + ".CommitDate OR " \
	+ "L" + str(l - 1) + ".ReceiptDate < L" + str(l) + ".ReceiptDate)\n"
q += "ORDER BY Weight ASC"

q = apply_limit_k(q, k)
q += ";\n\n"


end_statements(fp, q, schema, table, proc_name, k, l)


fp.close()