
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;

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
import entities.Tuple;
import entities.paths.DP_Path_ThetaJoin_Instance;
import entities.paths.DP_Solution;
import entities.paths.Path_ThetaJoin_Query;
import entities.trees.TDP_Solution;
import entities.trees.TDP_Thetajoin_Instance;
import entities.trees.Tree_ThetaJoin_Query;
import query_parser.JsonOption;
import query_parser.JsonOptionParser;
import query_parser.JsonParserTree;
import util.Common;
import util.Measurements;
import util.OpenCsvWriter;

public class MainEntryPoint {
    public static String getName() {
        String className = Thread.currentThread().getStackTrace()[2].getClassName();
        return className;
    }

    public static void main(String args[]) throws IOException {
        Options options = new Options();

        Option query_file_opt = new Option("q", "query", true, "path to query file (json)");
        query_file_opt.setRequired(true);
        options.addOption(query_file_opt);

        Option param_file_opt = new Option("p", "parameters", true, "path to additional parameters file (json)");
        param_file_opt.setRequired(false);
        options.addOption(param_file_opt);

        // ==== The following parameters can alternatively be specified in the
        // param_file ====
        // ==== Setting them in the command line has priority over the param_file
        // ============

        Option result_file_path_opt = new Option("r", "result_output_file", true, "path to result file to be written");
        result_file_path_opt.setRequired(false);
        options.addOption(result_file_path_opt);

        Option timings_file_path_opt = new Option("t", "timings_output_file", true,
                "path to timings file to be written");
        timings_file_path_opt.setRequired(false);
        options.addOption(timings_file_path_opt);

        Option algorithm_opt = new Option("a", "algorithm", true, "algorithm to use");
        algorithm_opt.setRequired(false);
        options.addOption(algorithm_opt);

        Option max_k_opt = new Option("k", "max_k", true, "maximum number of answers to return");
        max_k_opt.setRequired(false);
        options.addOption(max_k_opt);

        Option weight_cutoff_opt = new Option("w", "weight_cutoff", true, "maximum weight of answers to return");
        weight_cutoff_opt.setRequired(false);
        options.addOption(weight_cutoff_opt);

        Option timing_frequency_opt = new Option("tf", "timing_frequency", true,
                "Record time every X answers returned");
        timing_frequency_opt.setRequired(false);
        options.addOption(timing_frequency_opt);

        Option timing_measurements_opt = new Option("tm", "timing_measurements", true,
                "How many time measurements to make (requires -ers)");
        timing_measurements_opt.setRequired(false);
        options.addOption(timing_measurements_opt);

        Option est_result_size_opt = new Option("ers", "estimated_result_size", true,
                "An estimate of how many answers there will be (needed for -tm)");
        timing_measurements_opt.setRequired(false);
        options.addOption(est_result_size_opt);

        Option factorization_method_opt = new Option("fm", "factorization_method", true,
                "factorization method to use for inequalities");
        factorization_method_opt.setRequired(false);
        options.addOption(factorization_method_opt);

        Option path_optimization_opt = new Option("po", "path_optimization", false,
                "turn on if query is a path for better performance");
        path_optimization_opt.setRequired(false);
        options.addOption(path_optimization_opt);

        // ===================================================================================
        // ===================================================================================

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

        // Parse query file (always required)
        String query_file_path = cmd.getOptionValue(query_file_opt);
        JsonParserTree treeParser = new JsonParserTree(query_file_path);
        Tree_ThetaJoin_Query tree_query = treeParser.parseQuery();

        // Parse parameters file
        JsonOption jsonOption = null;
        if (cmd.hasOption("parameters")) {
            String param_file_path = cmd.getOptionValue(param_file_opt);
            JsonOptionParser jsonOptionParser = new JsonOptionParser(param_file_path);
            jsonOption = jsonOptionParser.getJsonOption();
        }

        // Consolidate parameters
        String result_file_path = null;
        if (jsonOption != null)
            result_file_path = jsonOption.getResult_Output_File();
        if (cmd.hasOption("result_output_file"))
            result_file_path = cmd.getOptionValue("result_output_file");

        String timings_file_path = null;
        if (jsonOption != null)
            timings_file_path = jsonOption.getTimings_Output_File();
        if (cmd.hasOption("timings_output_file"))
            timings_file_path = cmd.getOptionValue("timings_output_file");

        Integer max_k = Integer.MAX_VALUE;
        if (jsonOption != null && jsonOption.getMax_k() != null)
            max_k = jsonOption.getMax_k();
        if (cmd.hasOption("max_k"))
            max_k = Integer.parseInt(cmd.getOptionValue("max_k"));

        Double weight_cutoff = Double.MAX_VALUE;
        if (jsonOption != null && jsonOption.getWeight_cutoff() != null)
            weight_cutoff = jsonOption.getWeight_cutoff();
        if (cmd.hasOption("weight_cutoff"))
            weight_cutoff = Double.parseDouble(cmd.getOptionValue("weight_cutoff"));

        String algorithm = "Quick";
        boolean alg_set_by_user = false;
        if (jsonOption != null && jsonOption.getAlgorithm() != null) {
            algorithm = jsonOption.getAlgorithm();
            alg_set_by_user = true;
        }
        if (cmd.hasOption("algorithm")) {
            algorithm = cmd.getOptionValue("algorithm");
            alg_set_by_user = true;
        }
        if (!alg_set_by_user)
            System.out.println(
                    "Algorithm not specified in parameters file or command line. Defaulting to Anyk-Part-Quick.");

        Integer timing_frequency = 1;
        Integer timing_measurements = -1;
        if (jsonOption != null && jsonOption.getTiming_measurements() != null)
            timing_measurements = jsonOption.getTiming_measurements();
        if (cmd.hasOption("timing_measurements"))
            timing_measurements = Integer.parseInt(cmd.getOptionValue("timing_measurements"));
        Integer estimated_result_size = -1;
        if (jsonOption != null && jsonOption.getEstimated_result_size() != null)
            estimated_result_size = jsonOption.getEstimated_result_size();
        if (cmd.hasOption("estimated_result_size"))
            estimated_result_size = Integer.parseInt(cmd.getOptionValue("estimated_result_size"));
        if (timing_measurements > 0 && estimated_result_size > 0) {
            timing_frequency = (int) Math.ceil(estimated_result_size * 1.0 / timing_measurements);
        }
        if (jsonOption != null && jsonOption.getTiming_frequency() != null)
            timing_frequency = jsonOption.getTiming_frequency();
        if (cmd.hasOption("timing_frequency"))
            timing_frequency = Integer.parseInt(cmd.getOptionValue("timing_frequency"));

        String factorization_method = null;
        if (jsonOption != null)
            factorization_method = jsonOption.getFactorization_method();
        if (cmd.hasOption("factorization_method"))
            factorization_method = cmd.getOptionValue("factorization_method");

        boolean path_optimization = false;
        if (jsonOption != null && jsonOption.getPath_optimization() != null) {
            if (jsonOption.getPath_optimization().equals("true"))
                path_optimization = true;
            else if (jsonOption.getPath_optimization().equals("false"))
                path_optimization = false;
            else {
                System.err.println("Invalid value for path_optimization in parameters file.");
                System.exit(1);
            }
        }
        if (cmd.hasOption("path_optimization"))
            path_optimization = true;

        Configuration conf = new Configuration();

        // ======= Run the query =======
        // Keep results in memory, then write to a file at the end to not include I/O in
        // elapsed time
        ArrayList<String> resultList = new ArrayList<>();

        Measurements measurements = new Measurements(timing_frequency, max_k);

        Path_ThetaJoin_Query path_query = null;
        if (path_optimization)
            path_query = new Path_ThetaJoin_Query(tree_query);

        if (algorithm.equals("Count")) {
            if (!path_optimization) {
                TDP_Thetajoin_Instance instance = new TDP_Thetajoin_Instance(tree_query, factorization_method);
                BigInteger query_answer_cnt = instance.count_solutions();
                measurements.add_single_output(query_answer_cnt.intValue());
                resultList.add(query_answer_cnt.toString());
            } else {
                DP_Path_ThetaJoin_Instance instance = new DP_Path_ThetaJoin_Instance(path_query, factorization_method);
                BigInteger query_answer_cnt = instance.count_solutions();
                measurements.add_single_output(query_answer_cnt.intValue());
                resultList.add(query_answer_cnt.toString());
            }
            return;
        }
        if (algorithm.equals("Boolean")) {
            if (!path_optimization) {
                System.err.println("Algorithm not currently supported for tree queries");
                System.exit(1);
            } else {
                // boolean res = path_query.boolean_query();
                // measurements.add_boolean(res);
            }
        }

        else if (algorithm.equals("Yannakakis")) {
            Yannakakis yann = new Yannakakis(tree_query);
            Tuple t;
            for (int k = 1; k <= max_k; k++) {
                t = yann.get_next();
                if (t == null)
                    break;
                if (result_file_path != null) {
                    resultList.add(Common.tuple_to_output_string(t));
                }
                if (timings_file_path != null)
                    measurements.add_k(t);
                if (t.cost > weight_cutoff)
                    break;
            }
        }

        else if (algorithm.equals("YannakakisSorting")) {
            YannakakisSorting yann = new YannakakisSorting(tree_query);
            Tuple t;
            for (int k = 1; k <= max_k; k++) {
                t = yann.get_next();
                if (t == null)
                    break;
                if (result_file_path != null) {
                    resultList.add(Common.tuple_to_output_string(t));
                }
                if (timings_file_path != null)
                    measurements.add_k(t);
                if (t.cost > weight_cutoff)
                    break;
            }
        }

        else if (algorithm.equals("UnrankedEnum")) {
            if (!path_optimization) {
                System.err.println("Algorithm not currently supported");
                System.exit(1);
            }
            // DP_Path_Equijoin_Instance instance = new
            // DP_Path_Equijoin_Instance(path_query);
            // DP_Solution_Iterator iter_unranked = new DP_Solution_Iterator(instance);
            // DP_Solution solution;
            // for (int k = 1; k <= max_k; k++) {
            // solution = iter_unranked.get_next();
            // if (solution == null)
            // break;
            // else
            // measurements.add_k(solution.solutionToTuples());
            // }
        }

        else {
            if (path_optimization) {
                DP_Path_ThetaJoin_Instance instance = new DP_Path_ThetaJoin_Instance(path_query, factorization_method);
                instance.bottom_up();

                // // Return the first result in a uniform way (DP) for all algorithms
                // int k = 1;
                // if (!algorithm.equals("Batch") && !algorithm.equals("BatchSorting")) {
                // measurements.add_k(instance.get_best_solution());
                // k += 1;
                // }

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
                for (int k = 1; k <= max_k; k++) {
                    solution = iter.get_next();
                    if (solution == null)
                        break;
                    if (result_file_path != null) {
                        resultList.add(Common.solution_to_output_string(solution));
                    }
                    if (timings_file_path != null)
                        measurements.add_k(solution.solutionToTuples());
                    if (solution.get_cost() > weight_cutoff)
                        break;
                }
            } else {
                TDP_Thetajoin_Instance instance = new TDP_Thetajoin_Instance(tree_query, factorization_method);
                instance.bottom_up();

                // Return the first result in a uniform way (DP) for all algorithms
                // int k = 1;
                // TDP_Solution solution;
                // if (!algorithm.equals("Batch") && !algorithm.equals("BatchSorting")) {
                // solution = instance.get_best_solution();
                // measurements.add_k(instance.get_best_solution());
                // k += 1;
                // }

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

                // if (k == 2)
                // iter.get_next();

                TDP_Solution solution;
                for (int k = 1; k <= max_k; k++) {
                    solution = iter.get_next();
                    if (solution == null)
                        break;
                    if (result_file_path != null) {
                        resultList.add(Common.solution_to_output_string(solution));
                    }
                    if (timings_file_path != null)
                        measurements.add_k(solution.solutionToTuples());
                    if (solution.get_cost() > weight_cutoff)
                        break;
                }
            }
        }

        // ======= Finalize and print everyting =======
        if (timings_file_path != null)
            measurements.print_to_csv_file(timings_file_path);
        if (result_file_path != null) {
            OpenCsvWriter openCsv = new OpenCsvWriter(result_file_path);
            for (String record : resultList)
                openCsv.writeLine(record);
            openCsv.flushWriter();
            openCsv.closeWriter();
        }
    }
}
