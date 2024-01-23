# Reproducibility of VLDB 2021 Experiments

This page contains a detailed description on how to reproduce the experimental results reported 
in the VLDB 2021 paper titled 
[*Beyond Equi-joins: Ranking, Enumeration and Factorization*](https://dl.acm.org/doi/abs/10.14778/3476249.3476306).

The results have been [*reproduced by the VLDB Reproducibility Committee*](http://vldb.org/pvldb/reproducibility/).


## Research Paper
The official paper is available by
[VLDB (http://www.vldb.org/pvldb/vol14/p2599-tziavelis.pdf)](http://www.vldb.org/pvldb/vol14/p2599-tziavelis.pdf). 
The full version is available on [arXiV.2101.12158](https://arxiv.org/abs/2101.12158). 
For citing our work, we suggest using the [DBLP bib file](https://dblp.uni-trier.de/rec/bibtex/journals/pvldb/TziavelisGR21).


## Code Version
We update the code frequently and future updates may interfere with the reproducibility of the experiments as they were conducted when the paper was written and reproduced by the VLDB committee. 
To get the most accurate results, please use the following version: 
```
git checkout 35ef0702642ce8b97643242c0af0f9aa12cbbc58
```


## Hardware and OS Info   
The experiments were conducted on a PowerEdge R720 running Ubuntu Linux 20.04. The machine has the following specs:
- *Processor*: 2x Intel(R) Xeon(R) CPU E5-2643 @3.30GHz
- *Memory*: 128GB DDR-3 1600MHz (8x16) 

To run on a machine with less RAM, you can tweak `execution_parameters.sh` to reduce the size of the allocated VM memory. However, we cannot guarantee that the results will be the same in this case. Also, **don't** specify more memory than 80%-90% of what your machine has, otherwise you will get errors and some annoying `hs_err_pid*.log` files.


## Reproducing the Experiments
The recommended way to reproduce the experiments is with Docker. 
We provide a Dockerfile that recreates the environment and runs all the experiments when executed. 
Running a container in privileged mode minimizes the overhead of Docker.

Note that the paper contains a comparison with a commercial DBMS, anonymized as "System X".
These experiments are not made publicly available, please contact the authors if you are interested in them.


## Docker Details
1. Install [Docker](https://docs.docker.com/engine/install/).
The version we have tested is 20.10.

2. Build the docker image. This will compile the project and take care of all packages and dependencies. From the root directory of the project:
```
	docker build . -f Experiments/VLDB21/Dockerfile -t anyk_vldb21
```

3. Start a container. Upon starting, `start_container.sh` will be executing, configuring the database systems and starting the experiments. Note that the following command will also return the id of the container. 
```
	docker run -p 5432:5432 -itd --privileged anyk_vldb21
```

4. Running all experiments will take approximately a week. The container will stop when they finish and the plots will be found in `plots/` inside each of the four subdirectories. You can copy the results and plots from the container back to the host in a `docker_results` folder with: 
```
	docker cp <container_id>:/app/Experiments/VLDB21/ ./docker_results
```

Notes:
- If PostgreSQL is intalled in the host system, it has to be temporaily stopped since it will be started inside the container. This can be done with `sudo service postgresql stop`.
- To find the id of the container, use `docker ps`.
- To access the logs of the container, use `docker logs <container_id>`.
- To start a bash terminal on the container, use `docker exec -ti <container_id> bash`. The experiments will be running under a screen process.
- Container disk space is by default under /var/lib/docker. Make sure there is enough space there (a couple of GBs).


The following instructions provide more details on compilation, the environment and the execution of experiments that are only important if you do not follow the Docker method above.


## Programming Language and Dependencies
The source code is written in Java, tested on version 8. To install it in a Debian/Ubuntu system, you can use:
```
sudo apt-get update
sudo apt-get install openjdk-8-jdk
export JAVA_HOME=path_to_java_home
```
The project compiles with the [Maven](https://maven.apache.org/index.html) package manager.
For plotting and dataset preprocessing, several scripts need a working version of Python 2. We recommend using [Anaconda](https://docs.anaconda.com/anaconda/install/) to create an environment with all the required packages in [`../environment.yml`](/Experiments/environment.yml):
```
conda env create -f ../environment.yml
conda activate anyk_env
```
Please consult the `Dockerfile` for other packages that are required.



## Compilation
To compile, navigate to the root directory of the project and run:
```
mvn package
```
Successful comilation will produce a jar file in `/target/` from which classes that implement a `main` function can be executed, for example:
```
java -cp target/any-k-1.0.jar experiments.Path_Inequalities
```



## Datasets Used

From here on, we assume that the current directory is `Experiments/VLDB21/`.

### Synthetic data generator: 

The produced jar contains generators for the synthetic data in the `data/` package. To generate a syntetic instance:

```
java -cp ../../target/any-k-1.0.jar data.BinaryRandomPattern -q "path" -n 200 -l 3 -dom 100 -o Synthetic_data/inputs/example.in
```

The above will create a 3-path instance with 3 binary relations of size 200, drawn unifromly at random from a domain of size 100. The input file will be saved in `Synthetic_data/inputs/`. 

We also use the [TPC-H](https://www.tpc.org/tpch/) data generator. A binary is provided in this project in `TPCH/inputs/dbgen`.


### Real datasets:
* Reddit: Available [here](https://snap.stanford.edu/data/soc-redditHyperlinks-title.tsv)
* Birds dataset: Available [here](https://api.gbif.org/v1/occurrence/download/request/0113354-200613084148143.zip)

We preprocess them with scripts `Reddit_Temporal/inputs/preprocess_reddit.py` and `Birds_Oceania/inputs/preprocess_observations.py`
after downloading them to these directories.



## Running the Experiments
The script `run_and_plot.sh` runs all the experiments and plots the results, automating everything below. To more easily observe progress and potential error, it might be helpful to redirect stdout and stderr to a log file:
```
./run_and_plot.sh > execution.log 2>&1
```

There are four directories, which contain all the necessary scripts to get the input data, run the experiments, and then plot the figures. 
Each directory has an `inputs/` subdirectory that stores the input, 
`outputs/` that stores the raw timing measurements, 
and `plots/` that stores the figures. 
Additionally, there are `run_*` bash scripts that perform each experiment and also generate the data for the synthetic cases. 
For the real datasets, you need to first download and preprocesses them in a format readable by the program as described above. 
In both cases, a `do_plots.sh` script takes care of the plotting.

Consider using a tool like [screen](https://www.gnu.org/software/screen/) to let the process run in the background.

In case you want to restart fresh after an error, you can navigate to each subdirectory and run `./clean.sh`. This will wipe all inputs, outputs, and figures.


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

The complete configuration file is provided in `postgresql.conf`.

After editing the parameters, you need to reload the system parameters. Inside the psql environment, run:
```
SELECT pg_reload_conf();
```
