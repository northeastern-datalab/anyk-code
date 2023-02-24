package large_synthetic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import algorithms.Naive_For_Verification;
import algorithms.paths.DP_Solution_Iterator;
import data.BinaryRandomPattern;
import data.Database_Query_Generator;
import entities.Relation;
import entities.Tuple;
import entities.paths.DP_Path_Equijoin_Instance;
import entities.paths.DP_Problem_Instance;
import entities.paths.DP_Solution;
import entities.paths.Path_Equijoin_Query;

class Test_Path_Equijoin_Unranked 
{
    static int times_to_repeat = 2;
    // rel_size, rel_num, domain_size
    static String[] input_properties = new String[] 
    { 
        "50, 4, 50", 
        "50, 4, 300", 
        "20, 4, 8"
    };

    private static Stream<Arguments> provide_Test_Params_BinaryRandomDist() 
    {
        Stream<Arguments> arg_stream = Stream.of();

        for (int i = 0; i < times_to_repeat; i++)
        {
            for (String input : input_properties)
            {
                // Set up the query
                String[] input_as_string_arr = input.split(", ");
                int rel_size = Integer.parseInt(input_as_string_arr[0]);
                int rel_num = Integer.parseInt(input_as_string_arr[1]);
                int domain_size = Integer.parseInt(input_as_string_arr[2]);
                Database_Query_Generator gen = new BinaryRandomPattern(rel_size, rel_num, domain_size, "path");
                gen.create();
                List<Relation> db = gen.get_database();
                Path_Equijoin_Query q = new Path_Equijoin_Query(db);
                q.set_join_conditions(new int[]{1}, new int[]{0});

                // Compute the true result with a naive method
                ArrayList<ArrayList<Tuple>> true_result = Naive_For_Verification.produce_all_result_tuples_4_path_2attrs(db);

                arg_stream = Stream.concat(Stream.of(Arguments.of(q, rel_size, rel_num, domain_size, true_result)), arg_stream);

            }
        }
        return arg_stream;
    }

    @ParameterizedTest
    @MethodSource("provide_Test_Params_BinaryRandomDist")                                        
    void test_BinaryRandomDist(Path_Equijoin_Query q, int rel_size, int rel_num, int domain_size, ArrayList<ArrayList<Tuple>> true_result) 
    {
        // Run the any-k algorithm
        DP_Problem_Instance inst = new DP_Path_Equijoin_Instance(q);
        inst.bottom_up();
        DP_Solution_Iterator iter = new DP_Solution_Iterator(inst);
        List<DP_Solution> iter_results = new ArrayList<DP_Solution>();
        while (true)
        {
            DP_Solution sol = iter.get_next();
            if (sol != null) iter_results.add(sol);
            else break;
        }

        // Test if the output is the same by sorting it
        Collections.sort(true_result, new Comparator<ArrayList<Tuple>>() {
            @Override
            public int compare(ArrayList<Tuple> list_Of_tuples1, ArrayList<Tuple> list_Of_tuples2) 
            {
                double sum1 = 0.0;
                for (Tuple t : list_Of_tuples1) sum1 += t.cost;
                double sum2 = 0.0;
                for (Tuple t : list_Of_tuples2) sum2 += t.cost;     
                return Double.valueOf(sum1).compareTo(Double.valueOf(sum2));           
            }
            });
        Collections.sort(iter_results, new Comparator<DP_Solution>() {
            @Override
            public int compare(DP_Solution sol1, DP_Solution sol2) 
            { 
                return sol1.compareAgainst(sol2);
            }
            });
            
        List<List<Tuple>> iter_results_as_tuples = new ArrayList<List<Tuple>>();
        for (DP_Solution sol : iter_results) iter_results_as_tuples.add(sol.solutionToTuples_strict_order());

        assertEquals(true_result.size(), iter_results_as_tuples.size(), 
            "Incorrect size of result with n=" + rel_size + ", l=" + rel_num + ", d=" + domain_size);
        // Test if the output is the same (only check for cost equality)
        assertEquals(true_result, iter_results_as_tuples, 
            "Results not the same as naive with n=" + rel_size + ", l=" + rel_num + ", d=" + domain_size);
    }
}