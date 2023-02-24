#!/usr/bin/env python

import sys 
sys.path.append('../')


input_dir = sys.argv[1]
millieps = int(sys.argv[2])
k = int(sys.argv[3])
dbms = sys.argv[4]
l = 2

schema = "BIRDS"
table = "BirdObs"


if dbms == "psql":
    from psql_defs import *
elif dbms == "sysx":
    from sysx_defs import *


fp = open("sql_queries/QB1_e" + str(millieps) + "_k" + str(k) + "_" + dbms + ".sql", "w")
proc_name = "QBe" + str(millieps) + "k" + str(k)


start_statements(fp, schema, table)

create_birds_table(fp, input_dir)


q = "SELECT"
q += " B1.ID, B1.Latitude, B1.Longitude, B1.NegIndividualCount,"
q += " B2.ID, B2.Latitude, B2.Longitude, B2.NegIndividualCount,"
q += " B1.NegIndividualCount + B2.NegIndividualCount as Weight\n"
q += "FROM "
q += "BIRDS.BirdObs B1, BIRDS.BirdObs B2\n"
q += "WHERE B1.Latitude < B2.Latitude + " + str(millieps / 1000.0)
q += " AND B1.Longitude < B2.Longitude + " + str(millieps / 1000.0) + "\n"
q += "ORDER BY Weight ASC"

q = apply_limit_k(q, k)
q += ";\n\n"


end_statements(fp, q, schema, table, proc_name, k, l)


fp.close()