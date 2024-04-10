#!/bin/bash

JAR_PATH="../../target/any-k-1.0.jar"
OPTS="-Xmx8g -Xms8g -server"

# java -cp ${JAR_PATH} MainEntryPoint -q query.json -p parameters.json -po -r "result_path_optimization.out" -t "time_path_optimization.out"
# java -cp ${JAR_PATH} MainEntryPoint -q query.json -p parameters.json -r "result.out" -t "time.out"

java -cp ${JAR_PATH} MainEntryPoint -q query2.json -p parameters.json -po -r "result2.out" -t "time2.out"