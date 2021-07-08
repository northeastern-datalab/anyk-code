#!/bin/bash

## Synthetic Data

#cd Synthetic_data/
#
#./run_paths_all.sh
#./run_paths_few.sh
#./run_stars_all.sh
#./run_stars_few.sh
#./run_cycles_all.sh
#./run_cycles_few.sh
#
#./do_plots.sh
#
#cd ../

## Real Data

cd Real_data/

cd inputs/
./create_input.sh
cd ../

./run_bitcoin_path.sh
./run_twitter_path.sh
./run_bitcoin_star.sh
./run_twitter_star.sh
./run_twitter_cycle.sh
./run_bitcoin_cycle.sh

./do_plots.sh

cd ../

## PostgreSQL on Synthetic Data

cd Synthetic_data/psql/

./run_paths.sh
./run_stars.sh
./run_cycles.sh

cd ../../