#!/bin/bash

## Synthetic Data

cd Synthetic_data/

echo "    --- Running synthetic path queries, returning all query results"
./run_paths_all.sh
echo "    --- Running synthetic path queries, returning few query results"
./run_paths_few.sh
echo "    --- Running synthetic star queries, returning all query results"
./run_stars_all.sh
echo "    --- Running synthetic star queries, returning few query results"
./run_stars_few.sh
echo "    --- Running synthetic cycle queries, returning all query results"
./run_cycles_all.sh
echo "    --- Running synthetic cycle queries, returning few query results"
./run_cycles_few.sh

echo "    --- Plotting results for synthetic data"

./do_plots.sh

cd ../

## Real Data

cd Real_data/

echo "    --- Downloading and preprocessing real datasets"

cd inputs/
./create_input.sh
cd ../

echo "    --- Running path queries on bitcoin data"
./run_bitcoin_path.sh
echo "    --- Running path queries on twitter data"
./run_twitter_path.sh
echo "    --- Running star queries on bitcoin data"
./run_bitcoin_star.sh
echo "    --- Running star queries on twitter data"
./run_twitter_star.sh
echo "    --- Running cycle queries on bitcoin data"
./run_twitter_cycle.sh
echo "    --- Running cycle queries on twitter data"
./run_bitcoin_cycle.sh

echo "    --- Plotting results for real data"
./do_plots.sh

cd ../

## PostgreSQL on Synthetic Data

cd Synthetic_data/psql/

echo "    --- Running synthetic path queries with PSQL"
./run_paths.sh
echo "    --- Running synthetic star queries with PSQL"
./run_stars.sh
echo "    --- Running synthetic cycle queries with PSQL"
./run_cycles.sh

cd ../../