#!/bin/bash

# Start and configure PostgreSQL
service postgresql start
runuser -l postgres -c 'createuser root -s'
runuser -l postgres -c 'createdb root'
cp /app/Experiments/VLDB21/postgresql.conf /etc/postgresql/9.5/main/

# Start the python virtual environment
source activate anyk_env

cd /app/Experiments/VLDB21/
./run_and_plot.sh

# Below command can be used to avoid container termination
# sleep infinity