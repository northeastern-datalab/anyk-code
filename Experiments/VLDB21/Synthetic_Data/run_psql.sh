#!/bin/bash

source ../execution_parameters.sh
JAR_PATH="../../../target/any-k-1.0.jar"
DATA_PATH="inputs/"
OUT_PATH="outputs/"
k=1000
d=10000

for i in $(seq 1 $ITERS);
do 
	## ========  Q1 l2  ======== ##
	q="SynQ1"
	l=2
	for exp in 10 11 12 13 14 15 16
	do
        n=$((2**${exp}))
        # Create the input if it doesn't exist
        INPUT="${DATA_PATH}path_n2${exp}_l${l}_i${i}.in"
        if [ ! -f $INPUT ]; then
            java -cp ${JAR_PATH} data.BinaryRandomPattern -q "path" -n $n -l $l -dom $d -o $INPUT
        fi  
        # Generate csv files
        ./in_to_csv.py $INPUT
        # Generate sql query
        ./"generate_sql_${q}.py" `pwd`/inputs/ ${l} ${k} "psql"
        # Run the query
        psql $USER < "sql_queries/${q}_l${l}_k${k}_psql.sql" 2>> "${OUT_PATH}${q}_l${l}_msf${millisf}_k${k}_psql.out" | grep -A 9999 "QUERY PLAN" >> "${OUT_PATH}${q}_l${l}_n2${exp}_psql.out"
        echo "Done with ${q}, l=${l}, n=$n, iter $i"
        # Delete the csv files
        rm inputs/*.csv
    done

	## ========  Q1 l4  ======== ##
	q="SynQ1"
	l=4
	for exp in 5 6 7 8
	do
        n=$((2**${exp}))
        # Create the input if it doesn't exist
        INPUT="${DATA_PATH}path_n2${exp}_l${l}_i${i}.in"
        if [ ! -f $INPUT ]; then
            java -cp ${JAR_PATH} data.BinaryRandomPattern -q "path" -n $n -l $l -dom $d -o $INPUT
        fi  
        # Generate csv files
        ./in_to_csv.py $INPUT
        # Generate sql query
        ./"generate_sql_${q}.py" `pwd`/inputs/ ${l} ${k} "psql"
        # Run the query
        psql $USER < "sql_queries/${q}_l${l}_k${k}_psql.sql" 2>> "${OUT_PATH}${q}_l${l}_msf${millisf}_k${k}_psql.out" | grep -A 9999 "QUERY PLAN" >> "${OUT_PATH}${q}_l${l}_n2${exp}_psql.out"
        echo "Done with ${q}, l=${l}, n=$n, iter $i"
        # Delete the csv files
        rm inputs/*.csv
    done

	## ========  Q2 l2  ======== ##
	q="SynQ2"
	l=2
	for exp in 13 14 15 16
	do
        n=$((2**${exp}))
        # Create the input if it doesn't exist
        INPUT="${DATA_PATH}path_n2${exp}_l${l}_i${i}.in"
        if [ ! -f $INPUT ]; then
            java -cp ${JAR_PATH} data.BinaryRandomPattern -q "path" -n $n -l $l -dom $d -o $INPUT
        fi  
        # Generate csv files
        ./in_to_csv.py $INPUT
        # Generate sql query
        ./"generate_sql_${q}.py" `pwd`/inputs/ ${l} ${k} 50 "psql"
        # Run the query
        psql $USER < "sql_queries/${q}_l${l}_k${k}_psql.sql" 2>> "${OUT_PATH}${q}_l${l}_msf${millisf}_k${k}_psql.out" | grep -A 9999 "QUERY PLAN" >> "${OUT_PATH}${q}_l${l}_n2${exp}_psql.out"
        echo "Done with ${q}, l=${l}, n=$n, iter $i"
        # Delete the csv files
        rm inputs/*.csv
    done

	## ========  Q2 l4  ======== ##
	q="SynQ2"
	l=4
	for exp in 9 10 11
	do
        n=$((2**${exp}))
        # Create the input if it doesn't exist
        INPUT="${DATA_PATH}path_n2${exp}_l${l}_i${i}.in"
        if [ ! -f $INPUT ]; then
            java -cp ${JAR_PATH} data.BinaryRandomPattern -q "path" -n $n -l $l -dom $d -o $INPUT
        fi  
        # Generate csv files
        ./in_to_csv.py $INPUT
        # Generate sql query
        ./"generate_sql_${q}.py" `pwd`/inputs/ ${l} ${k} 50 "psql"
        # Run the query
        psql $USER < "sql_queries/${q}_l${l}_k${k}_psql.sql" 2>> "${OUT_PATH}${q}_l${l}_msf${millisf}_k${k}_psql.out" | grep -A 9999 "QUERY PLAN" >> "${OUT_PATH}${q}_l${l}_n2${exp}_psql.out"
        echo "Done with ${q}, l=${l}, n=$n, iter $i"
        # Delete the csv files
        rm inputs/*.csv
    done
done

## Run once those that are extremely slow
i=1
## ========  Q1 l4  ======== ##
q="SynQ1"
l=4
for exp in 9
# 9 will be cut off
do
    n=$((2**${exp}))
    # Create the input if it doesn't exist
    INPUT="${DATA_PATH}path_n2${exp}_l${l}_i${i}.in"
    if [ ! -f $INPUT ]; then
        java -cp ${JAR_PATH} data.BinaryRandomPattern -q "path" -n $n -l $l -dom $d -o $INPUT
    fi  
    # Generate csv files
    ./in_to_csv.py $INPUT
    # Generate sql query
    ./"generate_sql_${q}.py" `pwd`/inputs/ ${l} ${k} "psql"
    # Run the query
    psql $USER < "sql_queries/${q}_l${l}_k${k}_psql.sql" 2>> "${OUT_PATH}${q}_l${l}_n2${exp}_psql.out" | grep -A 9999 "QUERY PLAN" >> "${OUT_PATH}${q}_l${l}_n2${exp}_psql.out"
    echo "Done with ${q}, l=${l}, n=$n, iter $i"
    # Delete the csv files
    rm inputs/*.csv
done

## ========  Q2 l4  ======== ##
q="SynQ2"
l=4
for exp in 12
do
    n=$((2**${exp}))
    # Create the input if it doesn't exist
    INPUT="${DATA_PATH}path_n2${exp}_l${l}_i${i}.in"
    if [ ! -f $INPUT ]; then
        java -cp ${JAR_PATH} data.BinaryRandomPattern -q "path" -n $n -l $l -dom $d -o $INPUT
    fi  
    # Generate csv files
    ./in_to_csv.py $INPUT
    # Generate sql query
    ./"generate_sql_${q}.py" `pwd`/inputs/ ${l} ${k} 50 "psql"
    # Run the query
    psql $USER < "sql_queries/${q}_l${l}_k${k}_psql.sql" 2>> "${OUT_PATH}${q}_l${l}_n2${exp}_psql.out" | grep -A 9999 "QUERY PLAN" >> "${OUT_PATH}${q}_l${l}_n2${exp}_psql.out"
    echo "Done with ${q}, l=${l}, n=$n, iter $i"
    # Delete the csv files
    rm inputs/*.csv
done
