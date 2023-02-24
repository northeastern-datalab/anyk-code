#!/bin/bash

source ../execution_parameters.sh
JAR_PATH="../../../target/any-k-1.0.jar"
DATA_PATH="inputs/"
OUT_PATH="outputs/"
OPTS="$OTHER_OPTS $MEM"
k=1000000

for i in $(seq 1 $ITERS);
do
	## ========  QT1 ======== ##
	q="QT1"
    ALG_LIST=("Lazy" "UnrankedEnum")
	for millisf in 32 64
	do
		# Create the input if it doesn't exist
		INPUT="${DATA_PATH}lineitem_msf${millisf}.in"
		if [ ! -f $INPUT ]; then
			cd ${DATA_PATH}
			sf=$(expr 0.001*${millisf} | bc)
			echo "Generating TPC-H Data with scale factor ${sf}"
			./dbgen -s $sf -T L -f
			./preprocess_lineitem.py "lineitem.tbl" "lineitem_msf${millisf}.in"
			cd ../
		fi  
        for l in 2 3
        do
            for alg in "${ALG_LIST[@]}"
            do
                java $OPTS -cp ${JAR_PATH} experiments.Path_Inequalities -q $q -a $alg -i $INPUT -l $l -ds -k $k >> "${OUT_PATH}${q}_l${l}_msf${millisf}_${alg}.out"
				echo "Done with ${q}, alg $alg, msf=${millisf}, iter $i"
            done
        done
	done

	## ========  QT1D ======== ##
	q="QT1D"
    ALG_LIST=("Lazy") 
	for millisf in 32 64
	do
		# Create the input if it doesn't exist
		INPUT="${DATA_PATH}lineitem_msf${millisf}.in"
		if [ ! -f $INPUT ]; then
			cd ${DATA_PATH}
			sf=$(expr 0.001*${millisf} | bc)
			echo "Generating TPC-H Data with scale factor ${sf}"
			./dbgen -s $sf -T L -f
			./preprocess_lineitem.py "lineitem.tbl" "lineitem_msf${millisf}.in"
			cd ../
		fi  
        for l in 2 3
        do
            for alg in "${ALG_LIST[@]}"
            do
                java $OPTS -cp ${JAR_PATH} experiments.Path_Inequalities_Disjunction -q $q -a $alg -i $INPUT -l $l -ds -k $k >> "${OUT_PATH}${q}_l${l}_msf${millisf}_${alg}.out"
				echo "Done with ${q}, alg $alg, msf=${millisf}, iter $i"
            done
        done
	done
done
