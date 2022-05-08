#/bin/bash

source ../execution_parameters.sh
JAR_PATH="../../../target/any-k-1.0.jar"
DATA_PATH="inputs/"
OUT_PATH="outputs/"
OPTS=$MEM

iters=1
ALG_LIST=("BatchSorting")

if [ "$LENGTH4" = true ] ; then
	for i in $(seq 1 $iters);
	do
		for graph in bitcoinotc
		do
			for l in 4
			do
					for alg in "${ALG_LIST[@]}"
					do
						java $OPTS -cp ${JAR_PATH} experiments.Path_Equijoin -a $alg -i "${DATA_PATH}${graph}.in" -sj -l $l -ds >> "${OUT_PATH}path_${graph}_l${l}_${alg}.out"
					done
			done
		done
		echo "Done with run ${i}"
	done
fi

