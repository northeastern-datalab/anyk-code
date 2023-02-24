#!/bin/bash

source ../execution_parameters.sh
DATA_PATH="inputs/"
OUT_PATH="outputs/"
graph=redditTitle

# Generate csv files
./in_to_csv.py "$DATA_PATH/${graph}.in"

# QR1 l=3 exceeded 3 hours
for i in $(seq 1 $ITERS);
do 
    for l in 2
    do
        for q in "QR1" "QR2"
        do
            for k in 1 1000 1000000
            do
                # Generate sql query
                ./"generate_sql_${q}.py" `pwd`/inputs/ ${l} ${k} psql
                # Run the query
                psql $USER < sql_queries/${q}_l${l}_k${k}_psql.sql 2>> "${OUT_PATH}path_${graph}_${q}_l${l}_k${k}_psql.out" | grep -A 9999 "QUERY PLAN" >> "${OUT_PATH}path_${graph}_${q}_l${l}_k${k}_psql.out"
                echo "Done with ${q}, l=${l}, k=${k}, iter $i"
            done
        done
    done
done

# QR2 l=3 exceeded 2 hours, run once
i=1
for l in 3
do
    for q in "QR2"
    do
        for k in 1 1000 1000000
        do
            # Generate sql query
            ./"generate_sql_${q}.py" `pwd`/inputs/ ${l} ${k} psql
            # Run the query
            psql $USER < sql_queries/${q}_l${l}_k${k}_psql.sql 2>> "${OUT_PATH}path_${graph}_${q}_l${l}_k${k}_psql.out" | grep -A 9999 "QUERY PLAN" >> "${OUT_PATH}path_${graph}_${q}_l${l}_k${k}_psql.out"
            echo "Done with ${q}, l=${l}, k=${k}, iter $i"
        done
    done
done

# QR1 l=3 exceeded 3 hours, run once
for l in 3
do
    for q in "QR1"
    do
        for k in 1 1000 1000000
        do
            # Generate sql query
            ./"generate_sql_${q}.py" `pwd`/inputs/ ${l} ${k} psql
            # Run the query
            psql $USER < sql_queries/${q}_l${l}_k${k}_psql.sql 2>> "${OUT_PATH}path_${graph}_${q}_l${l}_k${k}_psql.out" | grep -A 9999 "QUERY PLAN" >> "${OUT_PATH}path_${graph}_${q}_l${l}_k${k}_psql.out"
            echo "Done with ${q}, l=${l}, k=${k}, iter $i"
        done
    done
done


# Delete the csv files
rm inputs/Reddit.csv