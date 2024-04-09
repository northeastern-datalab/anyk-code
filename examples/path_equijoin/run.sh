#!/bin/bash

JAR_PATH="../../target/any-k-1.0.jar"
OPTS="-Xmx8g -Xms8g -server"

# Pass parameters through json file
java -cp ${JAR_PATH} query_parser.MainEntryPoint -q query.json -p parameters.json

# Pass parameters through command line (overrides parameters.json)
# Only 3 answers returned here because of -k 3
# java -cp ${JAR_PATH} query_parser.MainEntryPoint -q query.json -p parameters.json -r "res.out" -t "time.out" -a "Recursive" -k 3
