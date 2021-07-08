#!/bin/bash

# Cycles (few results)
ALG_LIST=("Recursive" "Take2" "Lazy" "Eager" "All")
n=100000
l=4
./plot.py -a "${ALG_LIST[@]}" -i "outputs/cycle_n${n}_l${l}" -o "plots/Fig9j_cycle_n${n}_l${l}" -t "Cycle Query, n=${n}, l=${l}" -n $n -l $l

ALG_LIST=("Recursive" "Take2" "Lazy" "Eager" "All")
n=100000
l=6
./plot.py -a "${ALG_LIST[@]}" -i "outputs/cycle_n${n}_l${l}" -o "plots/cycle_n${n}_l${l}" -t "Cycle Query, n=${n}, l=${l}" -n $n -l $l

# Cycles (all results)
ALG_LIST=("Recursive" "Take2" "Lazy" "Eager" "All" "NPRR_Sort" "NPRR")
n=5000
l=4
./plot.py -a "${ALG_LIST[@]}" -i "outputs/cycle_n${n}_l${l}" -o "plots/Fig9i_cycle_n${n}_l${l}" -t "Cycle Query, n=${n}, l=${l}" -n $n -l $l

ALG_LIST=("Recursive" "Take2" "Lazy" "Eager" "All" "NPRR_Sort" "NPRR")
n=400
l=6
./plot.py -a "${ALG_LIST[@]}" -i "outputs/cycle_n${n}_l${l}" -o "plots/cycle_n${n}_l${l}" -t "Cycle Query, n=${n}, l=${l}" -n $n -l $l


# Stars (few results)
ALG_LIST=("Recursive" "Take2" "Lazy" "Eager" "All")
n=1000000
l=4
d=$((n / 10))
./plot.py -a "${ALG_LIST[@]}" -i "outputs/star_n${n}_l${l}_d${d}" -o "plots/Fig9f_star_n${n}_l${l}_d${d}" -t "Star Query, n=${n}, l=${l}, dom=${d}" -n $n -l $l -d $d

ALG_LIST=("Recursive" "Take2" "Lazy" "Eager" "All")
n=1000000
l=6
d=$((n / 10))
./plot.py -a "${ALG_LIST[@]}" -i "outputs/star_n${n}_l${l}_d${d}" -o "plots/star_n${n}_l${l}_d${d}" -t "Star Query, n=${n}, l=${l}, dom=${d}" -n $n -l $l -d $d

ALG_LIST=("Recursive" "Take2" "Lazy" "Eager" "All")
n=1000000
l=3
d=$((n / 10))
./plot.py -a "${ALG_LIST[@]}" -i "outputs/star_n${n}_l${l}_d${d}" -o "plots/star_n${n}_l${l}_d${d}" -t "Star Query, n=${n}, l=${l}, dom=${d}" -n $n -l $l -d $d


# Stars (all results)
ALG_LIST=("Recursive" "Take2" "Lazy" "Eager" "All" "BatchSorting" "Batch")
n=10000
l=4
d=$((n / 10))
./plot.py -a "${ALG_LIST[@]}" -i "outputs/star_n${n}_l${l}_d${d}" -o "plots/Fig9e_star_n${n}_l${l}_d${d}" -t "Star Query, n=${n}, l=${l}, dom=${d}" -n $n -l $l -d $d

ALG_LIST=("Recursive" "Take2" "Lazy" "Eager" "All" "BatchSorting" "Batch")
n=100
l=6
d=$((n / 10))
./plot.py -a "${ALG_LIST[@]}" -i "outputs/star_n${n}_l${l}_d${d}" -o "plots/star_n${n}_l${l}_d${d}" -t "Star Query, n=${n}, l=${l}, dom=${d}" -n $n -l $l -d $d

ALG_LIST=("Recursive" "Take2" "Lazy" "Eager" "All" "BatchSorting" "Batch")
n=100000
l=3
d=$((n / 10))
./plot.py -a "${ALG_LIST[@]}" -i "outputs/star_n${n}_l${l}_d${d}" -o "plots/star_n${n}_l${l}_d${d}" -t "Star Query, n=${n}, l=${l}, dom=${d}" -n $n -l $l -d $d



# Paths (few results)
ALG_LIST=("Recursive" "Take2" "Lazy" "Eager" "All")
n=1000000
l=4
d=$((n / 10))
./plot.py -a "${ALG_LIST[@]}" -i "outputs/path_n${n}_l${l}_d${d}" -o "plots/Fig9b_path_n${n}_l${l}_d${d}" -t "Path Query, n=${n}, l=${l}, d=${d}" -n $n -l $l -d $d

ALG_LIST=("Recursive" "Take2" "Lazy" "Eager" "All")
n=1000000
l=6
d=$((n / 10))
./plot.py -a "${ALG_LIST[@]}" -i "outputs/path_n${n}_l${l}_d${d}" -o "plots/path_n${n}_l${l}_d${d}" -t "Path Query, n=${n}, l=${l}, d=${d}" -n $n -l $l -d $d

ALG_LIST=("Recursive" "Take2" "Lazy" "Eager" "All")
n=1000000
l=3
d=$((n / 10))
./plot.py -a "${ALG_LIST[@]}" -i "outputs/path_n${n}_l${l}_d${d}" -o "plots/path_n${n}_l${l}_d${d}" -t "Path Query, n=${n}, l=${l}, d=${d}" -n $n -l $l -d $d



# Paths (all results)
ALG_LIST=("Recursive" "Take2" "Lazy" "Eager" "All" "BatchSorting" "Batch")
n=10000
l=4
d=$((n / 10))
./plot.py -a "${ALG_LIST[@]}" -i "outputs/path_n${n}_l${l}_d${d}" -o "plots/Fig9a_path_n${n}_l${l}_d${d}" -t "Path Query, n=${n}, l=${l}, d=${d}" -n $n -l $l -d $d

ALG_LIST=("Recursive" "Take2" "Lazy" "Eager" "All" "BatchSorting" "Batch")
n=100
l=6
d=$((n / 10))
./plot.py -a "${ALG_LIST[@]}" -i "outputs/path_n${n}_l${l}_d${d}" -o "plots/path_n${n}_l${l}_d${d}" -t "Path Query, n=${n}, l=${l}, d=${d}" -n $n -l $l -d $d

ALG_LIST=("Recursive" "Take2" "Lazy" "Eager" "All" "BatchSorting" "Batch")
n=100000
l=3
d=$((n / 10))
./plot.py -a "${ALG_LIST[@]}" -i "outputs/path_n${n}_l${l}_d${d}" -o "plots/path_n${n}_l${l}_d${d}" -t "Path Query, n=${n}, l=${l}, d=${d}" -n $n -l $l -d $d
