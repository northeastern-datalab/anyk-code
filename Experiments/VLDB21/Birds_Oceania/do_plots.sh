#/bin/bash

ALG_LIST=("Lazy" "QEq_Lazy" "BatchHeap" "psql" "sysx")

# QB1
q="QB1"
l=2
e=10
./plot_bars.py -a "${ALG_LIST[@]}" -i "outputs/birdOceania_${q}_e${e}" -o "plots/Fig9d_birdOceania_${q}_e${e}" -t "BirdsOceania ${q} $\epsilon=$ ${e}"

# VS Epslion
E_LIST=(10 20 40 80 160 320 640 1280)
k=1000
./plot_epsilon.py -a "${ALG_LIST[@]}" -e "${E_LIST[@]}" -k $k -i "outputs/birdOceania_${q}" -o "plots/Fig9h_birdOceania_${q}_l${l}" -t "BirdsOceania ${q}"