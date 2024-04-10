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
The source code is written in Java. The current version is tested on version 11. To install it in a Debian/Ubuntu system, you can use:
```
sudo apt-get update
sudo apt-get install openjdk-11-jdk
export JAVA_HOME=path_to_java_home
```
The project compiles with the [Maven](https://maven.apache.org/index.html) package manager.
To run the bundled experiments, several scripts need a working version of Python 2. We recommend using [Anaconda](https://docs.anaconda.com/anaconda/install/) to create an environment with all the required packages in [`Experiments/environment.yml`](/Experiments/environment.yml):
```
conda env create -f Experiments/environment.yml
conda activate anyk_env
```


## Compilation
To compile, navigate to the root directory of the project and run:
```
mvn package
```
Successful comilation will produce a jar file in `/target/` from which classes that implement a `main` function can be executed, e.g.,
```
java -cp target/any-k-1.0.jar entities.paths.DP_Path_Equijoin_Instance
```

## Limitations
1. The current version of the code does not support (arbitrary) cyclic queries.
To handle such a query, you need to decompose it into an acyclic query via a hypertree decomposition, which is not currently handled by the codebase, but may be integrated in the future.
2. The ranking function is assumed to be a sum of weights, one from each joining relation. The enumeration is always from the lowest weight to the largest weight (ascending).
Other ranking functions are not currently supported, but certain ones (e.g., lexicographic orders) are expressible by setting appropriate weights.
3. Certain algorithms or techniques may be restricted to certain queries 
(for example, the shared-ranges technique for inequality joins is currently restricted to path queries).
4. The code only works in-memory and will crash if enough RAM is not allocated. Make sure to use `-Xmx` and `-Xms` when you invoke the java program.


## Running on your own Queries and Data
Use the `MainEntryPoint` class to run on your own queries and data. See [examples/](/examples) for some helpful examples.
The query is specified as a join tree (in json format) where each joining relation can
refer to the same input file (a self-join) or a different file.
The execution can be parameterized by the following set of parameters,
provided in a different json file, or via the command line (has priority).

- `result_output_file`:  Path to file where the output tuples will be written. You can leave it empty if you only want to time the program.

- `timings_output_file`:  Path to file where timing information will be recorded. You can also leave it empty.

- `algorithm`: Has to be one of "Eager", "All", "Take2", "Lazy", "Quick", "QuickPlus", "Recursive", "BatchSorting", "Batch", "Yannakakis", "YannakakisSorting", "Count".

- `max_k`: Maximum number of output tuples to be produced.

- `weight_cutoff`: Instead of `max_k`, you can use this to stop the enumeration after a certain weight is exceeded in the output.

- `timing_frequency`: Useful if the query produces many answers and you want to restrict the number of timing measurements. If set to a number x, then time will only be recorded every x answers returned.

- `timing_measurements`: Similar to `timing_frequency`, but specifies the number of measurements instead. Has to be used in conjunction with `estimated_result_size`. Has lower priority than `timing_frequency`.

- `estimated_result_size`: An estimate for the number of query answers. Used to calculate `timing_frequency` if `timing_measurements` is used.

- `factorization_method`: This is only relevant for queries with inequality join conditions and controls the technique for handling those. Has to be one of "binary_part", "multi_part", "shared_ranges".

- `path_optimization`: If the query specified in the json file has a path structure, then turning this on may boost performance.

## Synthetic data generator

The produced jar contains a generator for synthetic data in the `data/` package. 
It creates ternary relations of size $n$, where the first two columns are intended for joins, while the third column encodes tuple weight.
The tuples of every relation are always distinct.
We parameterize our generator across three dimensions.
- We generate different join distributions by controlling the values that populate the first two columns. 
  - For a Uniform distribution, we draw integers in $[0 \ldots d]$ uniformly at random with replacement for some given value $d$, which defaults to $n / 10$.
  - For a Gaussian distribution, we round to integers the values drawn with a mean of $0$ and a given standard deviation, which defaults to $n / 10$.

- We generate different weight distributions.
  - For a Uniform distribution, we draw real numbers from $[0, w)$ where $w$ defaults to $10^4$.
  - For a Gaussian distribution, we take as input the mean and the standard deviation, which default to $0$ and $1$ respectively.
  - For a Lexicographic distribution, we ensure that the tuples of the first relation are always prioritized in the ranking and in the case of ties,
  the same happens with tuples of the second relation, and so on.

- We generate different query patterns by changing the names of the columns, assuming that two columns join if and only if they have the same name.
    Our generator supports path, star, and simple cycle patterns.

Example usage:

    java -cp target/any-k-1.0.jar data.BinaryRandomPattern -q "path" -n 200 -l 3 -dom 100 -w uniform -o Synthetic_data/inputs/example.in

The above will create a 3-path instance with 3 binary relations of size 200, drawn uniformly at random from a domain of size 100 and weights also drawn uniformly. The input file will be saved in `Synthetic_data/inputs/` (the directory needs to be created beforehand). 

To instead create a 4-star pattern with Gaussian domain values and a lexicographic ranking:

    java -cp target/any-k-1.0.jar data.BinaryGaussPattern -q "star" -n 200 -l 4 -w lex -o Synthetic_data/inputs/example.in

By default, the relations are all written in the same file, in a format used by some classes under `experiments`.
If instead you want to create a different file per relation use the `-mf` flag.
This will also remove headers and footers.

    java -cp target/any-k-1.0.jar data.BinaryRandomPattern -q "path" -n 200 -l 3 -dom 100 -w uniform -o example.csv -mf

The above will produce 3 different files: `example_1.in`, `example_2.csv`, `example_3.csv`


## Inequality-join to Equi-join Converter
The repository also contains tools to transform a join that may contain a DNF of join conditions to an equi-join over (polylogarithmically) larger relations.

Example Usage:

    java -cp target/any-k-1.0.jar factorization.Equijoin_Converter -i src/main/resources/reddit -q QR1 -l 3

The above takes as an input a small sample of the Reddit dataset and a 3-path query "QR1" that contains inequality predicates (defined in `src/main/java/factorization/Equijoin_Converter`),
and produces in the standard output new relations that join with equality predicates using new columns V1, V2, ...


## Reproducibility of Experiments
The repository contains detailed description for reproducing the experimental results reported in our research papers:
- VLDB 2020 : see [Experiments/VLDB20/README.md](/Experiments/VLDB20/README.md)
- VLDB 2021 : see [Experiments/VLDB21/README.md](/Experiments/VLDB21/README.md)



## Implementation Details

Directory `doc/` contains documentation of classes and methods generated by Javadoc in HTML format. 

The code is written in a way such that it is very easily extendable to other Dynamic Programming (DP) problems, making them any-k. This is done by extending the classes found in `paths/` packages. Specifically, the abstract class `DP_Problem_Instance` can be instantiated for "your own" DP problem by specifying how the bottom-up phase looks like. Then the rest of the code solves ranked enumeration for the problem. For DP problems that have a tree structure (Tree-DP), such as acyclic CQs, this is done with the `trees/` packages. For cyclic queries, `cycles/` contains methods for decomposing a simple cycle into a union of acyclic queries.

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
