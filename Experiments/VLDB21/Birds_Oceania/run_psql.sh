#!/bin/bash

source ../execution_parameters.sh
DATA_PATH="inputs/"
OUT_PATH="outputs/"
input=birdOceania
#input=birdSmall

# Generate csv files
./in_to_csv.py "$DATA_PATH/${input}.in"

# All k's exceed 6 hours, run them once
l=2
q="QB1"
millieps=10
for k in 1 1000 1000000
do
    # Generate sql query
    ./"generate_sql_${q}.py" `pwd`/inputs/ ${millieps} ${k} psql
    # Run the query
    psql $USER < sql_queries/${q}_e${millieps}_k${k}_psql.sql 2>> "${OUT_PATH}${input}_${q}_e${millieps}_k${k}_psql.out" | grep -A 9999 "QUERY PLAN" >> "${OUT_PATH}${input}_${q}_e${millieps}_k${k}_psql.out"
    echo "Done with ${q}, millieps=${millieps}, k=$k, iter $i"
done

#    k=1000
#    for millieps in 20 40 80 160 320 640 1280
#    do
#        # Generate sql query
#        ./"generate_sql_${q}.py" `pwd`/inputs/ ${millieps} ${k} psql
#        # Run the query
#        psql $USER < sql_queries/${q}_e${millieps}_k${k}.sql 2>> "${OUT_PATH}${input}_${q}_e${millieps}_k${k}_psql.out" | grep -A 9999 "QUERY PLAN" >> "${OUT_PATH}${input}_${q}_e${millieps}_k${k}_psql.out"
#        echo "Done with ${q}, millieps=${millieps}, k=$k, iter $i"
#    done

# Delete the csv files
rm inputs/BirdObs.csv