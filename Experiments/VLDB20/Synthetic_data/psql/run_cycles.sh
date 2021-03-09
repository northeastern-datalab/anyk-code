#!/bin/bash

JAR_PATH="../../../../target/any-k-1.0.jar"
#iters=200
iters=3

## 4-CYCLE
n=5000
l=4
## Generate sql file
./generate_sql_cycle.py `pwd`/inputs/ $l
for i in $(seq 1 $iters);
do
	# Create the input if it doesn't exist
	INPUT="../inputs/cycle_n${n}_l${l}_i${i}.in"
	if [ ! -f $INPUT ]; then
		java -cp ${JAR_PATH} data.Cycle_HeavyLightPattern -n $n -l $l -o $INPUT
	fi 
    # Generate csv files
    ./in_to_csv.py $INPUT
    # Run the query
    psql $USER < sql_code/4cycle.sql | grep "Execution time:" >> outputs/4cycle.out
    # Delete the csv files
    for j in $(seq 1 ${l});
    do
        rm "inputs/R${j}.csv"
    done
   echo "Done with 4-Cycle iteration $i"
done

## 6-CYCLE
n=400
l=6
## Generate sql file
./generate_sql_cycle.py `pwd`/inputs/ $l
for i in $(seq 1 $iters);
do
	# Create the input if it doesn't exist
	INPUT="../inputs/cycle_n${n}_l${l}_i${i}.in"
	if [ ! -f $INPUT ]; then
		java -cp ${JAR_PATH} data.Cycle_HeavyLightPattern -n $n -l $l -o $INPUT
	fi 
    # Generate csv files
    ./in_to_csv.py $INPUT
    # Run the query
    psql $USER < sql_code/6cycle.sql | grep "Execution time:" >> outputs/6cycle.out
    # Delete the csv files
    for j in $(seq 1 ${l});
    do
        rm "inputs/R${j}.csv"
    done
   echo "Done with 6-Cycle iteration $i"
done
