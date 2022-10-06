# Reproducibility of VLDB 2020 Experiments

This page contains a detailed description on how to reproduce the experimental results reported 
in the VLDB 2020 paper titled 
[*Optimal Algorithms for Ranked Enumeration of Answers to Full Conjunctive Queries*](https://dl.acm.org/doi/abs/10.14778/3397230.3397250).

The results have been [*reproduced by the VLDB Reproducibility Committee*](http://vldb.org/pvldb/reproducibility/).


## Research Paper
The official paper is available in the 
[ACM Digital Library (https://dl.acm.org/doi/abs/10.14778/3397230.3397250)](https://dl.acm.org/doi/abs/10.14778/3397230.3397250). 
The full version is available on [arXiV.1911.05582](https://arxiv.org/abs/1911.05582). 
For citing our work, we suggest using the [DBLP bib file](https://dblp.uni-trier.de/rec/bibtex/journals/pvldb/TziavelisAGRY20).



## Code Version
We update the code frequently and future updates may interfere with the reproducibility of the experiments as they were conducted when the paper was written and reproduced by the VLDB committee. 
To get the most accurate results, please use the following version: 
```
git checkout 714393358f92c0b3357c7ce8cf5fcb12b396091e
```



## Programming Language and Dependencies
The source code is written in Java, tested on version 8. To install it in a Debian/Ubuntu system, you can use:
```
sudo apt-get update
sudo apt-get install openjdk-8-jdk
export JAVA_HOME=path_to_java_home
```
The project compiles with the [Maven](https://maven.apache.org/index.html) package manager.
For plotting and dataset preprocessing, several scripts need a working version of Python 2. We recommend using [Anaconda](https://docs.anaconda.com/anaconda/install/) to create an environment with all the required packages in [`../environment.yml`](https://github.com/northeastern-datalab/any-k-code/tree/master/Experiments/environment.yml):
```
conda env create -f ../environment.yml
conda activate anyk_env
```



## Compilation
To compile, navigate to the root directory of the project and run:
```
mvn package
```
Successful comilation will produce a jar file in `/target/` from which classes that implement a `main` function can be executed, e.g.
```
java -cp target/any-k-1.0.jar entities.paths.DP_Path_Equijoin_Instance
```



## Datasets Used

From here on, we assume that the current directory is `Experiments/VLDB20/`.

### Synthetic data generator: 

The produced jar contains generators for the synthetic data in the `data/` package. To generate a syntetic instance:

```
java -cp ../../target/any-k-1.0.jar data.BinaryRandomPattern -q "path" -n 200 -l 3 -dom 100 -o Synthetic_data/inputs/example.in
```

The above will create a 3-path instance with 3 binary relations of size 200, drawn unifromly at random from a domain of size 100. The input file will be saved in `Synthetic_data/inputs/`. 

The other class in the package (`Cycle_HeavyLightPattern`) creates a certain join pattern that is interesting for cyclic queries.

### Real dataset repositories:
* Twitter: Available [here](http://datasets.syr.edu/datasets/Twitter.html)
* BitcoinOTC: Available [here](https://snap.stanford.edu/data/soc-sign-bitcoin-otc.html)

```
cd Real_data/inputs
./create_input.sh
```
The above lines will download the real datasets in `Real_data/inputs/` and preprocess them. Note that `create_input.sh` has to be executed from its directory.



## Hardware Info   
Experiments were run on a PowerEdge R720 machine with the following specs:
- *Processor*: 2x Intel(R) Xeon(R) CPU E5-2643 0 @3.30GHz
- *Memory*: 128GB DDR-3 1600MHz (8x16) 

To run in a machine with less RAM, you can tweak `execution_parameters.sh` to reduce the size of the allocated VM memory. However, doing so might give out-of-memory errors or different execution times because of garbage collection. To avoid all out-of-memory errors for the currently specified inputs, you need a large amount of memory (>= 40GB). 

**Don't** specify more memory than 80%-90% of what your machine has, otherwise you will get errors and some annoying `hs_err_pid*.log` files. To run experiments on a smaller machine, consider reducing the input size.


## Repeating the Experiments
There are two directories, `Synthetic_data/` and `Real_data/`, which contain all the necessary scripts to get the input data, run the experiments, and then plot the figures. Each directory has an `inputs/` subdirectory that stores the input, `outputs/` that stores the raw timing measurements, and `plots/` that stores the figures. Additionally, there are `run_*` bash scripts that perform each experiment and also generate the data for the synthetic case. For the real datasets, `Real_data/create_input.sh` is first needed to download and preprocesses the data in a format readable by the program as described above. In both cases, a `do_plots.sh` script takes care of the plotting.

All that is automated by simply running `run_and_plot.sh` in the current directory. To more easily observe progress and potential error, it might be helpful to redirect stdout and stderr to a log file:
```
./run_and_plot.sh > execution.log 2>&1
```

Running all the iterations required to minimize the variance and obtain the same figures as in the paper will take many days. To get some quick results (in a couple of hours), fewer iterations can be run simply by uncommenting a line in `execution_parameters.sh` (`QUICK=true`). 
By default, only the experiments for queries of size 4 will be run. To run for other query sizes, uncomment the appropriate lines in `execution_parameters.sh`.
Also, consider using a tool like [screen](https://www.gnu.org/software/screen/) to let the process run in the background.

In case you want to restart fresh after an error, you can navigate to either directory (`Synthetic_data/` or `Real_data/`) and run `./clean.sh`. This will wipe all inputs, outputs, and figures.


## PostgreSQL configuration
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

