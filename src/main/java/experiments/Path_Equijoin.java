package experiments;

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
import algorithms.paths.DP_Quick;
import algorithms.paths.DP_Recursive;
import algorithms.paths.DP_Take2;
import algorithms.paths.Path_Batch;
import algorithms.paths.Path_BatchSorting;
import data.BinaryRandomPattern;
import data.Database_Query_Generator;
import entities.Relation;
import entities.paths.DP_Path_Equijoin_Instance;
import entities.paths.DP_Solution;
import entities.paths.Path_Equijoin_Query;
import util.DatabaseParser;
import util.Measurements;

/** 
 * This class provides a main function that receives execution parameters from the command line
 * and runs experiments for equi-join queries where the joined relations are organized in a path.
 * All measurements are written in standard output.
 * Call with no arguments to get a list of accepted options.
 * @author Nikolaos Tziavelis
*/
public class Path_Equijoin
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
        input_option.setRequired(false);
        options.addOption(input_option);

        Option alg_option = new Option("a", "algorithm", true, "any-k algorithm to run");
        alg_option.setRequired(true);
        options.addOption(alg_option);

        Option k_option = new Option("k", "numOfResults", true, "run until the top-k'th result is returned");
        k_option.setRequired(false);
        options.addOption(k_option);

        Option sj_option = new Option("sj", "selfJoin", false, "set if the input file has only one relation and the query is a self-join");
        sj_option.setRequired(false);
        options.addOption(sj_option);

        Option ar_option = new Option("ar", "relationArity", true, "2 for binary relations, etc.");
        ar_option.setRequired(false);
        options.addOption(ar_option);

        Option downsample_option = new Option("ds", "downsample", false, "print only a sample of k times so that their total is < 500 + 1 + 1");
        downsample_option.setRequired(false);
        options.addOption(downsample_option);

        Option heap_type_option = new Option("ht", "heap type", true, "type of heap to use");
        heap_type_option.setRequired(false);
        options.addOption(heap_type_option);

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
        Path_Equijoin_Query query = null;
		DP_Path_Equijoin_Instance instance;
        String input_file;
        int n = -1;
        int l = -1;
        int domain = -1;
        int arity = -1;
        int max_k;
        int sample_rate = -2;
        if (cmd.hasOption("relationArity")) arity = Integer.parseInt(cmd.getOptionValue("relationArity"));
        else arity = 2;
        boolean self_join = false;
        if (cmd.hasOption("selfJoin")) self_join = true;
        String algorithm = cmd.getOptionValue("algorithm"); 
        if (cmd.hasOption("numOfResults")) max_k = Integer.parseInt(cmd.getOptionValue("numOfResults")); 
        else max_k = Integer.MAX_VALUE;
        if (cmd.hasOption("downsample")) 
        {
            long estimated_result_size = 0;
            // in case -k has been set, we know the output size	
            if (cmd.hasOption("numOfResults")) estimated_result_size = Integer.parseInt(cmd.getOptionValue("numOfResults"));
        	else if (cmd.hasOption("relationSize") && cmd.hasOption("relationNo") && cmd.hasOption("domain")) 
        	{
        		n = Integer.parseInt(cmd.getOptionValue("relationSize"));
        		l = Integer.parseInt(cmd.getOptionValue("relationNo"));
                domain = Integer.parseInt(cmd.getOptionValue("domain"));
                double average_connections = n * 1.0 / domain;
                estimated_result_size = n * (long) Math.pow(average_connections, l - 1);
        	}
        	else
        	{
                System.err.println("Need -n, -l and -d to run with downsampling");
                System.exit(1);  		
            }
            sample_rate = (int) Math.ceil(estimated_result_size / 500.0); 
        }
        else sample_rate = 1;
        if (cmd.hasOption("heap type")) conf.set_heap_type(cmd.getOptionValue("heap type"));



        // ======= Read the input =======
        if (cmd.hasOption("input")) 
        {
            // Read file and build database
            input_file = cmd.getOptionValue("input");
            DatabaseParser db_parser = new DatabaseParser(null);
            List<Relation> database = db_parser.parse_file(input_file);
            if (self_join)
            {
                // In the case of a self-join, we join l times a single relation
                if (cmd.hasOption("relationNo")) 
                {
                    l = Integer.parseInt(cmd.getOptionValue("relationNo"));
                }
                else
                {
                    System.err.println("Need -l to run a self-join");
                    System.exit(1);                    
                }
                // Construct query
                query = new Path_Equijoin_Query(database.get(0));
                // Add the same relation as many times as needed to find paths of length l
                for (int i = 1; i < l; i++) query.insert(database.get(0));
                query.set_join_conditions(new int[]{arity - 1}, new int[]{0});
            }
            else
            {
                // Construct query
                query = new Path_Equijoin_Query(database);
                query.set_join_conditions(new int[]{arity - 1}, new int[]{0});
            }
        }
        else
        {
            // No input file specified, generate input according to the parameters
            if (cmd.hasOption("relationSize") && cmd.hasOption("relationNo")) 
            {
                n = Integer.parseInt(cmd.getOptionValue("relationSize"));
                l = Integer.parseInt(cmd.getOptionValue("relationNo"));
                if (cmd.hasOption("domain")) domain = Integer.parseInt(cmd.getOptionValue("domain"));
                else domain = (int) Math.floor(Math.sqrt(n));
                // Build database
                Database_Query_Generator gen = new BinaryRandomPattern(n, l, domain, "path");
                gen.create();
                List<Relation> db = gen.get_database();
                // Construct query
                query = new Path_Equijoin_Query(db);
                query.set_join_conditions(new int[]{arity - 1}, new int[]{0});
            }
            else
            {
                System.err.println("Need -n and -l to run without an input file");
                System.exit(1);
            }
        }



        // ======= Run =======
        Measurements measurements = new Measurements(sample_rate, max_k);

        if (algorithm.equals("Boolean"))
        {
            boolean res = query.boolean_query();
            measurements.add_boolean(res);

        }
        else
        {
            instance = new DP_Path_Equijoin_Instance(query);
            instance.bottom_up();

            DP_Anyk_Iterator iter = null;
            // Run any-k
            if (algorithm.equals("Eager")) iter = new DP_Eager(instance, conf);
            else if (algorithm.equals("All")) iter = new DP_All(instance, conf);
            else if (algorithm.equals("Take2")) iter = new DP_Take2(instance, conf);
            else if (algorithm.equals("Lazy")) iter = new DP_Lazy(instance, conf);
            else if (algorithm.equals("Quick")) iter = new DP_Quick(instance, conf);
            else if (algorithm.equals("Recursive")) iter = new DP_Recursive(instance, conf);
            else if (algorithm.equals("BatchSorting")) iter = new Path_BatchSorting(instance, conf);
            else if (algorithm.equals("Batch")) iter = new Path_Batch(instance, conf);
            else
            {
                System.err.println("Any-k algorithm not recognized.");
                System.exit(1);
            }
    
            DP_Solution solution;
            for (int k = 1; k <= max_k; k++)
            {
                solution = iter.get_next();
                if (solution == null) break;
                else measurements.add_k(solution.solutionToTuples());
            }         
        }
        // Finalize and print everyting 
        measurements.print();
    }
}