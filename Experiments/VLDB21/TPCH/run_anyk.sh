#!/bin/bash

source ../execution_parameters.sh
JAR_PATH="../../../target/any-k-1.0.jar"
DATA_PATH="inputs/"
OUT_PATH="outputs/"
OPTS="$OTHER_OPTS $MEM"
k=1000000
l=3

for i in $(seq 1 $ITERS);
do
	## ========  QT1: All  ======== ##
	q="QT1"
	ALG_LIST=("Lazy" "QEq_Lazy" "BatchHeap") 
	for millisf in 1 2 4 8
	# 016 OOM for batch
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
		for alg in "${ALG_LIST[@]}"
		do
			java $OPTS -cp ${JAR_PATH} experiments.Path_Inequalities -q $q -a $alg -i $INPUT -l $l -ds -k $k >> "${OUT_PATH}${q}_l${l}_msf${millisf}_${alg}.out"
			echo "Done with ${q}, alg $alg, msf=${millisf}, iter $i"
		done
	done

	## ========  QT1: Lazy, QEq  ======== ##
	q="QT1"
	ALG_LIST=("Lazy" "QEq_Lazy") 
	for millisf in 16 32 64 128
	# 256 OOM for QEq
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
		for alg in "${ALG_LIST[@]}"
		do
			java $OPTS -cp ${JAR_PATH} experiments.Path_Inequalities -q $q -a $alg -i $INPUT -l $l -ds -k $k >> "${OUT_PATH}${q}_l${l}_msf${millisf}_${alg}.out"
			echo "Done with ${q}, alg $alg, msf=${millisf}, iter $i"
		done
	done

	## ========  QT1: Lazy  ======== ##
	q="QT1"
	ALG_LIST=("Lazy") 
	for millisf in 256 512 1024
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
		for alg in "${ALG_LIST[@]}"
		do
			java $OPTS -cp ${JAR_PATH} experiments.Path_Inequalities -q $q -a $alg -i $INPUT -l $l -ds -k $k >> "${OUT_PATH}${q}_l${l}_msf${millisf}_${alg}.out"
			echo "Done with ${q}, alg $alg, msf=${millisf}, iter $i"
		done
	done
done


## Run once those that throw OOM

i=1
q="QT1"
ALG_LIST=("BatchHeap") 
for millisf in 16
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
	for alg in "${ALG_LIST[@]}"
	do
		java $OPTS -cp ${JAR_PATH} experiments.Path_Inequalities -q $q -a $alg -i $INPUT -l $l -ds -k $k >> "${OUT_PATH}${q}_l${l}_msf${millisf}_${alg}.out"
		echo "Done with ${q}, alg $alg, msf=${millisf}, iter $i"
	done
done

q="QT1"
ALG_LIST=("QEq_Lazy") 
for millisf in 256 
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
	for alg in "${ALG_LIST[@]}"
	do
		java $OPTS -cp ${JAR_PATH} experiments.Path_Inequalities -q $q -a $alg -i $INPUT -l $l -ds -k $k >> "${OUT_PATH}${q}_l${l}_msf${millisf}_${alg}.out"
		echo "Done with ${q}, alg $alg, msf=${millisf}, iter $i"
	done
done