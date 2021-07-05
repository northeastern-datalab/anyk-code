#!/bin/bash

source ../execution_parameters.sh
JAR_PATH="../../../target/any-k-1.0.jar"
DATA_PATH="inputs/"
OUT_PATH="outputs/"
OPTS=$MEM

graph=twitter_large
ALG_LIST=("Eager" "All" "Take2" "Lazy" "Recursive") 

for i in $(seq 1 $ITERS_TWITTER_PATH);
do
	for l in 3 4 6
	do
			k=1125149 # n/2
			for alg in "${ALG_LIST[@]}"
			do
				java $OPTS -cp ${JAR_PATH} experiments.Path_Equijoin -a $alg -i "${DATA_PATH}${graph}.in" -sj -l $l -ds -k $k >> "${OUT_PATH}path_${graph}_l${l}_${alg}.out"
			done
	done
	echo "Done with run ${i}"
done
