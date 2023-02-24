#!/bin/bash

source ../execution_parameters.sh
JAR_PATH="../../../target/any-k-1.0.jar"
DATA_PATH="inputs/"
OUT_PATH="outputs/"
OPTS="$OTHER_OPTS $MEM"
k=1000000
ALG_LIST=("Count") 

for graph in redditTitle
do
	for q in "QR1" "QR2"
	do
		for l in 2 3 4 5 6 7 8 9 10
		do
				for alg in "${ALG_LIST[@]}"
				do
					java $OPTS -cp ${JAR_PATH} experiments.Path_Inequalities -q $q -a $alg -i "${DATA_PATH}${graph}.in" -l $l -ds -k $k >> "${OUT_PATH}path_${graph}_${q}_l${l}_${alg}.out"
					echo "Done with ${q}, Count query, l=${l}"
				done
		done
	done
done
