#/bin/bash

ALG_LIST=("Recursive" "Take2" "Lazy" "Eager" "All")

# Paths
l=4
./plot.py -a "${ALG_LIST[@]}" -i "outputs/path_twitter_large_l${l}" -o "plots/Fig9d_path_twitter_l${l}" -t "Twitter network Path Query, l=${l}" -l $l
./plot.py -a "${ALG_LIST[@]}" -i "outputs/path_bitcoinotc_l${l}" -o "plots/Fig9c_path_bitcoinotc_l${l}" -t "Bitcoin OTC network Path Query, l=${l}" -l $l

for l in 3 6
do
	./plot.py -a "${ALG_LIST[@]}" -i "outputs/path_twitter_large_l${l}" -o "plots/path_twitter_l${l}" -t "Twitter network Path Query, l=${l}" -l $l
	./plot.py -a "${ALG_LIST[@]}" -i "outputs/path_bitcoinotc_l${l}" -o "plots/path_bitcoinotc_l${l}" -t "Bitcoin OTC network Path Query, l=${l}" -l $l
done

# Stars
l=4
./plot.py -a "${ALG_LIST[@]}" -i "outputs/star_twitter_large_l${l}" -o "plots/Fig9h_star_twitter_l${l}" -t "Twitter network Star Query, l=${l}" -l $l
./plot.py -a "${ALG_LIST[@]}" -i "outputs/star_bitcoinotc_l${l}" -o "plots/Fig9g_star_bitcoinotc_l${l}" -t "Bitcoin OTC network Star Query, l=${l}" -l $l

for l in 3 6
do
	./plot.py -a "${ALG_LIST[@]}" -i "outputs/star_twitter_large_l${l}" -o "plots/star_twitter_l${l}" -t "Twitter network Star Query, l=${l}" -l $l
	./plot.py -a "${ALG_LIST[@]}" -i "outputs/star_bitcoinotc_l${l}" -o "plots/star_bitcoinotc_l${l}" -t "Bitcoin OTC network Star Query, l=${l}" -l $l
done

# Cycles
l=4
./plot.py -a "${ALG_LIST[@]}" -i "outputs/cycle_twitter_small_l${l}" -o "plots/Fig9l_cycle_twitter_l${l}" -t "Twitter network Cycle Query, l=${l}" -l $l
./plot.py -a "${ALG_LIST[@]}" -i "outputs/cycle_bitcoinotc_l${l}" -o "plots/Fig9k_cycle_bitcoinotc_l${l}" -t "Bitcoin OTC network Cycle Query, l=${l}" -l $l

for l in 6
do
	./plot.py -a "${ALG_LIST[@]}" -i "outputs/cycle_twitter_small_l${l}" -o "plots/cycle_twitter_l${l}" -t "Twitter network Cycle Query, l=${l}" -l $l
	./plot.py -a "${ALG_LIST[@]}" -i "outputs/cycle_bitcoinotc_l${l}" -o "plots/cycle_bitcoinotc_l${l}" -t "Bitcoin OTC network Cycle Query, l=${l}" -l $l
done
