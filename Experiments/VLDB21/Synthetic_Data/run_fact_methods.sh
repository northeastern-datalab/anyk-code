#!/bin/bash

source ../execution_parameters.sh
JAR_PATH="../../../target/any-k-1.0.jar"
DATA_PATH="inputs/"
OUT_PATH="outputs/"
OPTS=$MEM

## Different domain sizes
for i in $(seq 1 $ITERS_MANY);
do
	q="SynQ1"
	l=2
	k=1
	exp=16
    METHOD_LIST=("binary_part" "multi_part" "shared_ranges")
	for dexp in 8 10 12 14 16 18 20 22 24
	do
		n=$((2**${exp}))
		d=$((2**${dexp}))
        # Create the input if it doesn't exist
        INPUT="${DATA_PATH}path_n2${exp}_l${l}_d2${dexp}_i${i}.in"
        if [ ! -f $INPUT ]; then
            java -cp ${JAR_PATH} data.BinaryRandomPattern -q "path" -n $n -l $l -dom $d -o $INPUT
        fi  
		for method in "${METHOD_LIST[@]}"
		do
			java $OPTS -cp ${JAR_PATH} experiments.Path_Inequalities -q ${q} -a "Lazy" -fm $method -i $INPUT -ds -k $k -l $l >> "${OUT_PATH}${q}_l${l}_n2${exp}_d2${dexp}_${method}.out"
			java $OPTS -cp ${JAR_PATH} experiments.Path_Inequalities -q ${q} -a "Mem" -fm $method -i $INPUT -ds -k $k -l $l >> "${OUT_PATH}${q}_l${l}_n2${exp}_d2${dexp}_${method}_mem.out"

			echo "Done with ${q}, $method method, l=${l}, n=$n, dom=$d, iter $i"
		done
	done
done

## Different k
k=1000000
d=10000
for i in $(seq 1 $ITERS_MANY);
do
	q="SynQ1"
	l=2
    METHOD_LIST=("binary_part" "multi_part" "shared_ranges")
	for exp in 16
	do
		n=$((2**${exp}))
        # Create the input if it doesn't exist
        INPUT="${DATA_PATH}path_n2${exp}_l${l}_i${i}.in"
        if [ ! -f $INPUT ]; then
            java -cp ${JAR_PATH} data.BinaryRandomPattern -q "path" -n $n -l $l -dom $d -o $INPUT
        fi  
		for method in "${METHOD_LIST[@]}"
		do
			java $OPTS -cp ${JAR_PATH} experiments.Path_Inequalities -q ${q} -a "Lazy" -fm $method -i "${DATA_PATH}path_n2${exp}_l${l}_i${i}.in" -ds -k $k -l $l >> "${OUT_PATH}${q}_l${l}_n2${exp}_${method}.out"
		
			echo "Done with ${q}, $method method, l=${l}, n=$n, iter $i"
		done
	done
done

## Memory for different n
k=1000000
d=10000
for i in $(seq 1 $ITERS_MANY);
do
	q="SynQ1"
	l=2
    METHOD_LIST=("binary_part" "multi_part" "shared_ranges")
	for exp in 10 11 12 13 14 15 16 17 18 19 20 21 22
	do
		n=$((2**${exp}))
        # Create the input if it doesn't exist
        INPUT="${DATA_PATH}path_n2${exp}_l${l}_i${i}.in"
        if [ ! -f $INPUT ]; then
            java -cp ${JAR_PATH} data.BinaryRandomPattern -q "path" -n $n -l $l -dom $d -o $INPUT
        fi  
		for method in "${METHOD_LIST[@]}"
		do
			java $OPTS -cp ${JAR_PATH} experiments.Path_Inequalities -q ${q} -a "Mem" -fm $method -i "${DATA_PATH}path_n2${exp}_l${l}_i${i}.in" -ds -k $k -l $l >> "${OUT_PATH}${q}_l${l}_n2${exp}_${method}_mem.out"
		
			echo "Done with ${q}, $method method memory, l=${l}, n=$n, iter $i"
		done
	done
done