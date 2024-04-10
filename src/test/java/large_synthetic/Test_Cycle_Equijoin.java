package large_synthetic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import algorithms.Naive_For_Verification;
import algorithms.cycles.NPRR_Sort;
import algorithms.cycles.SimpleCycle_Anyk_Iterator;
import data.BinaryRandomPattern;
import data.Cycle_HeavyLightPattern;
import data.Database_Query_Generator;
import entities.Relation;
import entities.Tuple;
import entities.cycles.SimpleCycle_Equijoin_Query;

class Test_Cycle_Equijoin 
{
    static int times_to_repeat = 2;

    // rel_size, rel_num, domain_size
    static String[] input_properties = new String[] 
    { 
        "50, 4, heavylight",
        "10, 6, heavylight",
        "100, 4, 10", 
        "20, 4, 20",
        "20, 4, 10",
        "8, 6, 5",
        "8, 6, 20"
    };

    static String[] anyk_algs = new String[]{"BatchSorting", "Recursive", "Eager", "Lazy", "Take2", "All", "Quick", "QuickMemoized"};

    private static Stream<Arguments> provide_Test_Params() 
    {
        Stream<Arguments> arg_stream = Stream.of();

        for (int i = 0; i < times_to_repeat; i++)
        {
            for (String input : input_properties)
            {
                String[] input_as_string_arr = input.split(", ");
                int rel_size = Integer.parseInt(input_as_string_arr[0]);
                int rel_num = Integer.parseInt(input_as_string_arr[1]);
                String join_pattern = input_as_string_arr[2];

                // Set up the query
                int domain;
                Database_Query_Generator gen = null;
                try 
                {
                    domain = Integer.parseInt(join_pattern);
                    gen = new BinaryRandomPattern(rel_size, rel_num, domain, "cycle");
                } 
                catch (NumberFormatException nfe) {
                    if (join_pattern.equals("heavylight")) gen = new Cycle_HeavyLightPattern(rel_size, rel_num);
                    else
                    {
                        System.err.println("Unknown join pattern for cycle");
                        System.exit(1);
                    }
                }
                gen.create();
                List<Relation> db = gen.get_database();
                SimpleCycle_Equijoin_Query q = new SimpleCycle_Equijoin_Query(db);

                // Compute the true result with a naive method
                ArrayList<Tuple> true_result = null;
                if (rel_num == 4) true_result = Naive_For_Verification.produce_all_result_tuples_4_cycle_2attrs(db);
                else if (rel_num == 6) true_result = Naive_For_Verification.produce_all_result_tuples_6_cycle_2attrs(db);
                else
                {
                    System.err.println("Cannot handle a cycle of this length.");
                    System.exit(1);
                }
                Collections.sort(true_result);

                for (String anyk_alg : anyk_algs)
                {
                    arg_stream = Stream.concat(Stream.of(Arguments.of(q, rel_size, rel_num, join_pattern, anyk_alg, true_result)), arg_stream);
                }
                
            }
        }
        return arg_stream;
    }

    @ParameterizedTest
    @MethodSource("provide_Test_Params")                                        
    void test_anyk(SimpleCycle_Equijoin_Query q, int rel_size, int rel_num, String join_pattern, String anyk_alg, ArrayList<Tuple> true_result) 
    {
        // Run the any-k algorithm
        SimpleCycle_Anyk_Iterator iter = new SimpleCycle_Anyk_Iterator(q, anyk_alg, null);
        ArrayList<Tuple> iter_results = new ArrayList<Tuple>();
        while (true)
        {
            Tuple sol = iter.get_next();
            if (sol != null) iter_results.add(sol);
            else break;
        }

        assertEquals(true_result.size(), iter_results.size(), 
            "Incorrect size of result for " + anyk_alg + " with n=" + rel_size + ", l=" + rel_num + ", " + join_pattern);
        // Test if the output is the same (only check for cost equality)
        for (int j = 0; j < true_result.size(); j++)
        {
            String true_cost = String.format ("%.5f", true_result.get(j).cost);
            String returned_cost = String.format ("%.5f", iter_results.get(j).cost);
            assertEquals(true_cost, returned_cost, 
                "Results not the same as naive for " + anyk_alg + " with n=" + rel_size + ", l=" + rel_num + ", " + join_pattern);
        }        
    }

    @ParameterizedTest
    @MethodSource("provide_Test_Params")                                        
    void test_nprr(SimpleCycle_Equijoin_Query q, int rel_size, int rel_num, String join_pattern, String anyk_alg, ArrayList<Tuple> true_result) 
    {
        // Since we use the same params as the any-k test, only perform the test for 1 of the any-k algorithm parameters
        if (!anyk_alg.equals("BatchSorting")) return;

        // Run NPRR
        NPRR_Sort iter_nprr = new NPRR_Sort(q);
        ArrayList<Tuple> iter_results = new ArrayList<Tuple>();
        while (true)
        {
            Tuple sol = iter_nprr.get_next();
            if (sol != null) iter_results.add(sol);
            else break;
        }

        assertEquals(true_result.size(), iter_results.size(), 
            "Incorrect size of result for NPRR with n=" + rel_size + ", l=" + rel_num + ", " + join_pattern);
        // Test if the output is the same (only check for cost equality)
        for (int j = 0; j < true_result.size(); j++)
        {
            String true_cost = String.format ("%.5f", true_result.get(j).cost);
            String returned_cost = String.format ("%.5f", iter_results.get(j).cost);
            assertEquals(true_cost, returned_cost, 
                "Results not the same as naive for NPRR with n=" + rel_size + ", l=" + rel_num + ", " + join_pattern);
        }        
    }
}