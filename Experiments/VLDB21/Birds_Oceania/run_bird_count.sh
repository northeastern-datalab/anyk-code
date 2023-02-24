#!/bin/bash

source ../execution_parameters.sh
JAR_PATH="../../../target/any-k-1.0.jar"
DATA_PATH="inputs/"
OUT_PATH="outputs/"
OPTS="$OTHER_OPTS $MEM"
ALG_LIST=("Count") 


for graph in birdOceania
do
	l=2
	q="QB1"
	for millieps in 10 20 40 80 160 320 640 1280
	do					
		k=1000000
		for alg in "${ALG_LIST[@]}"
		do
			java $OPTS -cp ${JAR_PATH} experiments.Path_Inequalities -q "${q}_${millieps}" -a $alg -i "${DATA_PATH}${graph}.in" -l $l -ds -k $k >> "${OUT_PATH}${graph}_${q}_e${millieps}_${alg}.out"
			echo "Done with ${q}, Count query, millieps=${millieps}"
		done
	done
done
