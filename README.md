# Any-k: Ranked enumeration for join queries

[![Any-k](https://img.shields.io/badge/Anyk-Project-blue.svg)](https://northeastern-datalab.github.io/anyk/)
[![VLDB](https://img.shields.io/badge/VLDB-2020-blue.svg)](https://dl.acm.org/doi/abs/10.14778/3397230.3397250)
[![Paper1](http://img.shields.io/badge/arXiv1-1911.05582-blue.svg)](https://arxiv.org/abs/1911.05582)
[![Paper2](http://img.shields.io/badge/arXiv2-2101.12158-blue.svg)](https://arxiv.org/abs/2101.12158)
[![License](https://img.shields.io/badge/License-Apache%202.0-orange.svg)](https://opensource.org/licenses/Apache-2.0)



This repo provides an implementation of the any-k framework for ranked enumeration of the answers to a join query over a relational database.
More generally, the code can easily be extended to obtain ranked enumeration for any problem solvable via Dynamic Programming.
Also included are scripts to reproduce the experiments from our VLDB 2020 and VLDB 2021 papers:
[*Optimal Algorithms for Ranked Enumeration of Answers to Full Conjunctive Queries*](https://dl.acm.org/doi/abs/10.14778/3397230.3397250)
and
[*Beyond Equi-joins: Ranking, Enumeration and Factorization*](https://dl.acm.org/doi/10.14778/3476249.3476306).


**Overview of Any-k:** 
Given a relational database with weighted tuples, a join query, and a ranking function, ranked enumeration returns the query answers incrementally in the order of their importance, specified by the ranking function.
For more information, please visit the [project website](https://northeastern-datalab.github.io/anyk/). You can also watch the video presented at VLDB 2020:

[![Watch the video](https://img.youtube.com/vi/nw4XiaOnavE/0.jpg)](https://www.youtube.com/watch?v=nw4XiaOnavE&list=PL_72ERGKF6DR4R0Cowx-LnnnqLXRf4ZjB)

or the third part of our [SIGMOD 2020 tutorial](https://northeastern-datalab.github.io/topk-join-tutorial/):

[![Watch the video](https://img.youtube.com/vi/epvkyXBWefs/0.jpg)](https://www.youtube.com/watch?list=PL_72ERGKF6DTTD6T5oR4WQPuCyHZd7x_N&v=epvkyXBWefs)

or the sixth part of our [ICDE 2022 tutorial](https://northeastern-datalab.github.io/responsive-dbms-tutorial/):

[![Watch the video](https://img.youtube.com/vi/ao7kXi55Y94/0.jpg)](https://www.youtube.com/watch?v=ao7kXi55Y94&list=PL_72ERGKF6DTInW_P3a9zTYPSNLwbqOAx&ab_channel=DATALabNortheastern)


## Programming Language and Dependencies
The source code is written in Java. The current version is tested on version 8. To install it in a Debian/Ubuntu system, you can use:
```
sudo apt-get update
sudo apt-get install openjdk-8-jdk
export JAVA_HOME=path_to_java_home
```
The project compiles with the [Maven](https://maven.apache.org/index.html) package manager.
To run the bundled experiments, several scripts need a working version of Python 2. We recommend using [Anaconda](https://docs.anaconda.com/anaconda/install/) to create an environment with all the required packages in [`Experiments/environment.yml`](/Experiments/environment.yml):
```
conda env create -f Experiments/environment.yml
conda activate anyk_env
```

## Implementation Details

Currently, the implementation is for in-memory computation: we assume that the input and the data structures we create fit in main memory. If that is not the case, the program will terminate with an out-of-memory exception.

Directory `doc/` contains documentation of classes and methods generated by Javadoc in HTML format. 

The code is written in a way such that it is very easily extendable to other Dynamic Programming (DP) problems, making them any-k. This is done by extending the classes found in `paths/` packages. Specifically, the abstract class `DP_Problem_Instance` can be instantiated for "your own" DP problem by specifying how the bottom-up phase looks like. Then the rest of the code solves ranked enumeration for the problem. For DP problems that have a tree structure (Tree-DP), such as acyclic CQs, this is done with the `trees/` packages. For cyclic queries, `cycles/` contains methods for decomposing a simple cycle into a union of acyclic queries.


## Compilation
To compile, navigate to the root directory of the project and run:
```
mvn package
```
Successful comilation will produce a jar file in `/target/` from which classes that implement a `main` function can be executed, e.g.,
```
java -cp target/any-k-1.0.jar entities.paths.DP_Path_Equijoin_Instance
```


## Reproducibility of Experiments
The repository contains detailed description for reproducing the experimental results reported in our research papers:
- VLDB 2020 : see [Experiments/VLDB20/README.md](/Experiments/VLDB20/README.md)
- VLDB 2021 : see [Experiments/VLDB21/README.md](/Experiments/VLDB21/README.md)


## Running on your own Data
The first step is to convert the data into a format that is recognized by the parser. The expected input format is:

```
Relation [RelationName]
[Attribute1] [Attribute2] [Attribute3] ...
[val1] [val2] [val3]
[val1'] [val2'] [val3']
...
End of [RelationName]
Relation [RelationName']
...
```
You can generate an example synthetic file by running the synthetic data generator:
```
java -cp target/any-k-1.0.jar data.BinaryRandomPattern -q "path" -n 200 -l 3 -dom 100 -o example.in
```
The above will create a 3-path instance with 3 binary relations of size 200, drawn unifromly at random from a domain of size 100. The input file will be saved as `example.in`. 

Next, you need to make sure that the join query you wish to run is currently supported by the code. These are:
* Equi-join path queries, including binary joins (via entities.paths.DP_Path_Equijoin_Instance)
* Equi-join star queries with binary relations (via entities.trees.TDP_BinaryStar_Equijoin_Instance)
* Equi-join simple cycles of binary relations (via entities.cycles.SimpleCycle_Equijoin_Query)
* Path queries with equality/inequality/non-equality/band predicates in DNF form (via entities.paths.DP_Path_ThetaJoin_Instance)

Finally, you need to write an appropriate java file that reads the input and then calls the appropriate methods. Please see the files under `src/main/java/experiments` for many examples. These files go through the following steps:
* Read certain parameters from the command line (not needed if you write your own)
* Read the input file and construct a representation that is understood by the program
* Specify the exact join conditions between the relations (e.g. which attributes join with which and with what type of predicate in the case of inequalities)
* Specify which column is used for ranking (sum-of-weights)
* Initialize a measurements object that keeps track of time and memory usage
* Run an any-k algorithm by calling "next" on an iterator object k times
* Write out the measurements to stdout

Instead of writing your own file, you can try to use the existing ones by setting the appropriate parameters from the command line. For example, to run the "Lazy" any-k algorithm on the previously generated example:
```
java -cp target/any-k-1.0.jar experiments.Path_Equijoin -a Lazy -i example.in -n 300 -l 3 -dom 100
```
Execute the class without parameters to get a list of the possible options.

#### Note
Setting the algorithm to UnrankedEnum in the command line arguments of experiments.Path_Equijoin will run an unranked enumeration algorithm on the instance. See the example in [src/main/java/experiments/Binary_Join_Unranked.java](src/main/java/experiments/Binary_Join_Unranked.java).

## License
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)


## Citation
If you use this code in your work, please cite: 
```bibtex
@article{TziavelisAGRY:2020,
  author = {Nikolaos Tziavelis and Deepak Ajwani and Wolfgang Gatterbauer and Mirek Riedewald and Xiaofeng Yang},
  title = {Optimal Algorithms for Ranked Enumeration of Answers to Full Conjunctive Queries},
  journal = {Proc. {VLDB} Endow.},
  volume = {13},
  number = {9},
  pages = {1582--1597},
  year = {2020},
  doi = {10.14778/3397230.3397250}
}
```
and/or
```bibtex
@article{TziavelisGR:2021,
  author    = {Nikolaos Tziavelis and Wolfgang Gatterbauer and Mirek Riedewald},
  title     = {Beyond Equi-joins: Ranking, Enumeration and Factorization},
  journal = {Proc. {VLDB} Endow.},
  volume = {14},
  number = {11},
  pages = {2599--2612},
  year = {2021},
  doi = {10.14778/3476249.3476306}
}
```

## Contact
[Nikos Tziavelis](https://ntzia.github.io/) (ntziavelis@gmail.com)
