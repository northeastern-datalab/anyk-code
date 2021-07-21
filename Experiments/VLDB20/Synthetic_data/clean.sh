#!/bin/bash

rm inputs/*.in
rm outputs/*.out
rm plots/*.pdf
rm plots/*.png

cd psql/
./clean.sh
cd ../