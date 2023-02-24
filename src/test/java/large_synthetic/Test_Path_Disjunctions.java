package large_synthetic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import algorithms.Configuration;
import algorithms.Naive_For_Verification;
import algorithms.paths.DP_Anyk_Iterator;
import algorithms.paths.DP_Quick;
import algorithms.paths.DP_Recursive;
import data.BinaryRandomPattern;
import data.Database_Query_Generator;
import entities.Join_Predicate;
import entities.Relation;
import entities.Tuple;
import entities.paths.DP_Path_ThetaJoin_Instance;
import entities.paths.DP_Problem_Instance;
import entities.paths.DP_Solution;
import entities.paths.Path_ThetaJoin_Query;

class Test_Path_Disjunctions
{
    static int times_to_repeat = 2;
    // rel_size, rel_num, domain_size
    static String[] input_properties = new String[] 
    { 
        "Q1, 50, 2, 50",
        "Q2, 50, 2, 50",
        "Q3, 50, 3, 200",
        "Q4, 50, 3, 200",
        // "50, 3, 10"
    };
    // Every input is run with the join conditions in the same index
    // The same join condition is used to join every pair of relations in the path
    static List<List<List<Join_Predicate>>> conds = Arrays.asList(
        // Disjunctions
        // Q1
        Arrays.asList(Arrays.asList(new Join_Predicate("IL", 0, 1, null)), 
                    Arrays.asList(new Join_Predicate("IL", 0, 1, null))),
        // Q2
        Arrays.asList(Arrays.asList(new Join_Predicate("IL", 0, 1, null)), 
                    Arrays.asList(new Join_Predicate("B", 1, 0, 1.2))),
        // Q3
        Arrays.asList(Arrays.asList(new Join_Predicate("IG", 0, 0, null)), 
                    Arrays.asList(new Join_Predicate("IL", 1, 1, 3.0))),
        // Q4
        Arrays.asList(Arrays.asList(new Join_Predicate("IG", 0, 0, null), new Join_Predicate("IL", 1, 1, null)), 
                    Arrays.asList(new Join_Predicate("IL", 1, 1, null))),
        // Q5
        Arrays.asList(Arrays.asList(new Join_Predicate("E", 1, 0, 1.0), new Join_Predicate("B", 0, 1, 5.0)), 
                    Arrays.asList(new Join_Predicate("IL", 0, 1, null)))
    );

    static Class<?>[] anyk_algs = new Class[] 
    {
        DP_Recursive.class,
        DP_Quick.class
    };

    private static Stream<Arguments> provide_Test_Params_BinaryRandomDist() 
    {
        Stream<Arguments> arg_stream = Stream.of();
        for (int i = 0; i < times_to_repeat; i++)
        {
            int j = 0;
            for (String input : input_properties)
            {
                // Set up the query
                String[] input_as_string_arr = input.split(", ");
                String q_id = input_as_string_arr[0];
                int rel_size = Integer.parseInt(input_as_string_arr[1]);
                int rel_num = Integer.parseInt(input_as_string_arr[2]);
                int domain_size = Integer.parseInt(input_as_string_arr[3]);
                Database_Query_Generator gen = new BinaryRandomPattern(rel_size, rel_num, domain_size, "path");
                gen.create();
                List<Relation> db = gen.get_database();
                Path_ThetaJoin_Query q = new Path_ThetaJoin_Query(db);
                q.set_join_conditions_as_dnf(conds.get(j));

                // Compute the true result with a naive method
                ArrayList<ArrayList<Tuple>> true_result = Naive_For_Verification.produce_all_result_tuples_path_theta(q);
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

                for (Class<?> anyk_alg : anyk_algs)
                {
                    arg_stream = Stream.concat(Stream.of(Arguments.of(q_id, q, rel_size, rel_num, domain_size, anyk_alg, true_result)), arg_stream);
                } 
                j += 1;
            }
        }
        return arg_stream;
    }

    @ParameterizedTest
    @MethodSource("provide_Test_Params_BinaryRandomDist")                                        
    void test_BinaryRandomDist(String q_id, Path_ThetaJoin_Query q, int rel_size, int rel_num, int domain_size, Class<?> anyk_alg, ArrayList<ArrayList<Tuple>> true_result) 
    {
        // Run the any-k algorithm
        DP_Problem_Instance inst = new DP_Path_ThetaJoin_Instance(q, null);
        inst.bottom_up();
        DP_Anyk_Iterator iter = null;
        try{
            iter = (DP_Anyk_Iterator) anyk_alg.getDeclaredConstructor(DP_Problem_Instance.class, Configuration.class).newInstance(inst, null);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        List<DP_Solution> iter_results = new ArrayList<DP_Solution>();
        while (true)
        {
            DP_Solution sol = iter.get_next();
            if (sol != null) iter_results.add(sol);
            else break;
        }
        // Remove duplicates
        List<DP_Solution> iter_results_filtered = new ArrayList<DP_Solution>(new LinkedHashSet<DP_Solution>(iter_results));
        List<List<Tuple>> iter_results_as_tuples = new ArrayList<List<Tuple>>();
        for (DP_Solution sol : iter_results_filtered) iter_results_as_tuples.add(sol.solutionToTuples_strict_order());

        assertEquals(true_result.size(), iter_results_as_tuples.size(), 
            "Incorrect size of result for " + q_id + " with " + anyk_alg.getName() + " n=" + rel_size + ", l=" + rel_num + ", d=" + domain_size);
        assertEquals(true_result, iter_results_as_tuples, 
            "Results not the same as naive for " + q_id + " with " + anyk_alg.getName() + " n=" + rel_size + ", l=" + rel_num + ", d=" + domain_size);
    }
}