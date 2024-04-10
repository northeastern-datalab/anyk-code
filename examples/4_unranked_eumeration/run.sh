#!/bin/bash

JAR_PATH="../../target/any-k-1.0.jar"
OPTS="-Xmx8g -Xms8g -server"

# Pass parameters through json file
java -cp ${JAR_PATH} MainEntryPoint -q query.json -p parameters.json
