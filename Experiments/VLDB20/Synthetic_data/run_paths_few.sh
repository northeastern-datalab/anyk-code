#!/bin/bash

source ../execution_parameters.sh
JAR_PATH="../../../target/any-k-1.0.jar"
DATA_PATH="inputs/"
OUT_PATH="outputs/"
OPTS=$MEM

ALG_LIST=("Eager" "All" "Take2" "Lazy" "Recursive") 


l=4
for i in $(seq 1 $ITERS_PATHS_FEW);
do
        for n in 1000000
        do
                for d in $((n / 10))
                do
                        # Create the input if it doesn't exist
                        INPUT="${DATA_PATH}path_n${n}_l${l}_d${d}_i${i}.in"
                        if [ ! -f $INPUT ]; then
                                java -cp ${JAR_PATH} data.BinaryRandomPattern -q "path" -n $n -l $l -dom $d -o $INPUT
                        fi 

                        for alg in "${ALG_LIST[@]}"
                        do
                                k=$((n / 2))
                                java $OPTS -cp ${JAR_PATH} experiments.Path_Equijoin -a $alg -i $INPUT -n $n -l $l -dom $d -ds -k $k >> "${OUT_PATH}path_n${n}_l${l}_d${d}_${alg}.out"
                        done
                done
        done
        echo "Done with n=${n}, l=${l}, d=${d}, iter ${i}"
done

l=6
for i in $(seq 1 $ITERS_PATHS_FEW);
do
        for n in 1000000
        do
                for d in $((n / 10))
                do
                        # Create the input if it doesn't exist
                        INPUT="${DATA_PATH}path_n${n}_l${l}_d${d}_i${i}.in"
                        if [ ! -f $INPUT ]; then
                                java -cp ${JAR_PATH} data.BinaryRandomPattern -q "path" -n $n -l $l -dom $d -o $INPUT
                        fi 

                        for alg in "${ALG_LIST[@]}"
                        do
                                k=$((n / 2))
                                java $OPTS -cp ${JAR_PATH} experiments.Path_Equijoin -a $alg -i $INPUT -n $n -l $l -dom $d -ds -k $k >> "${OUT_PATH}path_n${n}_l${l}_d${d}_${alg}.out"
                        done
                done
        done
        echo "Done with n=${n}, l=${l}, d=${d}, iter ${i}"
done

l=3
for i in $(seq 1 $ITERS_PATHS_FEW);
do
        for n in 1000000
        do
                for d in $((n / 10))
                do
                        # Create the input if it doesn't exist
                        INPUT="${DATA_PATH}path_n${n}_l${l}_d${d}_i${i}.in"
                        if [ ! -f $INPUT ]; then
                                java -cp ${JAR_PATH} data.BinaryRandomPattern -q "path" -n $n -l $l -dom $d -o $INPUT
                        fi 

                        for alg in "${ALG_LIST[@]}"
                        do
                                k=$((n / 2))
                                java $OPTS -cp ${JAR_PATH} experiments.Path_Equijoin -a $alg -i $INPUT -n $n -l $l -dom $d -ds -k $k >> "${OUT_PATH}path_n${n}_l${l}_d${d}_${alg}.out"
                        done
                done
        done
        echo "Done with n=${n}, l=${l}, d=${d}, iter ${i}"
done
