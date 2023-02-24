#!/bin/bash

source ../execution_parameters.sh
DATA_PATH="inputs/"
OUT_PATH="outputs/"
k=1000

for i in $(seq 1 $ITERS);
do
	## ========  QT1 l=3  ======== ##
	q="QT1"
	l=3
	for millisf in 1 2 4
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
		# Generate csv files
		./in_to_csv.py $INPUT
        # Generate sql query
        ./"generate_sql_${q}.py" `pwd`/inputs/Lineitem.csv ${l} ${k} psql
		# Run the query
		psql $USER < sql_queries/${q}_l${l}_k${k}_psql.sql 2>> "${OUT_PATH}${q}_l${l}_msf${millisf}_k${k}_psql.out" | grep -A 9999 "QUERY PLAN" >> "${OUT_PATH}${q}_l${l}_msf${millisf}_k${k}_psql.out"
		
		echo "Done with ${q}, msf=${millisf}, iter $i"
	done

	## ========  QT1D l=3  ======== ##
	q="QT1D"
	l=3
	for millisf in 0.1 1 2 4
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
		# Generate csv files
		./in_to_csv.py $INPUT
        # Generate sql query
        ./"generate_sql_${q}.py" `pwd`/inputs/Lineitem.csv ${l} ${k} psql
		# Run the query
		psql $USER < sql_queries/${q}_l${l}_k${k}_psql.sql 2>> "${OUT_PATH}${q}_l${l}_msf${millisf}_k${k}_psql.out" | grep -A 9999 "QUERY PLAN" >> "${OUT_PATH}${q}_l${l}_msf${millisf}_k${k}_psql.out"
	
		echo "Done with ${q}, msf=${millisf}, iter $i"
	done
done 


## Run once those that will be cut off
i=1
q="QT1"
l=3
for millisf in 8
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
	# Generate csv files
	./in_to_csv.py $INPUT
	# Generate sql query
	./"generate_sql_${q}.py" `pwd`/inputs/Lineitem.csv ${l} ${k} psql
	# Run the query
	psql $USER < sql_queries/${q}_l${l}_k${k}_psql.sql 2>> "${OUT_PATH}${q}_l${l}_msf${millisf}_k${k}_psql.out" | grep -A 9999 "QUERY PLAN" >> "${OUT_PATH}${q}_l${l}_msf${millisf}_k${k}_psql.out"

	echo "Done with ${q}, msf=${millisf}, iter $i"
done

q="QT1D"
l=3
for millisf in 8
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
	# Generate csv files
	./in_to_csv.py $INPUT
	# Generate sql query
	./"generate_sql_${q}.py" `pwd`/inputs/Lineitem.csv ${l} ${k} psql
	# Run the query
	psql $USER < sql_queries/${q}_l${l}_k${k}_psql.sql 2>> "${OUT_PATH}${q}_l${l}_msf${millisf}_k${k}_psql.out" | grep -A 9999 "QUERY PLAN" >> "${OUT_PATH}${q}_l${l}_msf${millisf}_k${k}_psql.out"

	echo "Done with ${q}, msf=${millisf}, iter $i"
done

# Delete the csv files
rm `pwd`/inputs/Lineitem.csv