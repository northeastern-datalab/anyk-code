#!/bin/bash

source ../../execution_parameters.sh
JAR_PATH="../../../../target/any-k-1.0.jar"

## 4-CYCLE
if [ "$LENGTH4" = true ] ; then
    n=5000
    l=4
    ## Generate sql file
    ./generate_sql_cycle.py `pwd`/inputs/ $l
    for i in $(seq 1 $ITERS_PSQL_CYCLES);
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
fi

## 6-CYCLE
if [ "$LENGTH6" = true ] ; then
    n=400
    l=6
    ## Generate sql file
    ./generate_sql_cycle.py `pwd`/inputs/ $l
    for i in $(seq 1 $ITERS_PSQL_CYCLES);
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
fi
