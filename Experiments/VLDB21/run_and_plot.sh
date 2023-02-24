#!/bin/bash

## Synthetic Data
cd Synthetic_Data/
./clean.sh

echo "=============== Counting results for Synthetic queries"
./run_count.sh

echo "=============== Running Synthetic queries with Any-k"
./run_anyk.sh

echo "=============== Running Any-k variants"
./run_fact_methods.sh

echo "=============== Running Synthetic queries with PSQL"
./run_psql.sh

# echo "=============== Running Synthetic queries with System X"
# ./run_sysx.sh

echo "=============== Plotting results for Synthetic data"
./do_plots.sh

cd ../

## Reddit
cd Reddit_Temporal/
./clean.sh

echo "=============== Downloading and preprocessing Reddit"

cd inputs/
wget -N https://snap.stanford.edu/data/soc-redditHyperlinks-title.tsv
./preprocess_reddit.py
cd ../

echo "=============== Counting results for Reddit"
./run_reddit_count.sh

echo "=============== Running Reddit queries with Any-k"
./run_anyk.sh

echo "=============== Running Reddit queries with PSQL"
./run_psql_reddit.sh

# echo "=============== Running Reddit queries with System X"
# ./run_sysx_reddit.sh

echo "=============== Plotting results for Reddit"
./do_plots.sh

cd ../

## TPCH
cd TPCH/
./clean.sh

echo "=============== Counting results for TPCH"
./run_count.sh

echo "=============== Running TPCH queries with Any-k"
./run_anyk.sh
./run_disjunction.sh
./run_k.sh

echo "=============== Running TPCH queries with PSQL"
./run_psql_tpch.sh

# echo "=============== Running TPCH queries with System X"
# ./run_sysx_tpch.sh

echo "=============== Plotting results for TPCH"
./do_plots.sh

cd ../

## Birds
cd Birds_Oceania/
./clean.sh

echo "=============== Downloading and preprocessing Birds dataset"
cd inputs/
wget -N https://api.gbif.org/v1/occurrence/download/request/0113354-200613084148143.zip
unzip 0113354-200613084148143.zip
./preprocess_observations.py
cd ../

echo "=============== Counting results for Birds"
./run_bird_count.sh

echo "=============== Running Birds queries with Any-k"
./run_anyk.sh

echo "=============== Running Birds queries with PSQL"
./run_psql.sh

# echo "=============== Running Birds queries with System X"
# ./run_sysx.sh

echo "=============== Plotting results for Birds"
./do_plots.sh

cd ../
