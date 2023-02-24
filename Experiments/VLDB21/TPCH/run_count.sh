#!/bin/bash

source ../execution_parameters.sh
JAR_PATH="../../../target/any-k-1.0.jar"
DATA_PATH="inputs/"
OUT_PATH="outputs/"
OPTS="$OTHER_OPTS $MEM"
ALG_LIST=("Count") 
k=1000000

## ========  QT1 l=3  ======== ##
q="QT1"
l=3
for millisf in 1 2 4 8 16 32 64 128 256 512 1024
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
		echo "Done with ${q}, Count query, msf=${millisf}"
	done
done

## ========  QT1D l=3  ======== ##
q="QT1D"
l=3
for millisf in 1 2 4 8 16 32 64 128 256 512 1024
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
		java $OPTS -cp ${JAR_PATH} experiments.Path_Inequalities_Disjunction -q $q -a $alg -i $INPUT -l $l -ds -k $k >> "${OUT_PATH}${q}_l${l}_msf${millisf}_${alg}.out"
		echo "Done with ${q}, Count query, msf=${millisf}"
	done
done