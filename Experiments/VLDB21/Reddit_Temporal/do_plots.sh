#/bin/bash

ALG_LIST=("Lazy" "QEq_Lazy" "BatchHeap" "psql" "sysx")

# QR1
q="QR1"
l=2
./plot_bars.py -a "${ALG_LIST[@]}" -i "outputs/path_redditTitle_${q}_l${l}" -o "plots/Fig9a_path_redditTitle_${q}_l${l}" -t "Reddit Title ${q}, ${l}-Path" -l $l
l=3
./plot_bars.py -a "${ALG_LIST[@]}" -i "outputs/path_redditTitle_${q}_l${l}" -o "plots/Fig9b_path_redditTitle_${q}_l${l}" -t "Reddit Title ${q}, ${l}-Path" -l $l
L_LIST=(2 3 4 5 6 7 8 9 10)
k=1000
./plot_length.py -a "${ALG_LIST[@]}" -i "outputs/path_redditTitle_${q}" -l "${L_LIST[@]}" -k $k -o "plots/Fig9c_path_redditTitle_${q}" -t "Reddit Title ${q}"

# QR2
q="QR2"
l=2
./plot_bars.py -a "${ALG_LIST[@]}" -i "outputs/path_redditTitle_${q}_l${l}" -o "plots/Fig9e_path_redditTitle_${q}_l${l}" -t "Reddit Title ${q}, ${l}-Path" -l $l
l=3
./plot_bars.py -a "${ALG_LIST[@]}" -i "outputs/path_redditTitle_${q}_l${l}" -o "plots/Fig9f_path_redditTitle_${q}_l${l}" -t "Reddit Title ${q}, ${l}-Path" -l $l
L_LIST=(2 3 4 5 6 7 8 9 10)
k=1000
./plot_length.py -a "${ALG_LIST[@]}" -i "outputs/path_redditTitle_${q}" -l "${L_LIST[@]}" -k $k -o "plots/Fig9g_path_redditTitle_${q}" -t "Reddit Title ${q}"
