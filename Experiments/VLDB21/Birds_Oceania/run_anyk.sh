#!/bin/bash

source ../execution_parameters.sh
JAR_PATH="../../../target/any-k-1.0.jar"
DATA_PATH="inputs/"
OUT_PATH="outputs/"
OPTS="$OTHER_OPTS $MEM"
k=1000000

q="QB1"
l=2
for i in $(seq 1 $ITERS);
do
	for graph in birdOceania
	do
		ALG_LIST=("Lazy") 
		for millieps in 10 20 40 80 160 320 640 1280
		do
			for alg in "${ALG_LIST[@]}"
			do
				java $OPTS -cp ${JAR_PATH} experiments.Path_Inequalities -q "${q}_${millieps}" -a $alg -i "${DATA_PATH}${graph}.in" -l $l -ds -k $k >> "${OUT_PATH}${graph}_${q}_e${millieps}_${alg}.out"
				echo "Done with ${q}, alg $alg, millieps=${millieps}, iter $i"
			done
		done
	done

	ALG_LIST=("QEq_Lazy" "BatchHeap") 
	for millieps in 10 20 40 80
	do
		for alg in "${ALG_LIST[@]}"
		do
			java $OPTS -cp ${JAR_PATH} experiments.Path_Inequalities -q "${q}_${millieps}" -a $alg -i "${DATA_PATH}${graph}.in" -l $l -ds -k $k >> "${OUT_PATH}${graph}_${q}_e${millieps}_${alg}.out"
			echo "Done with ${q}, alg $alg, millieps=${millieps}, iter $i"
		done
	done
done

ALG_LIST=("QEq_Lazy" "BatchHeap") 
# They both throw OOM for 160, run once
i=1
for millieps in 160
do
	for alg in "${ALG_LIST[@]}"
	do
		java $OPTS -cp ${JAR_PATH} experiments.Path_Inequalities -q "${q}_${millieps}" -a $alg -i "${DATA_PATH}${graph}.in" -l $l -ds -k $k >> "${OUT_PATH}${graph}_${q}_e${millieps}_${alg}.out"
		echo "Done with ${q}, alg $alg, millieps=${millieps}, iter $i"
	done
done