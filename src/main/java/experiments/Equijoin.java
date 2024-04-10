package experiments;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import algorithms.Configuration;
import algorithms.Yannakakis;
import algorithms.YannakakisSorting;
import algorithms.paths.DP_All;
import algorithms.paths.DP_Anyk_Iterator;
import algorithms.paths.DP_Eager;
import algorithms.paths.DP_Lazy;
import algorithms.paths.DP_Quick;
import algorithms.paths.DP_QuickPlus;
import algorithms.paths.DP_Recursive;
import algorithms.paths.DP_Solution_Iterator;
import algorithms.paths.DP_Take2;
import algorithms.paths.Path_Batch;
import algorithms.paths.Path_BatchSorting;
import algorithms.trees.TDP_All;
import algorithms.trees.TDP_Anyk_Iterator;
import algorithms.trees.TDP_Eager;
import algorithms.trees.TDP_Lazy;
import algorithms.trees.TDP_Quick;
import algorithms.trees.TDP_QuickPlus;
import algorithms.trees.TDP_Recursive;
import algorithms.trees.TDP_Take2;
import algorithms.trees.Tree_Batch;
import algorithms.trees.Tree_BatchSorting;
import data.BinaryRandomPattern;
import data.Database_Query_Generator;
import entities.Join_Predicate;
import entities.Relation;
import entities.Tuple;
import entities.paths.DP_Path_Equijoin_Instance;
import entities.paths.DP_Solution;
import entities.paths.Path_Equijoin_Query;
import entities.trees.TDP_Solution;
import entities.trees.TDP_Thetajoin_Instance;
import entities.trees.Tree_ThetaJoin_Query;
import util.DatabaseParser;
import util.Measurements;

/**
 * This class provides a main function that receives execution parameters from
 * the command line
 * and runs experiments for equi-join queries.
 * All measurements are written in standard output.
 * Call with no arguments to get a list of accepted options.
 * 
 * @author Nikolaos Tziavelis
 */
public class Equijoin {
    static Configuration conf = null;
    static Path_Equijoin_Query path_query = null;
    static Tree_ThetaJoin_Query tree_query = null;
    static int max_k = -1;
    static String algorithm;
    static Measurements measurements = null;
    static int warmup_iters, run_iters;

    public static void run(String query_type) {
        if (algorithm.equals("Count")) {
            if (query_type.equals("tree")) {
                TDP_Thetajoin_Instance instance = new TDP_Thetajoin_Instance(tree_query, null);
                System.out.println("Number_of_Results = " + instance.count_solutions());
            } else {
                DP_Path_Equijoin_Instance instance = new DP_Path_Equijoin_Instance(path_query);
                System.out.println("Number_of_Results = " + instance.count_solutions());
            }
            return;
        }
        if (algorithm.equals("Boolean")) {
            if (query_type.equals("tree")) {
                System.err.println("Algorithm not currently supported for tree queries");
                System.exit(1);
            }
            boolean res = path_query.boolean_query();
            measurements.add_single_output(res ? 1 : 0);
        } else if (algorithm.equals("Yannakakis")) {
            Yannakakis yann = null;
            if (query_type.equals("path")) {
                yann = new Yannakakis(path_query);
            } else if (query_type.equals("tree")) {
                yann = new Yannakakis(tree_query);
            } else {
                System.err.println("Query type not recognized");
                System.exit(1);
                return;
            }
            Tuple t;
            for (int k = 1; k <= max_k; k++) {
                t = yann.get_next();
                if (t == null)
                    break;
                else
                    measurements.add_k(t);
            }
        } else if (algorithm.equals("YannakakisSorting")) {
            YannakakisSorting yann = null;
            if (query_type.equals("path")) {
                yann = new YannakakisSorting(path_query);
            } else if (query_type.equals("tree")) {
                yann = new YannakakisSorting(tree_query);
            } else {
                System.err.println("Query type not recognized");
                System.exit(1);
                return;
            }
            Tuple t;
            for (int k = 1; k <= max_k; k++) {
                t = yann.get_next();
                if (t == null)
                    break;
                else
                    measurements.add_k(t);
            }
        } else if (algorithm.equals("UnrankedEnum")) {
            if (query_type.equals("tree")) {
                System.err.println("Algorithm not currently supported for tree queries");
                System.exit(1);
            }
            DP_Path_Equijoin_Instance instance = new DP_Path_Equijoin_Instance(path_query);
            DP_Solution_Iterator iter_unranked = new DP_Solution_Iterator(instance);
            DP_Solution solution;
            for (int k = 1; k <= max_k; k++) {
                solution = iter_unranked.get_next();
                if (solution == null)
                    break;
                else
                    measurements.add_k(solution.solutionToTuples());
            }
        } else {
            if (query_type.equals("path")) {
                DP_Path_Equijoin_Instance instance = new DP_Path_Equijoin_Instance(path_query);
                instance.bottom_up();

                // Return the first result in a uniform way (DP) for all algorithms
                int k = 1;
                if (!algorithm.equals("Batch") && !algorithm.equals("BatchSorting")) {
                    measurements.add_k(instance.get_best_solution());
                    k += 1;
                }

                DP_Anyk_Iterator iter = null;
                // Run any-k
                if (algorithm.equals("Eager"))
                    iter = new DP_Eager(instance, conf);
                else if (algorithm.equals("All"))
                    iter = new DP_All(instance, conf);
                else if (algorithm.equals("Take2"))
                    iter = new DP_Take2(instance, conf);
                else if (algorithm.equals("Lazy"))
                    iter = new DP_Lazy(instance, conf);
                else if (algorithm.equals("Quick"))
                    iter = new DP_Quick(instance, conf);
                else if (algorithm.equals("QuickPlus"))
                    iter = new DP_QuickPlus(instance, conf);
                else if (algorithm.equals("Recursive"))
                    iter = new DP_Recursive(instance, conf);
                else if (algorithm.equals("BatchSorting"))
                    iter = new Path_BatchSorting(instance, conf);
                else if (algorithm.equals("Batch"))
                    iter = new Path_Batch(instance, conf);
                else {
                    System.err.println("Algorithm not recognized.");
                    System.exit(1);
                }

                DP_Solution solution;
                if (k == 2)
                    iter.get_next();

                for (; k <= max_k; k++) {
                    solution = iter.get_next();
                    if (solution == null)
                        break;
                    else
                        measurements.add_k(solution.solutionToTuples());
                }
            } else if (query_type.equals("tree")) {
                TDP_Thetajoin_Instance instance = new TDP_Thetajoin_Instance(tree_query, null);
                instance.bottom_up();

                // Return the first result in a uniform way (DP) for all algorithms
                int k = 1;
                if (!algorithm.equals("Batch") && !algorithm.equals("BatchSorting")) {
                    measurements.add_k(instance.get_best_solution());
                    k += 1;
                }

                TDP_Anyk_Iterator iter = null;
                // Run any-k
                if (algorithm.equals("Eager"))
                    iter = new TDP_Eager(instance, conf);
                else if (algorithm.equals("All"))
                    iter = new TDP_All(instance, conf);
                else if (algorithm.equals("Take2"))
                    iter = new TDP_Take2(instance, conf);
                else if (algorithm.equals("Lazy"))
                    iter = new TDP_Lazy(instance, conf);
                else if (algorithm.equals("Quick"))
                    iter = new TDP_Quick(instance, conf);
                else if (algorithm.equals("QuickPlus"))
                    iter = new TDP_QuickPlus(instance, conf);
                else if (algorithm.equals("Recursive"))
                    iter = new TDP_Recursive(instance, conf);
                else if (algorithm.equals("BatchSorting"))
                    iter = new Tree_BatchSorting(instance, conf);
                else if (algorithm.equals("Batch"))
                    iter = new Tree_Batch(instance, conf);
                else {
                    System.err.println("Algorithm not recognized.");
                    System.exit(1);
                }

                TDP_Solution solution;

                if (k == 2)
                    iter.get_next();

                for (; k <= max_k; k++) {
                    solution = iter.get_next();
                    if (solution == null)
                        break;
                    else
                        measurements.add_k(solution.solutionToTuples());
                }
            }
        }
    }

    /**
     * Utility method for help messages.
     * 
     * @return String
     */
    public static String getName() {
        String className = Thread.currentThread().getStackTrace()[2].getClassName();
        return className;
    }

    public static void main(String args[]) {
        // ======= Parse the command line =======
        Options options = new Options();

        Option input_option = new Option("i", "input", true, "path of input file");
        input_option.setRequired(false);
        options.addOption(input_option);

        Option query_option = new Option("q", "query", true, "query type (path/star/one_branch");
        query_option.setRequired(true);
        options.addOption(query_option);

        Option alg_option = new Option("a", "algorithm", true, "any-k algorithm to run");
        alg_option.setRequired(true);
        options.addOption(alg_option);

        Option k_option = new Option("k", "numOfResults", true, "run until the top-k'th result is returned");
        k_option.setRequired(false);
        options.addOption(k_option);

        Option sj_option = new Option("sj", "selfJoin", false,
                "set if the input file has only one relation and the query is a self-join");
        sj_option.setRequired(false);
        options.addOption(sj_option);

        Option ar_option = new Option("ar", "relationArity", true, "2 for binary relations, etc.");
        ar_option.setRequired(false);
        options.addOption(ar_option);

        Option downsample_option = new Option("ds", "downsample", false,
                "print only a sample of k times so that their total is < 500 + 1 + 1");
        downsample_option.setRequired(false);
        options.addOption(downsample_option);

        Option heap_type_option = new Option("ht", "heap type", true, "type of heap to use");
        heap_type_option.setRequired(false);
        options.addOption(heap_type_option);

        Option warmup_iter_option = new Option("wi", "warmup iterations", true,
                "how many iterations to run without recording time");
        warmup_iter_option.setRequired(false);
        options.addOption(warmup_iter_option);

        Option run_iter_option = new Option("ri", "run iterations", true,
                "how many iterations to run with recording time");
        run_iter_option.setRequired(false);
        options.addOption(run_iter_option);

        // This option is needed when calling without an input file
        Option n_option = new Option("n", "relationSize", true, "number of tuples per relation");
        n_option.setRequired(false);
        options.addOption(n_option);

        // This option is needed when calling without an input file
        Option l_option = new Option("l", "relationNo", true, "number of relations");
        l_option.setRequired(false);
        options.addOption(l_option);

        // This option is needed when calling without an input file
        Option dom_option = new Option("dom", "domain", true, "number of joining tuples per tuple (outDegree)");
        dom_option.setRequired(false);
        options.addOption(dom_option);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            formatter.printHelp(getName(), options);
            System.exit(1);
        }

        // ======= Initialize parameters =======
        int n, l = -1, arity, domain, sample_rate;
        String input_file = null;
        String query = cmd.getOptionValue("query");
        if (cmd.hasOption("relationArity"))
            arity = Integer.parseInt(cmd.getOptionValue("relationArity"));
        else
            arity = 2;
        boolean self_join = false;
        if (cmd.hasOption("selfJoin"))
            self_join = true;
        algorithm = cmd.getOptionValue("algorithm");
        if (cmd.hasOption("numOfResults"))
            max_k = Integer.parseInt(cmd.getOptionValue("numOfResults"));
        else
            max_k = Integer.MAX_VALUE;
        if (cmd.hasOption("downsample")) {
            long estimated_result_size = 0;
            // in case -k has been set, we know the output size
            if (cmd.hasOption("numOfResults"))
                estimated_result_size = Integer.parseInt(cmd.getOptionValue("numOfResults"));
            else if (cmd.hasOption("relationSize") && cmd.hasOption("relationNo") && cmd.hasOption("domain")) {
                n = Integer.parseInt(cmd.getOptionValue("relationSize"));
                l = Integer.parseInt(cmd.getOptionValue("relationNo"));
                domain = Integer.parseInt(cmd.getOptionValue("domain"));
                double average_connections = n * 1.0 / domain;
                estimated_result_size = n * (long) Math.pow(average_connections, l - 1);
                System.out.println("estimated_result_size: " + estimated_result_size);
            }
            // Hardcode some real queries
            else if (cmd.hasOption("input") && cmd.getOptionValue("input").contains("bitcoinotc")
                    && cmd.getOptionValue("relationNo").equals("3")) {
                estimated_result_size = 8 * 10_000_000L;
            } else if (cmd.hasOption("input") && cmd.getOptionValue("input").contains("bitcoinotc_sample_210")
                    && cmd.getOptionValue("relationNo").equals("4")) {
                estimated_result_size = 10_000_000L;
            } else if (cmd.hasOption("input") && cmd.getOptionValue("input").contains("bitcoinotc_sample_211")
                    && cmd.getOptionValue("relationNo").equals("4")) {
                estimated_result_size = 400_000_000L;
            } else if (cmd.hasOption("input") && cmd.getOptionValue("input").contains("bitcoinotc_sample_26")
                    && cmd.getOptionValue("relationNo").equals("6")) {
                estimated_result_size = 31_300_000L;
            } else if (cmd.hasOption("input") && cmd.getOptionValue("input").contains("bitcoinotc")
                    && cmd.getOptionValue("relationNo").equals("4")) {
                estimated_result_size = 4 * 10_000_000_000L;
            } else if (cmd.hasOption("input") && cmd.getOptionValue("input").contains("twitter_sample_210")
                    && cmd.getOptionValue("relationNo").equals("3")) {
                estimated_result_size = 2_200_000L;
            } else if (cmd.hasOption("input") && cmd.getOptionValue("input").contains("twitter_sample_211")
                    && cmd.getOptionValue("relationNo").equals("3")) {
                estimated_result_size = 9_100_000L;
            } else if (cmd.hasOption("input") && cmd.getOptionValue("input").contains("twitter_sample_28")
                    && cmd.getOptionValue("relationNo").equals("4")) {
                estimated_result_size = 1_500_000L;
            } else if (cmd.hasOption("input") && cmd.getOptionValue("input").contains("twitter_sample_29")
                    && cmd.getOptionValue("relationNo").equals("4")) {
                estimated_result_size = 43_000_000L;
            } else if (cmd.hasOption("input") && cmd.getOptionValue("input").contains("twitter_sample_27")
                    && cmd.getOptionValue("relationNo").equals("6")) {
                estimated_result_size = 1_200_000L;
            } else if (cmd.hasOption("input") && cmd.getOptionValue("input").contains("friendship")
                    && cmd.getOptionValue("relationNo").equals("3")) {
                estimated_result_size = 460_000L;
            } else if (cmd.hasOption("input") && cmd.getOptionValue("input").contains("friendship")
                    && cmd.getOptionValue("relationNo").equals("4")) {
                estimated_result_size = 3_000_000L;
            } else if (cmd.hasOption("input") && cmd.getOptionValue("input").contains("friendship")
                    && cmd.getOptionValue("relationNo").equals("5")) {
                estimated_result_size = 20_000_000L;
            } else if (cmd.hasOption("input") && cmd.getOptionValue("input").contains("friendship")
                    && cmd.getOptionValue("relationNo").equals("6")) {
                estimated_result_size = 131_000_000L;
            } else if (cmd.hasOption("input") && cmd.getOptionValue("input").contains("foodweb")
                    && cmd.getOptionValue("relationNo").equals("3")) {
                estimated_result_size = 260_000L;
            } else if (cmd.hasOption("input") && cmd.getOptionValue("input").contains("foodweb")
                    && cmd.getOptionValue("relationNo").equals("4")) {
                estimated_result_size = 2_700_000L;
            } else if (cmd.hasOption("input") && cmd.getOptionValue("input").contains("foodweb")
                    && cmd.getOptionValue("relationNo").equals("5")) {
                estimated_result_size = 31_100_000L;
            } else {
                System.err.println("Need to know the approximate output size to run with downsampling");
                System.exit(1);
            }
            sample_rate = (int) Math.ceil(estimated_result_size / 500.0);
        } else
            sample_rate = 1;
        System.out.println("sample_rate: " + sample_rate);
        if (cmd.hasOption("heap type"))
            conf.set_heap_type(cmd.getOptionValue("heap type"));
        conf = new Configuration();
        if (cmd.hasOption("warmup iterations"))
            warmup_iters = Integer.parseInt(cmd.getOptionValue("warmup iterations"));
        else
            warmup_iters = 0;
        if (cmd.hasOption("run iterations"))
            run_iters = Integer.parseInt(cmd.getOptionValue("run iterations"));
        else
            run_iters = 1;

        // ======= Read the input =======
        List<Relation> database = null;
        if (cmd.hasOption("input")) {
            // Read file and build database
            input_file = cmd.getOptionValue("input");
            DatabaseParser db_parser = new DatabaseParser(null);
            database = db_parser.parse_file(input_file);
            if (self_join) {
                // In the case of a self-join, we join l times a single relation
                if (database.size() != 1) {
                    System.err.println("Specified self-join but input file does not contain exactly one relation");
                    System.exit(1);
                }
                if (cmd.hasOption("relationNo")) {
                    l = Integer.parseInt(cmd.getOptionValue("relationNo"));
                } else {
                    System.err.println("Need -l to run a self-join");
                    System.exit(1);
                }
                // Add the same relation as many times as needed
                if (cmd.hasOption("input") && cmd.getOptionValue("input").contains("citations")) {
                    DatabaseParser db_parser2 = new DatabaseParser(3);
                    List<Relation> database2 = db_parser2.parse_file(input_file);
                    for (int i = 1; i < l; i++) {
                        database.add(database2.get(0));
                    }
                } else {
                    for (int i = 1; i < l; i++)
                        database.add(database.get(0));
                }

            }
        } else {
            // No input file specified, generate input according to the parameters
            if (cmd.hasOption("relationSize") && cmd.hasOption("relationNo")) {
                n = Integer.parseInt(cmd.getOptionValue("relationSize"));
                l = Integer.parseInt(cmd.getOptionValue("relationNo"));
                if (cmd.hasOption("domain"))
                    domain = Integer.parseInt(cmd.getOptionValue("domain"));
                else
                    domain = (int) Math.floor(Math.sqrt(n));
                // Build database
                Database_Query_Generator gen = new BinaryRandomPattern(n, l, domain, "path");
                gen.create();
                database = gen.get_database();
            } else {
                System.err.println("Need -n and -l to run without an input file");
                System.exit(1);
            }
        }

        // Construct query
        if (query.equals("path")) {
            path_query = new Path_Equijoin_Query(database);
            if (cmd.hasOption("input") && cmd.getOptionValue("input").contains("citations"))
                path_query.set_join_conditions(new int[] { 1 }, new int[] { 0 });
            else
                path_query.set_join_conditions(new int[] { arity - 1 }, new int[] { 0 });
        } else if (query.equals("binary_star")) {
            path_query = new Path_Equijoin_Query(database);
            path_query.set_join_conditions(new int[] { 0 }, new int[] { 0 });
        } else {
            if (cmd.hasOption("relationNo"))
                l = Integer.parseInt(cmd.getOptionValue("relationNo"));
            else {
                System.err.println("Need -l to construct tree query");
                System.exit(1);
            }

            if (query.equals("star")) {
                tree_query = new Tree_ThetaJoin_Query();
                tree_query.add_to_tree_wConjunction(database.get(0), 0, -1, null);
                for (int j = 1; j < l; j++)
                    tree_query.add_to_tree_wConjunction(database.get(j), j, 0,
                            Arrays.asList(new Join_Predicate("E", 0, 0, null)));
            } else if (query.equals("one_branch")) {
                tree_query = new Tree_ThetaJoin_Query();
                tree_query.add_to_tree_wConjunction(database.get(0), 0, -1, null);
                for (int j = 1; j < l - 1; j++)
                    tree_query.add_to_tree_wConjunction(database.get(j), j, j - 1,
                            Arrays.asList(new Join_Predicate("E", 1, 0, null)));
                tree_query.add_to_tree_wConjunction(database.get(l - 1), l - 1, l - 3,
                        Arrays.asList(new Join_Predicate("E", 0, 0, null)));
            } else if (query.equals("UnaryCartesianProduct")) {
                path_query = new Path_Equijoin_Query(database);
                path_query.set_join_conditions(new int[] { 0 }, new int[] { 0 });
                query = "path";
            } else {
                System.err.println("Query not recognized");
                System.exit(1);
            }
        }

        // ======= Run =======
        measurements = new Measurements(sample_rate, max_k);
        for (int iter = 0; iter < warmup_iters + run_iters; iter++) {
            // Record measurements only after warm-up (throw away the previous ones)
            if (iter == warmup_iters) {
                measurements.consume();
                measurements = new Measurements(sample_rate, max_k);
            }
            // System.out.println("<<<<< Starting any-k warm-up");
            if (query.equals("path") || query.equals("binary_star"))
                run("path");
            else
                run("tree");
            measurements.start_new_run();
            // System.out.println(">>>>> Stopped any-k warm-up");
        }
        // Finalize and print everyting
        measurements.print();
    }
}