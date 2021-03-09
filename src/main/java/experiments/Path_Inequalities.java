package experiments;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import algorithms.paths.DP_All;
import algorithms.paths.DP_Anyk_Iterator;
import algorithms.paths.DP_Eager;
import algorithms.paths.DP_Lazy;
import algorithms.paths.DP_Recursive;
import algorithms.paths.DP_Take2;
import algorithms.paths.Path_Batch;
import algorithms.paths.Path_BatchSorting;
import entities.Join_Predicate;
import entities.Relation;
import entities.paths.DP_Path_Equijoin_Instance;
import entities.paths.DP_Path_ThetaJoin_Instance;
import entities.paths.DP_Problem_Instance;
import entities.paths.DP_Solution;
import entities.paths.Path_Equijoin_Query;
import entities.paths.Path_ThetaJoin_Query;
import util.DatabaseParser;
import util.Measurements;

/** 
 * This class provides a main function that receives execution parameters from the command line
 * and runs experiments for join queries with inequalities
 * where the joined relations are organized in a path.
 * All measurements are written in standard output.
 * Call with no arguments to get a list of accepted options.
 * @author Nikolaos Tziavelis
*/
public class Path_Inequalities
{
    
    /**
	 * Utility method for help messages.
     * @return String
	 */
    public static String getName()
    {
        String className = Thread.currentThread().getStackTrace()[2].getClassName(); 
        return className;
    }

    public static void main(String args[]) 
    {
        // ======= Parse the command line =======
        Options options = new Options();

        Option input_option = new Option("i", "input", true, "path of input file");
        input_option.setRequired(true);
        options.addOption(input_option);

        Option query_option = new Option("q", "query", true, "query type");
        query_option.setRequired(true);
        options.addOption(query_option);

        Option l_option = new Option("l", "pathLength", true, "length of paths that we are searching for");
        l_option.setRequired(true);
        options.addOption(l_option);

        Option alg_option = new Option("a", "algorithm", true, "algorithm to run");
        alg_option.setRequired(true);
        options.addOption(alg_option);

        Option k_option = new Option("k", "numOfResults", true, "run until the top-k'th result is returned");
        k_option.setRequired(false);
        options.addOption(k_option);

        Option downsample_option = new Option("ds", "downsample", false, "print only a sample of k times so that their total is < 1000 + 1 + 1");
        downsample_option.setRequired(false);
        options.addOption(downsample_option);

        Option fact_method_option = new Option("fm", "factorization method", true, "can be binary_part|multi_part|shared_ranges");
        fact_method_option.setRequired(false);
        options.addOption(fact_method_option);

        Option heap_type_option = new Option("ht", "heap type", true, "type of heap to use");
        heap_type_option.setRequired(false);
        options.addOption(heap_type_option);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;
        try
        {
            cmd = parser.parse(options, args);
        } 
        catch (ParseException e) 
        {
            System.err.println(e.getMessage());
            formatter.printHelp(getName(), options);
            System.exit(1);
        }



        // ======= Initialize parameters =======
        Path_ThetaJoin_Query query = null;
		DP_Problem_Instance instance;
        String input_file = cmd.getOptionValue("input");
        String query_type = cmd.getOptionValue("query");
        int l = Integer.parseInt(cmd.getOptionValue("pathLength"));
        String algorithm = cmd.getOptionValue("algorithm"); 
        int max_k;
        if (cmd.hasOption("numOfResults")) max_k = Integer.parseInt(cmd.getOptionValue("numOfResults")); 
        else max_k = Integer.MAX_VALUE;
        int sample_rate; 
        if (cmd.hasOption("downsample")) 
        {
            if (cmd.hasOption("numOfResults"))
                sample_rate = (int) Math.ceil(max_k / 1000.0);
            else sample_rate = 1000;
        }
        else
        {
            sample_rate = 1;
        }
        String heap_type;
        if (cmd.hasOption("heap type")) heap_type = cmd.getOptionValue("heap type");
        else heap_type = null;   // let the classes choose by themselves
        String factorization_method;
        if (cmd.hasOption("factorization method")) factorization_method = cmd.getOptionValue("factorization method");
        else factorization_method = null;   // let the classes choose by themselves




        // ======= Read the input =======
        DatabaseParser db_parser;
        List<Relation> database;
        List<Join_Predicate> ps;

        // Synthetic
        if (query_type.equals("SynQ1"))
        {
            input_file = cmd.getOptionValue("input");
            db_parser = new DatabaseParser(null);
            database = db_parser.parse_file(input_file);
            query = new Path_ThetaJoin_Query(database);
            ps = new ArrayList<Join_Predicate>();
            ps.add(new Join_Predicate("IL", 1, 0, null));
            query.set_join_conditions(ps);
        }
        else if (query_type.equals("SynQ2"))
        {
            input_file = cmd.getOptionValue("input");
            db_parser = new DatabaseParser(null);
            database = db_parser.parse_file(input_file);
            query = new Path_ThetaJoin_Query(database);
            ps = new ArrayList<Join_Predicate>();
            ps.add(new Join_Predicate("B", 1, 0, 50.0));
            ps.add(new Join_Predicate("N", 0, 1, null));
            query.set_join_conditions(ps);
        }
        else if (query_type.equals("Q1"))
        {
            int weight_attribute = 3;
            db_parser = new DatabaseParser(weight_attribute);
            database = db_parser.parse_file(input_file);
            // Our graphs have only one relation (edges)
            // Add the same relation as many times as needed to search for paths of length l
            for (int i = 1; i < l; i++) database.add(database.get(0));
            query = new Path_ThetaJoin_Query(database);
            ps = new ArrayList<Join_Predicate>();
            ps.add(new Join_Predicate("E", 1, 0, null));
            ps.add(new Join_Predicate("IL", 2, 2, null));
            query.set_join_conditions(ps);
        }
        else if (query_type.equals("Q2"))
        {
            int weight_attribute = 4;
            db_parser = new DatabaseParser(weight_attribute);
            database = db_parser.parse_file(input_file);
            // Our graphs have only one relation (edges)
            // Add the same relation as many times as needed to search for paths of length l
            for (int i = 1; i < l; i++) database.add(database.get(0));
            query = new Path_ThetaJoin_Query(database);
            ps = new ArrayList<Join_Predicate>();
            ps.add(new Join_Predicate("E", 1, 0, null));
            ps.add(new Join_Predicate("IL", 2, 2, null));
            ps.add(new Join_Predicate("IG", 3, 3, null));
            query.set_join_conditions(ps);
        }
        else if (query_type.equals("Q3"))
        {
            int weight_attribute = 5;
            db_parser = new DatabaseParser(weight_attribute);
            database = db_parser.parse_file(input_file);
            // Our graphs have only one relation (edges)
            // Add the same relation as many times as needed to search for paths of length l
            for (int i = 1; i < l; i++) database.add(database.get(0));
            query = new Path_ThetaJoin_Query(database);
            ps = new ArrayList<Join_Predicate>();
            ps.add(new Join_Predicate("E", 1, 0, null));
            ps.add(new Join_Predicate("IL", 2, 2, null));
            ps.add(new Join_Predicate("IG", 3, 3, null));
            query.set_join_conditions(ps);
        }
        else if (query_type.startsWith("Q4_"))
        {
            int weight_attribute = 3;
            double epsilon = Double.parseDouble(query_type.split("_")[1]) / 1000.0;
            db_parser = new DatabaseParser(weight_attribute);
            database = db_parser.parse_file(input_file);
            // Our graphs have only one relation (edges)
            // Add the same relation as many times as needed to search for paths of length l
            for (int i = 1; i < l; i++) database.add(database.get(0));
            query = new Path_ThetaJoin_Query(database);
            ps = new ArrayList<Join_Predicate>();
            ps.add(new Join_Predicate("B", 1, 1, epsilon));
            ps.add(new Join_Predicate("B", 2, 2, epsilon));
            query.set_join_conditions(ps);
        }
        else
        {
            System.err.println("Query type not recongnized!");
            System.exit(1);
        }



        // ======= Run =======
        DP_Anyk_Iterator iter = null;
        Measurements measurements = null;

        if (algorithm.equals("Count"))
        {
            instance = new DP_Path_ThetaJoin_Instance(query, factorization_method);
            System.out.println("Number_of_Results = " + instance.count_solutions());
            return;
        }
        else if (algorithm.equals("Mem"))
        {
            instance = new DP_Path_ThetaJoin_Instance(query, factorization_method);
            System.out.println("Graph_size = " + instance.graph_size());
            return;
        }
        else if (algorithm.equals("BatchSorting"))
        {
            long startTime = System.nanoTime();
            // Before starting the real clock, compute the entire result set
            instance = new DP_Path_ThetaJoin_Instance(query, factorization_method);
            Path_Batch batch = new Path_Batch(instance);
            double elapsedTime = (double) (System.nanoTime() - startTime) / 1_000_000_000.0;
            System.out.println("Not measured time for creating the query results: " + elapsedTime + " sec");
            // Now start the real clock
            measurements = new Measurements(sample_rate, max_k);
            // Time the sorting of the results
            iter = new Path_BatchSorting(batch);
        }
        else if (algorithm.startsWith("QEq_"))
        {
            long startTime = System.nanoTime();
            // Special case: if the new query only has 1 relation, 
            // then just use a PQ instead of running any-k DP
            if (query.length == 2)
            {
                // Before starting the real clock, compute the quadratic relations
                DP_Solution sol;
                // To avoid consuming more memory return the DP solutions of batch
                // instead of materializing a new relation
                DP_Path_ThetaJoin_Instance binary_dp_instance = new DP_Path_ThetaJoin_Instance(query, factorization_method);
                Path_Batch batch_alg = new Path_Batch(binary_dp_instance);
                // The solutions have been computed
                double elapsedTime = (double) (System.nanoTime() - startTime) / 1_000_000_000.0;
                System.out.println("Not measured time for creating the quadratic relations: " + elapsedTime + " sec");
                // Now start the real clock
                measurements = new Measurements(sample_rate, max_k);
                PriorityQueue<DP_Solution> pq = new PriorityQueue<DP_Solution>(batch_alg.all_solutions);
                for (int k = 1; k <= max_k; k++)
                {
                    sol = pq.poll();
                    if (sol == null) break;
                    else measurements.add_k(sol.solutionToTuples());
                }
                // Finalize and print everyting 
                measurements.print();
                return;
            }
            // Otherwise run any-k DP normally
            // Before starting the clock, transform the query into a quadratic equijoin
            Path_Equijoin_Query quadratic_equijoin = query.to_Quadratic_Equijoin();

            double elapsedTime = (double) (System.nanoTime() - startTime) / 1_000_000_000.0;
            System.out.println("Not measured time for creating the quadratic relations: " + elapsedTime + " sec");
            // Now start thereal clock
            measurements = new Measurements(sample_rate, max_k);
            // Time the bottom-up phase of the new equijoin
            instance = new DP_Path_Equijoin_Instance(quadratic_equijoin);
            instance.bottom_up();

            if (algorithm.endsWith("Eager")) iter = new DP_Eager(instance, heap_type);
            else if (algorithm.endsWith("All")) iter = new DP_All(instance, heap_type);
            else if (algorithm.endsWith("Take2")) iter = new DP_Take2(instance, heap_type);
            else if (algorithm.endsWith("Lazy")) iter = new DP_Lazy(instance, heap_type);
            else if (algorithm.endsWith("Recursive")) iter = new DP_Recursive(instance);
            else if (algorithm.endsWith("BatchSorting")) iter = new Path_BatchSorting(instance);         
            else
            {
                System.err.println("Any-k algorithm not recognized.");
                System.exit(1);
            }
        }
        else
        {
            // Start the clock
            measurements = new Measurements(sample_rate, max_k);
            // Run any-k on the theta-join query
            instance = new DP_Path_ThetaJoin_Instance(query, factorization_method);
            instance.bottom_up();

            if (algorithm.equals("Eager")) iter = new DP_Eager(instance, heap_type);
            else if (algorithm.equals("All")) iter = new DP_All(instance, heap_type);
            else if (algorithm.equals("Take2")) iter = new DP_Take2(instance, heap_type);
            else if (algorithm.equals("Lazy")) iter = new DP_Lazy(instance, heap_type);
            else if (algorithm.equals("Recursive")) iter = new DP_Recursive(instance);       
            else
            {
                System.err.println("Any-k algorithm not recognized.");
                System.exit(1);
            }
        }

        DP_Solution solution;
        for (int k = 1; k <= max_k; k++)
        {
            solution = iter.get_next();
            if (solution == null) break;
            else measurements.add_k(solution.solutionToTuples());
        }
        // Finalize and print everyting 
        measurements.print();
    }
}