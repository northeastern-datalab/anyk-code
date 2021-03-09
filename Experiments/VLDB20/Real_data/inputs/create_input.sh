#!/bin/bash

# Download the BitcoinOTC dataset
wget https://snap.stanford.edu/data/soc-sign-bitcoinotc.csv.gz
gzip -d soc-sign-bitcoinotc.csv.gz -y
# Preprocess the BitcoinOTC dataset
./bitcoin-edit.py

# Preprocess the Twitter dataset
./twitter-sample.py
./twitter-edit.py "twitter_small"
./twitter-edit.py "twitter_large"
