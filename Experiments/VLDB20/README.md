# Reproducibility of VLDB 2020 Experiments

This page contains a detailed description to reproduce the experimental results reported 
in the VLDB 2020 paper titled 
[*Optimal Algorithms for Ranked Enumeration of Answers to Full Conjunctive Queries*](https://dl.acm.org/doi/abs/10.14778/3397230.3397250).



### Research Paper
The official paper is available in the 
[ACM Digital Library (https://dl.acm.org/doi/abs/10.14778/3397230.3397250)](https://dl.acm.org/doi/abs/10.14778/3397230.3397250). 
The full version is available on [arXiV.1911.05582](https://arxiv.org/abs/1911.05582). 
For citing our work, we suggest using the [DBLP bib file](https://dblp.uni-trier.de/rec/bibtex/journals/pvldb/TziavelisAGRY20).



### Programming Language and Dependencies
The source code is written in Java, tested on version 8. To install it in a Debian/Ubuntu system, you can use:
```
sudo apt-get update
sudo apt-get install openjdk-8-jdk
export JAVA_HOME=path_to_java_home
```
The project compiles with the [Maven](https://maven.apache.org/index.html) package manager.
For plotting and dataset preprocessing, several scripts need a working version of Python 2. We recommend using [Anaconda](https://docs.anaconda.com/anaconda/install/) to create an environment with all the required packages in [`../environment.yml`](https://github.com/northeastern-datalab/any-k-code/tree/master/Experiments/environment.yml):
```
conda env create -f environment.yml
conda activate anyk_env
```



### Compilation
To compile, navigate to the root directory of the project and run:
```
mvn package
```
Successful comilation will produce a jar file in `/target/` from which a class can be executed, e.g.
```
java -cp target/any-k-1.0.jar entities.paths.DP_Path_Equijoin_Instance
```



### Datasets Used
**Synthetic data generator**: 
The produced jar contains the generators for the synthetic data experiments in the `data/` package. 

**Real datasets Repository**: 
The `Real_data/` experiments assume the .csv files to be in the corresponding `Real_data/inputs/` directory. Running `Real_data/inputs/create_input.sh` will download and preprocess them.
* Twitter: Available [here](https://snap.stanford.edu/data/soc-sign-bitcoin-otc.html)
* BitcoinOTC: Available [here](http://datasets.syr.edu/datasets/Twitter.html)



### Hardware Info   
Experiments were run on a PowerEdge R720 machine with the following specs:
- *Processor*: 2x Intel(R) Xeon(R) CPU E5-2643 0 @3.30GHz
- *Memory*: 128GB DDR-3 1600MHz (8x16) 

To run in a machine with less RAM, you can tweak `execution_parameters.sh` to reduce the size of the allocated VM memory. However, doing so might give out-of-memory errors or different timing measurements because of garbage collection.



### PostgreSQL configuration
To reproduce the experiments on PostgreSQL, you first need a working version of the system. The version that has been tested is 9.5.20.
First, create a psql role and database matching your operating system user name ($USER).
Then, edit your postgresql.conf file (`psql -U $USER -c 'SHOW config_file'` to find its location) with the following parameters:

* shared_buffers = 32GB
* work_mem = 32GB
* bgwriter_lru_maxpages = 0
* bgwriter_delay = 10000ms
* fsync = off
* synchronous_commit = off
* full_page_writes = off
* checkpoint_timeout = 1h
* max_wal_size = 1000GB

After these edits, you need to reload the system parameters. Inside the psql environment, run:
```
SELECT pg_reload_conf();
```

To run the experiments, navigate to `Synthetic_data/psql` and use the `run_*` scripts (`run_and_plot.sh` will also execute those). To get the median timings, you can use `Synthetic_data/psql/report_median.py`.


### Repeating the Experiments
Directories `Synthetic_data` and `Real_data` contain `run_*` bash scripts that produce the synthetic data, run the experiments, and store the timing measurements in an `outputs/` directory. For the real datasets, you first need to execute `Real_data/create_input.sh` that preprocesses the data in a format readable by the program. Each directory also contains a `do_plots.sh` script that generates the figures of the paper in the `plots/` directory.

All that is automated by simply running `run_and_plot.sh` in the current directory.

Running all the iterations required to minimize the variance and obtain the same figures as in the paper will take many days. To get some quick results, fewer iterations can be run by appropriately modifying `execution_parameters.sh`.