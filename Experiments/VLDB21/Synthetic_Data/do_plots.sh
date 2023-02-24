#!/bin/bash

OUT_PATH="outputs/"

METHOD_LIST=("binary_part" "multi_part" "shared_ranges")
q="SynQ1"
l=2
k=65536
exp=16
./plot_delay.py -a "${METHOD_LIST[@]}" -i "${OUT_PATH}${q}_l${l}_n2${exp}" -c $k -o "plots/Fig10b_${q}_l${l}_n2${exp}_delay" -t "Synthetic Query ${q}, l=${l}, n=2^${exp} Delay"
./plot_bars.py -a "${METHOD_LIST[@]}" -i "${OUT_PATH}${q}_l${l}_n2${exp}" -o "plots/Fig10a_${q}_l${l}_n2${exp}_bars" -t "Synthetic Query ${q}, l=${l}, n=2^${exp} TT(k)"

n_exp_list=(10 11 12 13 14 15 16 17 18 19 20 21 22)
./plot_mem.py -a "${METHOD_LIST[@]}" -n "${n_exp_list[@]}" -i "${OUT_PATH}${q}_l${l}" -k $k -o "plots/Fig10c_${q}_l${l}_mem" -t "Synthetic Query ${q}, l=${l}"

D_LIST=(8 10 12 14 16 18 20 22 24) 
./plot_dom.py -d "${D_LIST[@]}" -a "${METHOD_LIST[@]}" -i "${OUT_PATH}${q}_l${l}_n2${exp}" -o "plots/Fig10d_${q}_l${l}_n2${exp}_dom" -t "Synthetic Query ${q}, l=${l}, n=2^${exp} Domains"

./plot_legend.py -a "${METHOD_LIST[@]}" -o "plots/legend2" -nh



ALG_LIST=("Lazy" "QEq_Lazy" "BatchHeap" "psql" "sysx") 
k=1000

./plot_legend.py -a "${ALG_LIST[@]}" -o "plots/legend"

q="SynQ1"
l=2
n_exp_list=(11 12 13 14 15 16 17 18 19 20 21 22)
./plot_n.py -a "${ALG_LIST[@]}" -n "${n_exp_list[@]}" -i "${OUT_PATH}${q}_l${l}" -k $k -o "plots/Fig7a_${q}_l${l}" -t "Synthetic Query ${q}, l=${l}"

q="SynQ1"
l=4
n_exp_list=(6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21)
./plot_n.py -a "${ALG_LIST[@]}" -n "${n_exp_list[@]}" -i "${OUT_PATH}${q}_l${l}" -k $k -o "plots/Fig7b_${q}_l${l}" -t "Synthetic Query ${q}, l=${l}"

q="SynQ2"
l=2
n_exp_list=(13 14 15 16 17 18 19 20 21 22)
./plot_n.py -a "${ALG_LIST[@]}" -n "${n_exp_list[@]}" -i "${OUT_PATH}${q}_l${l}" -k $k -o "plots/Fig7c_${q}_l${l}" -t "Synthetic Query ${q}, l=${l}"

q="SynQ2"
l=4
n_exp_list=(9 10 11 12 13 14 15 16 17 18 19 20)
./plot_n.py -a "${ALG_LIST[@]}" -n "${n_exp_list[@]}" -i "${OUT_PATH}${q}_l${l}" -k $k -o "plots/Fig7d_${q}_l${l}" -t "Synthetic Query ${q}, l=${l}"



