package experiments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import algorithms.Configuration;
import algorithms.paths.DP_All;
import algorithms.paths.DP_Anyk_Iterator;
import algorithms.paths.DP_Eager;
import algorithms.paths.DP_Lazy;
import algorithms.paths.DP_Recursive;
import algorithms.paths.DP_Take2;
import algorithms.paths.Path_Batch;
import algorithms.paths.Path_BatchHeap;
import algorithms.paths.Path_BatchSorting;
import entities.Join_Predicate;
import entities.Relation;
import entities.paths.DP_Path_Equijoin_Instance;
import entities.paths.DP_Path_ThetaJoin_Instance;
import entities.paths.DP_Problem_Instance;
import entities.paths.DP_Solution;
import entities.paths.Path_Equijoin_Query;
import entities.paths.Path_Query_Solution;
import entities.paths.Path_ThetaJoin_Query;
import util.DatabaseParser;
import util.Measurements;

/** 
 * This class provides a main function that receives execution parameters from the command line
 * and runs experiments for join queries with inequalities (that include disjunctions)
 * where the joined relations are organized in a path.
 * All measurements are written in standard output.
 * Call with no arguments to get a list of accepted options.
 * @author Nikolaos Tziavelis
*/
public class Path_Inequalities_Disjunction
{
    private static Path_ThetaJoin_Query query_QT1D(List<Relation> database)
    {
        // Regular counting will fail for disjunctions because of the duplicate paths
        // Make the disjunction disjoint before counting (by introducing more conjunctions)
        Path_ThetaJoin_Query res = new Path_ThetaJoin_Query(database);

        List<List<Join_Predicate>> cond = new ArrayList<List<Join_Predicate>>();
        cond.add(Arrays.asList(
            new Join_Predicate("E", 2, 2, null),
            new Join_Predicate("IL", 4, 4, null),
            new Join_Predicate("IL", 6, 6, null)
        ));
        cond.add(Arrays.asList(
            new Join_Predicate("E", 2, 2, null),
            new Join_Predicate("IL", 4, 4, null),
            new Join_Predicate("IL", 7, 7, null)
        ));
        cond.add(Arrays.asList(
            new Join_Predicate("E", 2, 2, null),
            new Join_Predicate("IL", 4, 4, null),
            new Join_Predicate("IL", 8, 8, null)
        ));

        res.set_join_conditions_as_dnf(cond);
        return res; 
    }

    private static Path_ThetaJoin_Query query_QT1D_no_duplicates(List<Relation> database)
    {
        // Regular counting will fail for disjunctions because of the duplicate paths
        // Make the disjunction disjoint before counting (by introducing more conjunctions)
        Path_ThetaJoin_Query res = new Path_ThetaJoin_Query(database);

        List<List<Join_Predicate>> cond = new ArrayList<List<Join_Predicate>>();
        cond.add(Arrays.asList(
            new Join_Predicate("E", 2, 2, null),
            new Join_Predicate("IL", 4, 4, null),
            new Join_Predicate("IL", 6, 6, null)
        ));
        cond.add(Arrays.asList(
            new Join_Predicate("E", 2, 2, null),
            new Join_Predicate("IL", 4, 4, null),
            new Join_Predicate("IL", 7, 7, null),
            // Equal with greater than is not yet implemented
            // Subtract 1 from the "relation on the right" to get the same result for this query
            new Join_Predicate("IG", 6, 6, -1.0)
        ));
        cond.add(Arrays.asList(
            new Join_Predicate("E", 2, 2, null),
            new Join_Predicate("IL", 4, 4, null),
            new Join_Predicate("IL", 8, 8, null),
            // Equal with greater than is not yet implemented
            // Subtract 1 from the "relation on the right" to get the same result for this query
            new Join_Predicate("IG", 6, 6, -1.0),
            new Join_Predicate("IG", 7, 7, -1.0)
        ));

        res.set_join_conditions_as_dnf(cond);
        return res; 
    }

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
        Configuration conf = new Configuration();
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
        if (cmd.hasOption("heap type")) conf.set_heap_type(cmd.getOptionValue("heap type"));
        String factorization_method;
        if (cmd.hasOption("factorization method")) factorization_method = cmd.getOptionValue("factorization method");
        else factorization_method = null;   // let the classes choose by themselves




        // ======= Read the input =======
        DatabaseParser db_parser;
        List<Relation> database = null;

        // TPC-H
        if (query_type.equals("QT1D"))
        {
            int weight_attribute = 5;
            db_parser = new DatabaseParser(weight_attribute);
            database = db_parser.parse_file(input_file);
            // We have only one relation (lineitem)
            // Add the same relation as many times as needed to search for a chain of length l
            for (int i = 1; i < l; i++) database.add(database.get(0));
            query = query_QT1D(database);
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
            query = query_QT1D_no_duplicates(database);
            // Now run counting as usual
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
            query = query_QT1D_no_duplicates(database);
            long startTime = System.nanoTime();
            // Before starting the real clock, compute the entire result set
            instance = new DP_Path_ThetaJoin_Instance(query, factorization_method);
            Path_Batch batch = new Path_Batch(instance, conf);
            // Remove duplicates
            batch.all_solutions = new ArrayList<Path_Query_Solution>(new LinkedHashSet<Path_Query_Solution>(batch.all_solutions));
            double elapsedTime = (double) (System.nanoTime() - startTime) / 1_000_000_000.0;
            System.out.println("Not measured time for creating the query results: " + elapsedTime + " sec");
            // Now start the real clock
            measurements = new Measurements(sample_rate, max_k);
            // Time the sorting of the results
            iter = new Path_BatchSorting(batch);
        }
        else if (algorithm.equals("BatchHeap"))
        {
            query = query_QT1D_no_duplicates(database);
            long startTime = System.nanoTime();
            // Before starting the real clock, compute the entire result set
            instance = new DP_Path_ThetaJoin_Instance(query, factorization_method);
            Path_Batch batch = new Path_Batch(instance, conf);
            // Remove duplicates
            batch.all_solutions = new ArrayList<Path_Query_Solution>(new LinkedHashSet<Path_Query_Solution>(batch.all_solutions));
            double elapsedTime = (double) (System.nanoTime() - startTime) / 1_000_000_000.0;
            System.out.println("Not measured time for creating the query results: " + elapsedTime + " sec");
            // Now start the real clock
            measurements = new Measurements(sample_rate, max_k);
            // Time the construction of the PQ
            iter = new Path_BatchHeap(batch);
        }
        else if (algorithm.startsWith("QEq_"))
        {
            query = query_QT1D_no_duplicates(database);
            long startTime = System.nanoTime();
            // Before starting the clock, transform the query into a quadratic equijoin
            Path_Equijoin_Query quadratic_equijoin = query.to_Quadratic_Equijoin_with_duplicate_filter();

            double elapsedTime = (double) (System.nanoTime() - startTime) / 1_000_000_000.0;
            System.out.println("Not measured time for creating the quadratic relations: " + elapsedTime + " sec");
            // Now start thereal clock
            measurements = new Measurements(sample_rate, max_k);
            // Time the bottom-up phase of the new equijoin
            instance = new DP_Path_Equijoin_Instance(quadratic_equijoin);
            instance.bottom_up();

            if (algorithm.endsWith("Eager")) iter = new DP_Eager(instance, conf);
            else if (algorithm.endsWith("All")) iter = new DP_All(instance, conf);
            else if (algorithm.endsWith("Take2")) iter = new DP_Take2(instance, conf);
            else if (algorithm.endsWith("Lazy")) iter = new DP_Lazy(instance, conf);
            else if (algorithm.endsWith("Recursive")) iter = new DP_Recursive(instance, conf);
            else if (algorithm.endsWith("BatchSorting")) iter = new Path_BatchSorting(instance, conf);         
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

            if (algorithm.equals("Eager")) iter = new DP_Eager(instance, conf);
            else if (algorithm.equals("All")) iter = new DP_All(instance, conf);
            else if (algorithm.equals("Take2")) iter = new DP_Take2(instance, conf);
            else if (algorithm.equals("Lazy")) iter = new DP_Lazy(instance, conf);
            else if (algorithm.equals("Recursive")) iter = new DP_Recursive(instance, conf);       
            else
            {
                System.err.println("Any-k algorithm not recognized.");
                System.exit(1);
            }

            // For any-k, filter the duplicates on-the-fly
            DP_Solution solution;
            double prev_cost = Double.MIN_VALUE;
            HashSet<DP_Solution> hash = new HashSet<DP_Solution>(4);
            int total_iter_results = 0;
            int k;
            for (k = 1; k <= max_k; k++)
            {
                do 
                {
                    solution = iter.get_next();
                    total_iter_results += 1;
                }
                while (solution != null && hash.contains(solution));
                if (solution == null) break;
                if (solution.get_cost() > prev_cost + Double.MIN_NORMAL) hash = new HashSet<DP_Solution>(4);
                prev_cost = solution.get_cost();
                hash.add(solution);
                measurements.add_k(solution.solutionToTuples());
            }
            // Finalize and print everyting 
            measurements.print();    
            int duplicates_filtered = total_iter_results - k - 1;
            System.out.println("Duplicates filtered = " + duplicates_filtered);
            return;        
        }

        // Batch and QEq execute enumeration here without filtering
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