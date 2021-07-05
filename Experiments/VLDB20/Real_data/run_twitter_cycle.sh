#!/bin/bash

source ../execution_parameters.sh
JAR_PATH="../../../target/any-k-1.0.jar"
DATA_PATH="inputs/"
OUT_PATH="outputs/"
OPTS=$MEM

graph=twitter_small
ALG_LIST=("Eager" "All" "Take2" "Lazy" "Recursive") 


for i in $(seq 1 $ITERS_TWITTER_CYCLE);
do
	l=4
	# 87687 edges
	k=876870 # 10n
	for alg in "${ALG_LIST[@]}"
	do
		java $OPTS -cp ${JAR_PATH} experiments.SimpleCycle_Equijoin -a $alg -i "${DATA_PATH}${graph}.in" -sj -l $l -ds -k $k >> "${OUT_PATH}cycle_${graph}_l${l}_${alg}.out"
	done
	
	l=6
	# 87687 edges
	k=876870 # 10n
	for alg in "${ALG_LIST[@]}"
	do
		java $OPTS -cp ${JAR_PATH} experiments.SimpleCycle_Equijoin -a $alg -i "${DATA_PATH}${graph}.in" -sj -l $l -ds -k $k >> "${OUT_PATH}cycle_${graph}_l${l}_${alg}.out"
	done	

	echo "Done with run ${i}"
done


