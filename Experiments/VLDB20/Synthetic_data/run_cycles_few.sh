#!/bin/bash

source ../../execution_parameters.sh
JAR_PATH="../../../target/any-k-1.0.jar"
DATA_PATH="inputs/"
OUT_PATH="outputs/"
OPTS=$MEM

iters=2000
ALG_LIST=("Eager" "All" "Take2" "Lazy" "Recursive") 


l=4
for i in $(seq 1 $iters);
do
	n=100000
	# Create the input if it doesn't exist
	INPUT="${DATA_PATH}cycle_n${n}_l${l}_i${i}.in"
	if [ ! -f $INPUT ]; then
		java -cp ${JAR_PATH} data.Cycle_HeavyLightPattern -n $n -l $l -o $INPUT
	fi 
	
	for alg in "${ALG_LIST[@]}"
	do
		k=$((n / 2))
		java $OPTS -cp ${JAR_PATH} experiments.SimpleCycle_Equijoin -a $alg -i $INPUT -n $n -l $l -k $k -ds >> "${OUT_PATH}cycle_n${n}_l${l}_${alg}.out"
	done
	echo "Done with n=${n}, l=${l}, iter ${i}"
done

l=6
for i in $(seq 1 $iters);
do
	n=100000
	# Create the input if it doesn't exist
	INPUT="${DATA_PATH}cycle_n${n}_l${l}_i${i}.in"
	if [ ! -f $INPUT ]; then
		java -cp ${JAR_PATH} data.Cycle_HeavyLightPattern -n $n -l $l -o $INPUT
	fi 

	for alg in "${ALG_LIST[@]}"
	do
		k=$((n / 2))
		java $OPTS -cp ${JAR_PATH} experiments.SimpleCycle_Equijoin -a $alg -i $INPUT -n $n -l $l -k $k -ds >> "${OUT_PATH}cycle_n${n}_l${l}_${alg}.out"
	done
	echo "Done with n=${n}, l=${l}, iter ${i}"
done
