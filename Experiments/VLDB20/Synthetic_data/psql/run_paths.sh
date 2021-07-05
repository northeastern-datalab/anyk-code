#!/bin/bash

source ../../execution_parameters.sh
JAR_PATH="../../../../target/any-k-1.0.jar"

## 3-PATH
n=100000
l=3
d=$((n / 10))
# Generate sql queries
./generate_sql_path.py `pwd`/inputs/ $l
for i in $(seq 1 $ITERS_PSQL_PATHS);
do
    # Create the input if it doesn't exist
    INPUT="../inputs/path_n${n}_l${l}_d${d}_i${i}.in"
    if [ ! -f $INPUT ]; then
        java -cp ${JAR_PATH} data.BinaryRandomPattern -q "path" -n $n -l $l -dom $d -o $INPUT
    fi 
    # Generate csv files
    ./in_to_csv.py $INPUT
    # Run the query
    psql $USER < sql_code/3path.sql | grep "Execution time:" >> outputs/3path.out
    # Delete the csv files
    for j in $(seq 1 ${l});
    do
        rm "inputs/R${j}.csv"
    done
   echo "Done with 3-Path iteration $i"
done

## 4-PATH
n=10000
l=4
d=$((n / 10))
# Generate sql queries
./generate_sql_path.py `pwd`/inputs/ $l
for i in $(seq 1 $ITERS_PSQL_PATHS);
do
    # Create the input if it doesn't exist
    INPUT="../inputs/path_n${n}_l${l}_d${d}_i${i}.in"
    if [ ! -f $INPUT ]; then
        java -cp ${JAR_PATH} data.BinaryRandomPattern -q "path" -n $n -l $l -dom $d -o $INPUT
    fi 
    # Generate csv files
    ./in_to_csv.py $INPUT
    # Run the query
    psql $USER < sql_code/4path.sql | grep "Execution time:" >> outputs/4path.out
    # Delete the csv files
    for j in $(seq 1 ${l});
    do
        rm "inputs/R${j}.csv"
    done
   echo "Done with 4-Path iteration $i"
done

## 6-PATH
n=100
l=6
d=$((n / 10))
# Generate sql queries
./generate_sql_path.py `pwd`/inputs/ $l
for i in $(seq 1 $ITERS_PSQL_PATHS);
do
    # Create the input if it doesn't exist
    INPUT="../inputs/path_n${n}_l${l}_d${d}_i${i}.in"
    if [ ! -f $INPUT ]; then
        java -cp ${JAR_PATH} data.BinaryRandomPattern -q "path" -n $n -l $l -dom $d -o $INPUT
    fi 
    # Generate csv files
    ./in_to_csv.py $INPUT
    # Run the query
    psql $USER < sql_code/6path.sql | grep "Execution time:" >> outputs/6path.out
    # Delete the csv files
    for j in $(seq 1 ${l});
    do
        rm "inputs/R${j}.csv"
    done
    echo "Done with 6-Path iteration $i"
done
