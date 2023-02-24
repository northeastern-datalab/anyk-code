#!/bin/bash

source ../execution_parameters.sh
JAR_PATH="../../../target/any-k-1.0.jar"
DATA_PATH="inputs/"
OUT_PATH="outputs/"
OPTS="$OTHER_OPTS $MEM"
k=1000000
d=10000

for i in $(seq 1 $ITERS);
do
	## ========  Q1 l2 : All Algorithms ======== ##
	q="SynQ1"
	l=2
	ALG_LIST=("Lazy" "QEq_Lazy" "BatchHeap") 
	for exp in 10 11 12 13 14
	# 15 throws OOM for batch and Qeq
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
			echo "Done with ${q}, alg $alg, l=${l}, n=$n, iter $i"
		done
	done

	## ========  Q1 l2 : Only Lazy ======== ##
	q="SynQ1"
	l=2
	ALG_LIST=("Lazy") 
	for exp in 15 16 17 18 19 20 21 22
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
			echo "Done with ${q}, alg $alg, l=${l}, n=$n, iter $i"
		done
	done

	## ========  Q1 l4 : All Algorithms ======== ##
	q="SynQ1"
	l=4
	ALG_LIST=("Lazy" "QEq_Lazy" "BatchHeap") 
	for exp in 5 6 7
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
			echo "Done with ${q}, alg $alg, l=${l}, n=$n, iter $i"
		done
	done

	## ========  Q1 l4 : Lazy and QEq ======== ##
	q="SynQ1"
	l=4
	ALG_LIST=("Lazy" "QEq_Lazy") 
	for exp in 8 9 10 11 12 13
	# 14 throws OOM for QEq
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
			echo "Done with ${q}, alg $alg, l=${l}, n=$n, iter $i"
		done
	done

	## ========  Q1 l4 : Only Lazy ======== ##
	q="SynQ1"
	l=4
	ALG_LIST=("Lazy") 
	for exp in 14 15 16 17 18 19 20 21
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
			echo "Done with ${q}, alg $alg, l=${l}, n=$n, iter $i"
		done
	done

	## ========  Q2 l2 : All Algorithms ======== ##
	q="SynQ2"
	l=2
	ALG_LIST=("Lazy" "QEq_Lazy" "BatchHeap") 
	for exp in 13 14 15 16 17
	# 18 throws OOM for batch and Qeq
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
			echo "Done with ${q}, alg $alg, l=${l}, n=$n, iter $i"
		done
	done

	## ========  Q2 l2 : Only Lazy ======== ##
	q="SynQ2"
	l=2
	ALG_LIST=("Lazy") 
	for exp in 18 19 20 21 22
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
			echo "Done with ${q}, alg $alg, l=${l}, n=$n, iter $i"
		done
	done

	## ========  Q2 l4 : All Algorithms ======== ##
	q="SynQ2"
	l=4
	ALG_LIST=("Lazy" "QEq_Lazy" "BatchHeap") 
	for exp in 9 10 11 12
	# 13 throws OOM for Batch 
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
			echo "Done with ${q}, alg $alg, l=${l}, n=$n, iter $i"
		done
	done

	## ========  Q2 l4 : Lazy and QEq ======== ##
	q="SynQ2"
	l=4
	ALG_LIST=("Lazy" "QEq_Lazy") 
	for exp in 13 14 15 16
	# 17 throws OOM for QEq
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
			echo "Done with ${q}, alg $alg, l=${l}, n=$n, iter $i"
		done
	done

	## ========  Q2 l4 : Only Lazy ======== ##
	q="SynQ2"
	l=4
	ALG_LIST=("Lazy") 
	for exp in 17 18 19 20
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
			echo "Done with ${q}, alg $alg, l=${l}, n=$n, iter $i"
		done
	done
done

## -- Run once all those that throw OOM
i=1
q="SynQ1"
l=2
ALG_LIST=("QEq_Lazy" "BatchHeap") 
for exp in 15
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
		echo "Done with ${q}, alg $alg, l=${l}, n=$n, iter $i"
	done
done

q="SynQ1"
l=4
ALG_LIST=("BatchHeap") 
for exp in 8 9
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
		echo "Done with ${q}, alg $alg, l=${l}, n=$n, iter $i"
	done
done

q="SynQ1"
l=4
ALG_LIST=("QEq_Lazy") 
for exp in 14
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
		echo "Done with ${q}, alg $alg, l=${l}, n=$n, iter $i"
	done
done

q="SynQ2"
l=2
ALG_LIST=("QEq_Lazy" "BatchHeap") 
for exp in 18
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
		echo "Done with ${q}, alg $alg, l=${l}, n=$n, iter $i"
	done
done

q="SynQ2"
l=4
ALG_LIST=("BatchHeap") 
for exp in 13
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
		echo "Done with ${q}, alg $alg, l=${l}, n=$n, iter $i"
	done
done

q="SynQ2"
l=4
ALG_LIST=("QEq_Lazy") 
for exp in 17
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
		echo "Done with ${q}, alg $alg, l=${l}, n=$n, iter $i"
	done
done