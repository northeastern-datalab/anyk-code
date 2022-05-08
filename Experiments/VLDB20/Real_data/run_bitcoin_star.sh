#!/bin/bash

source ../execution_parameters.sh
JAR_PATH="../../../target/any-k-1.0.jar"
DATA_PATH="inputs/"
OUT_PATH="outputs/"
OPTS=$MEM

graph=bitcoinotc
ALG_LIST=("Eager" "All" "Take2" "Lazy" "Recursive") 


for i in $(seq 1 $ITERS_BITCOIN_STAR);
do
	if [ "$LENGTH4" = true ] ; then
			l=4
			k=17796 # n/2
			for alg in "${ALG_LIST[@]}"
			do
				java $OPTS -cp ${JAR_PATH} experiments.BinaryStar_Equijoin -a $alg -i "${DATA_PATH}${graph}.in" -sj -l $l -ds -k $k >> "${OUT_PATH}star_${graph}_l${l}_${alg}.out"
			done
	fi
			
	if [ "$LENGTH3" = true ] ; then
			l=3
			k=17796 # n/2
			for alg in "${ALG_LIST[@]}"
			do
				java $OPTS -cp ${JAR_PATH} experiments.BinaryStar_Equijoin -a $alg -i "${DATA_PATH}${graph}.in" -sj -l $l -ds -k $k >> "${OUT_PATH}star_${graph}_l${l}_${alg}.out"
			done
	fi

	if [ "$LENGTH6" = true ] ; then
			l=6
			k=17796 # n/2
			for alg in "${ALG_LIST[@]}"
			do
				java $OPTS -cp ${JAR_PATH} experiments.BinaryStar_Equijoin -a $alg -i "${DATA_PATH}${graph}.in" -sj -l $l -ds -k $k >> "${OUT_PATH}star_${graph}_l${l}_${alg}.out"
			done
	fi

	echo "Done with run ${i}"
done
