#!/bin/bash

# Download the BitcoinOTC dataset
wget https://snap.stanford.edu/data/soc-sign-bitcoinotc.csv.gz
gzip -d soc-sign-bitcoinotc.csv.gz -y
# Preprocess the BitcoinOTC dataset
./bitcoin-edit.py

# Download the Twitter dataset
wget http://datasets.syr.edu/uploads/1296759055/Twitter-dataset.zip
unzip -o Twitter\-dataset.zip
mv Twitter-dataset/data/edges.csv twitter.csv
# Preprocess the Twitter dataset
./twitter-sample.py
./twitter-edit.py "twitter_small"
./twitter-edit.py "twitter_large"
