#/bin/bash

OUT_PATH="outputs/"

# QT1 with unranked and disjunctions
q="QT1"
ALG_LIST=("Lazy" "UnrankedEnum" "Disjunction") 
for millisf in 32 64
do
    for l in 2
    do
        ./plot_k.py -a "${ALG_LIST[@]}" -i "${OUT_PATH}${q}_l${l}_msf${millisf}" -o "plots/${q}_l${l}_msf${millisf}_k" -t "TPC-H Query ${q}, l=${l}, msf=${millisf}"
    done
done

# Different scale factors
# QT1
ALG_LIST=("Lazy" "QEq_Lazy" "BatchHeap" "psql" "sysx") 
l=3
k=1000
millisf_list=(1 2 4 8 16 32 64 128 256 512 1024)
q="QT1"
./plot_sf.py -a "${ALG_LIST[@]}" -sf "${millisf_list[@]}" -i "${OUT_PATH}${q}_l${l}" -k $k -o "plots/Fig8a_${q}_l${l}" -t "TPC-H Query ${q}, l=${l}"
q="QT1D"
./plot_sf.py -a "${ALG_LIST[@]}" -sf "${millisf_list[@]}" -i "${OUT_PATH}${q}_l${l}" -k $k -o "plots/Fig8b_${q}_l${l}" -t "TPC-H Query ${q}, l=${l}"

