#!/bin/bash

source ../../execution_parameters.sh
JAR_PATH="../../../../target/any-k-1.0.jar"

## 3-Star
n=100000
l=3
d=$((n / 10))
## Generate csv files
./generate_sql_star.py `pwd`/inputs/ $l
for i in $(seq 1 $ITERS_PSQL_STARS);
do
    # Create the input if it doesn't exist
    INPUT="../inputs/star_n${n}_l${l}_d${d}_i${i}.in"
    if [ ! -f $INPUT ]; then
        java -cp ${JAR_PATH} data.BinaryRandomPattern -q "star" -n $n -l $l -dom $d -o $INPUT
    fi
    # Generate csv files
    ./in_to_csv.py $INPUT
    # Run the query
    psql $USER < sql_code/3star.sql | grep "Execution time:" >> outputs/3star.out
    # Delete the csv files
    for j in $(seq 1 ${l});
    do
        rm "inputs/R${j}.csv"
    done
   echo "Done with 3-Star iteration $i"
done

## 4-Star
n=10000
l=4
d=$((n / 10))
## Generate csv files
./generate_sql_star.py `pwd`/inputs/ $l
for i in $(seq 1 $ITERS_PSQL_STARS);
do
    # Create the input if it doesn't exist
    INPUT="../inputs/star_n${n}_l${l}_d${d}_i${i}.in"
    if [ ! -f $INPUT ]; then
        java -cp ${JAR_PATH} data.BinaryRandomPattern -q "star" -n $n -l $l -dom $d -o $INPUT
    fi
    # Generate csv files
    ./in_to_csv.py $INPUT
    # Run the query
    psql $USER < sql_code/4star.sql | grep "Execution time:" >> outputs/4star.out
    # Delete the csv files
    for j in $(seq 1 ${l});
    do
        rm "inputs/R${j}.csv"
    done
   echo "Done with 4-Star iteration $i"
done

## 6-Star
n=100
l=6
d=$((n / 10))
## Generate csv files
./generate_sql_star.py `pwd`/inputs/ $l
for i in $(seq 1 $ITERS_PSQL_STARS);
do
    # Create the input if it doesn't exist
    INPUT="../inputs/star_n${n}_l${l}_d${d}_i${i}.in"
    if [ ! -f $INPUT ]; then
        java -cp ${JAR_PATH} data.BinaryRandomPattern -q "star" -n $n -l $l -dom $d -o $INPUT
    fi
    # Generate csv files
    ./in_to_csv.py $INPUT
    # Run the query
    psql $USER < sql_code/6star.sql | grep "Execution time:" >> outputs/6star.out
    # Delete the csv files
    for j in $(seq 1 ${l});
    do
        rm "inputs/R${j}.csv"
    done
   echo "Done with 6-Star iteration $i"
done
