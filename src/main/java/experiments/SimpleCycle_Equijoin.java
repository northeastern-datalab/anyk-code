package experiments;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import algorithms.cycles.NPRR;
import algorithms.cycles.NPRR_Sort;
import algorithms.cycles.SimpleCycle_Anyk_Iterator;
import data.BinaryRandomPattern;
import data.Cycle_HeavyLightPattern;
import data.Database_Query_Generator;
import entities.Relation;
import entities.Tuple;
import entities.cycles.SimpleCycle_Equijoin_Query;
import util.DatabaseParser;
import util.Measurements;

/** 
 * This class provides a main function that receives execution parameters from the command line
 * and runs experiments for equi-join queries where the joined relations are binary and organized in a simple cycle.
 * All measurements are written in standard output.
 * Call with no arguments to get a list of accepted options.
 * @author Nikolaos Tziavelis
*/
public class SimpleCycle_Equijoin
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
        SimpleCycle_Equijoin_Query query;
        String input_file;
        int n = -1;
        int l = -1;
        String algorithm = cmd.getOptionValue("algorithm"); 
        int max_k;
        if (cmd.hasOption("numOfResults")) max_k = Integer.parseInt(cmd.getOptionValue("numOfResults")); 
        else max_k = Integer.MAX_VALUE;
        int sample_rate = 1;
        String heap_type;
        if (cmd.hasOption("heap type")) heap_type = cmd.getOptionValue("heap type");
        else heap_type = null;   // let the classes choose by themselves
        boolean self_join = false;
        if (cmd.hasOption("selfJoin")) self_join = true;
        if (cmd.hasOption("downsample")) 
        {
            long estimated_result_size = 0;
            // in case -k has been set, we know the output size	
            if (cmd.hasOption("numOfResults")) estimated_result_size = Integer.parseInt(cmd.getOptionValue("numOfResults"));
        	else if (cmd.hasOption("relationSize") && cmd.hasOption("relationNo")) 
        	{
        		n = Integer.parseInt(cmd.getOptionValue("relationSize"));
        		l = Integer.parseInt(cmd.getOptionValue("relationNo"));
                estimated_result_size = Math.round(n * Math.pow(n / 2, (l / 2) - 1));
        	}
        	else
        	{
                System.err.println("Need -n, -l and -d to run with downsampling");
                System.exit(1);  		
        	}
            sample_rate = (int) Math.ceil(estimated_result_size / 500.0); 
        }
        else sample_rate = 1;



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
                Relation r = database.get(0);
                // Our graphs have only one relation (edges)
                // Add the same relation as many times as needed to search for paths of length l
                List<Relation> rs = new ArrayList<Relation>();
                for (int i = 0; i < l; i++) rs.add(r);
                query = new SimpleCycle_Equijoin_Query(rs);
            }
            else
            {
                // Construct query
                query = new SimpleCycle_Equijoin_Query(database);
            } 
        }
        else
        {
            // No input file specified, generate input according to the parameters
            if (cmd.hasOption("relationSize") && cmd.hasOption("relationNo")) 
            {
                n = Integer.parseInt(cmd.getOptionValue("relationSize"));
                l = Integer.parseInt(cmd.getOptionValue("relationNo"));
                // Build database
                Database_Query_Generator gen = new Cycle_HeavyLightPattern(n, l);
                gen.create();
                List<Relation> db = gen.get_database();
                // Construct query
                query = new SimpleCycle_Equijoin_Query(db);
            }
            else
            {
                System.err.println("Need -n and -l to run without an input file");
                System.exit(1);
                return;
            }
        }


        // ======= Warm-up phase =======
        double dummy_counter = 0.0;  // to verify the computation is not removed by the compiler 
        for (int i = 0; i < 10000; i++) 
            dummy_counter += warm_up_cycle_equijoin(algorithm, heap_type);
        System.out.println("Dummy counter = " + dummy_counter);


        // ======= Run =======
        Measurements measurements = new Measurements(sample_rate, max_k);

        if (algorithm.equals("NPRR"))
        {
            NPRR iter = new NPRR(query);

            Tuple solution_tuple;
            for (int k = 1; k <= max_k; k++)
            {
                solution_tuple = iter.get_next();
                if (solution_tuple == null) break;
                else measurements.add_k(solution_tuple);
            }
        }
        else if (algorithm.equals("NPRR_Sort"))
        {
            NPRR_Sort iter = new NPRR_Sort(query);

            Tuple solution_tuple;
            for (int k = 1; k <= max_k; k++)
            {
                solution_tuple = iter.get_next();
                if (solution_tuple == null) break;
                else measurements.add_k(solution_tuple);
            }
        }
        else
        {
            SimpleCycle_Anyk_Iterator iter = new SimpleCycle_Anyk_Iterator(query, algorithm, heap_type);

            // Run any-k
            Tuple solution_tuple;
            for (int k = 1; k <= max_k; k++)
            {
                solution_tuple = iter.get_next();
                if (solution_tuple == null) break;
                else measurements.add_k(solution_tuple);
            }   
        }
        measurements.print();
    }

    private static double warm_up_cycle_equijoin(String algorithm, String heap_type)
    {
        Database_Query_Generator gen;
        SimpleCycle_Equijoin_Query query;
        Tuple solution_tuple;
        double dummy_counter = 0;  // to verify the computation is not removed by the compiler

        // Heavy-Light pattern
        gen = new Cycle_HeavyLightPattern(20, 4);
        gen.create();
        query = new SimpleCycle_Equijoin_Query(gen.get_database());
        if (algorithm.equals("NPRR"))
        {
            NPRR iter = new NPRR(query);
            while (true)
            {
                solution_tuple = iter.get_next();
                if (solution_tuple == null) break;
                dummy_counter += solution_tuple.values[0];
            }
        }
        else if (algorithm.equals("NPRR_Sort"))
        {
            NPRR_Sort iter = new NPRR_Sort(query);
            while (true)
            {
                solution_tuple = iter.get_next();
                if (solution_tuple == null) break;
                dummy_counter += solution_tuple.values[0];
            }
        }
        else
        {
            SimpleCycle_Anyk_Iterator iter = new SimpleCycle_Anyk_Iterator(query, algorithm, heap_type);
            while (true)
            {
                solution_tuple = iter.get_next();
                if (solution_tuple == null) break;
                dummy_counter += solution_tuple.values[0];
            }   
        }

        // BinaryRandom pattern
        gen = new BinaryRandomPattern(20, 4, 6, "cycle");
        gen.create();
        query = new SimpleCycle_Equijoin_Query(gen.get_database());
        if (algorithm.equals("NPRR"))
        {
            NPRR iter = new NPRR(query);
            while (true)
            {
                solution_tuple = iter.get_next();
                if (solution_tuple == null) break;
                dummy_counter += solution_tuple.values[0];
            }
        }
        else if (algorithm.equals("NPRR_Sort"))
        {
            NPRR_Sort iter = new NPRR_Sort(query);
            while (true)
            {
                solution_tuple = iter.get_next();
                if (solution_tuple == null) break;
                dummy_counter += solution_tuple.values[0];
            }
        }
        else
        {
            SimpleCycle_Anyk_Iterator iter = new SimpleCycle_Anyk_Iterator(query, algorithm, heap_type);
            while (true)
            {
                solution_tuple = iter.get_next();
                if (solution_tuple == null) break;
                dummy_counter += solution_tuple.values[0];
            }   
        }

        return dummy_counter;
    }
}