#!/bin/bash

source ../execution_parameters.sh
JAR_PATH="../../../target/any-k-1.0.jar"
DATA_PATH="inputs/"
OUT_PATH="outputs/"
OPTS="$OTHER_OPTS $MEM"
ALG_LIST=("Count") 
k=1000000
d=10000


## ========  Q1 l2  ======== ##
q="SynQ1"
l=2
for exp in 10 11 12 13 14 15 16 17 18 19 20 21 22
do
	n=$((2**${exp}))

	# Create the input if it doesn't exist
	INPUT="${DATA_PATH}path_n2${exp}_l${l}_i${i}.in"
	if [ ! -f $INPUT ]; then
		java -cp ${JAR_PATH} data.BinaryRandomPattern -q "path" -n $n -l $l -dom $d -o $INPUT
	fi  

	for alg in "${ALG_LIST[@]}"
	do
		java $OPTS -cp ${JAR_PATH} experiments.Path_Inequalities -q ${q} -a $alg -i "${DATA_PATH}path_n2${exp}_l${l}_i${i}.in" -ds -k $k -l $l >> "${OUT_PATH}${q}_l${l}_n2${exp}_${alg}.out"
		echo "Done with ${q}, Count query, l=${l}, n=$n"
	done
done

## ========  Q1 l4  ======== ##
q="SynQ1"
l=4
for exp in 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21
do
	n=$((2**${exp}))

	# Create the input if it doesn't exist
	INPUT="${DATA_PATH}path_n2${exp}_l${l}_i${i}.in"
	if [ ! -f $INPUT ]; then
		java -cp ${JAR_PATH} data.BinaryRandomPattern -q "path" -n $n -l $l -dom $d -o $INPUT
	fi  

	for alg in "${ALG_LIST[@]}"
	do
		java $OPTS -cp ${JAR_PATH} experiments.Path_Inequalities -q ${q} -a $alg -i "${DATA_PATH}path_n2${exp}_l${l}_i${i}.in" -ds -k $k -l $l >> "${OUT_PATH}${q}_l${l}_n2${exp}_${alg}.out"
		echo "Done with ${q}, Count query, l=${l}, n=$n"
	done
done

## ========  Q2 l2  ======== ##
q="SynQ2"
l=2 
for exp in 13 14 15 16 17 18 19 20 21 22
do
	n=$((2**${exp}))

	# Create the input if it doesn't exist
	INPUT="${DATA_PATH}path_n2${exp}_l${l}_i${i}.in"
	if [ ! -f $INPUT ]; then
		java -cp ${JAR_PATH} data.BinaryRandomPattern -q "path" -n $n -l $l -dom $d -o $INPUT
	fi  

	for alg in "${ALG_LIST[@]}"
	do
		java $OPTS -cp ${JAR_PATH} experiments.Path_Inequalities -q ${q} -a $alg -i "${DATA_PATH}path_n2${exp}_l${l}_i${i}.in" -ds -k $k -l $l >> "${OUT_PATH}${q}_l${l}_n2${exp}_${alg}.out"
		echo "Done with ${q}, Count query, l=${l}, n=$n"
	done
done

## ========  Q2 l4  ======== ##
q="SynQ2"
l=4
for exp in 9 10 11 12 13 14 15 16 17 18 19 20
do
	n=$((2**${exp}))

	# Create the input if it doesn't exist
	INPUT="${DATA_PATH}path_n2${exp}_l${l}_i${i}.in"
	if [ ! -f $INPUT ]; then
		java -cp ${JAR_PATH} data.BinaryRandomPattern -q "path" -n $n -l $l -dom $d -o $INPUT
	fi  

	for alg in "${ALG_LIST[@]}"
	do
		java $OPTS -cp ${JAR_PATH} experiments.Path_Inequalities -q ${q} -a $alg -i "${DATA_PATH}path_n2${exp}_l${l}_i${i}.in" -ds -k $k -l $l >> "${OUT_PATH}${q}_l${l}_n2${exp}_${alg}.out"
		echo "Done with ${q}, Count query, l=${l}, n=$n"
	done
done