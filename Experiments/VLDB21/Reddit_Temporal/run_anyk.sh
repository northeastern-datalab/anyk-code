#!/bin/bash

source ../execution_parameters.sh
JAR_PATH="../../../target/any-k-1.0.jar"
DATA_PATH="inputs/"
OUT_PATH="outputs/"
OPTS="$OTHER_OPTS $MEM"
k=1000000

for i in $(seq 1 $ITERS);
do
	for graph in redditTitle
	do
		## ========  QR1, QR2: All  ======== ##
		for q in "QR1" "QR2"
		do
			for l in 2
			# 3 throws OOM for Batch
			do
				ALG_LIST=("Lazy" "QEq_Lazy" "BatchHeap") 
				for alg in "${ALG_LIST[@]}"
				do
					java $OPTS -cp ${JAR_PATH} experiments.Path_Inequalities -q $q -a $alg -i "${DATA_PATH}${graph}.in" -l $l -ds -k $k >> "${OUT_PATH}path_${graph}_${q}_l${l}_${alg}.out"
					echo "Done with ${q}, alg $alg, l=${l}, iter $i"
				done
			done
		done

		## ========  QR1: Lazy, QEq  ======== ##
		q="QR1"
		for l in 3 4
		# 5 throws OOM for QEq
		do
				ALG_LIST=("Lazy" "QEq_Lazy") 
				for alg in "${ALG_LIST[@]}"
				do
					java $OPTS -cp ${JAR_PATH} experiments.Path_Inequalities -q $q -a $alg -i "${DATA_PATH}${graph}.in" -l $l -ds -k $k >> "${OUT_PATH}path_${graph}_${q}_l${l}_${alg}.out"
					echo "Done with ${q}, alg $alg, l=${l}, iter $i"
				done
		done

		## ========  QR1: Lazy  ======== ##
		q="QR1"
		for l in 5 6 7 8 9 10
		do
				ALG_LIST=("Lazy") 
				for alg in "${ALG_LIST[@]}"
				do
					java $OPTS -cp ${JAR_PATH} experiments.Path_Inequalities -q $q -a $alg -i "${DATA_PATH}${graph}.in" -l $l -ds -k $k >> "${OUT_PATH}path_${graph}_${q}_l${l}_${alg}.out"
					echo "Done with ${q}, alg $alg, l=${l}, iter $i"
				done
		done

		## ========  QR2: Lazy, QEq  ======== ##
		q="QR2"
		for l in 3 4 5 6 7 8 9 10
		do
				ALG_LIST=("Lazy" "QEq_Lazy") 
				for alg in "${ALG_LIST[@]}"
				do
					java $OPTS -cp ${JAR_PATH} experiments.Path_Inequalities -q $q -a $alg -i "${DATA_PATH}${graph}.in" -l $l -ds -k $k >> "${OUT_PATH}path_${graph}_${q}_l${l}_${alg}.out"
					echo "Done with ${q}, alg $alg, l=${l}, iter $i"
				done
		done
	done
done

# Run once those that throw OOM
i=1
for graph in redditTitle
do
	for q in "QR1" "QR2"
	do
		for l in 3
		do
				ALG_LIST=("BatchHeap") 
				for alg in "${ALG_LIST[@]}"
				do
					java $OPTS -cp ${JAR_PATH} experiments.Path_Inequalities -q $q -a $alg -i "${DATA_PATH}${graph}.in" -l $l -ds -k $k >> "${OUT_PATH}path_${graph}_${q}_l${l}_${alg}.out"
					echo "Done with ${q}, alg $alg, l=${l}, iter $i"
				done
		done
	done

	q="QR1"
	for l in 5
	do
			ALG_LIST=("QEq_Lazy") 
			for alg in "${ALG_LIST[@]}"
			do
				java $OPTS -cp ${JAR_PATH} experiments.Path_Inequalities -q $q -a $alg -i "${DATA_PATH}${graph}.in" -l $l -ds -k $k >> "${OUT_PATH}path_${graph}_${q}_l${l}_${alg}.out"
				echo "Done with ${q}, alg $alg, l=${l}, iter $i"
			done
	done
done