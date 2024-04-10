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

import algorithms.Configuration;
import algorithms.Naive_For_Verification;
import algorithms.trees.TDP_All;
import algorithms.trees.TDP_Anyk_Iterator;
import algorithms.trees.TDP_Eager;
import algorithms.trees.TDP_Lazy;
import algorithms.trees.TDP_Quick;
import algorithms.trees.TDP_Recursive;
import algorithms.trees.TDP_Take2;
import algorithms.trees.Tree_BatchSorting;
import data.BinaryRandomPattern;
import data.Database_Query_Generator;
import entities.Relation;
import entities.Tuple;
import entities.trees.Star_Equijoin_Query;
import entities.trees.TDP_BinaryStar_Equijoin_Instance;
import entities.trees.TDP_Problem_Instance;
import entities.trees.TDP_Solution;

class Test_Star_Equijoin 
{
    static int times_to_repeat = 2;
    // rel_size, rel_num, domain_size
    static String[] input_properties = new String[] 
    { 
        "50, 4, 50", 
        "50, 4, 300", 
        "20, 4, 8"
    };
    static Class<?>[] anyk_algs = new Class[] 
    {
        TDP_Recursive.class,
        Tree_BatchSorting.class,
        TDP_All.class,
        TDP_Eager.class,
        TDP_Take2.class,
        TDP_Lazy.class,
        TDP_Quick.class
    };

    private static Stream<Arguments> provide_Test_Params_BinaryRandomDist() 
    {
        Stream<Arguments> arg_stream = Stream.of();

        for (int i = 0; i < times_to_repeat; i++)
        {
            for (String input : input_properties)
            {
                String[] input_as_string_arr = input.split(", ");
                int rel_size = Integer.parseInt(input_as_string_arr[0]);
                int rel_num = Integer.parseInt(input_as_string_arr[1]);
                int domain_size = Integer.parseInt(input_as_string_arr[2]);

                // Set up the query
                Database_Query_Generator gen = new BinaryRandomPattern(rel_size, rel_num, domain_size, "star");
                gen.create();
                List<Relation> db = gen.get_database();
                Star_Equijoin_Query q = new Star_Equijoin_Query(db);

                // Compute the true result with a naive method
                ArrayList<ArrayList<Tuple>> true_result = Naive_For_Verification.produce_all_result_tuples_4_star_2attrs(db);
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
                    arg_stream = Stream.concat(Stream.of(Arguments.of(q, rel_size, rel_num, domain_size, anyk_alg, true_result)), arg_stream);
                }
            }
        }
        return arg_stream;
    }

    @ParameterizedTest
    @MethodSource("provide_Test_Params_BinaryRandomDist")                                        
    void test_BinaryRandomDist(Star_Equijoin_Query q, int rel_size, int rel_num, int domain_size, Class<?> anyk_alg, ArrayList<ArrayList<Tuple>> true_result) 
    {
        // Run the any-k algorithm
        TDP_Problem_Instance inst = new TDP_BinaryStar_Equijoin_Instance(q);
        inst.bottom_up();
        TDP_Anyk_Iterator iter = null;
        try{
            iter = (TDP_Anyk_Iterator) anyk_alg.getDeclaredConstructor(TDP_Problem_Instance.class, Configuration.class).newInstance(inst, null);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        List<TDP_Solution> iter_results = new ArrayList<TDP_Solution>();
        while (true)
        {
            TDP_Solution sol = iter.get_next();
            if (sol != null) iter_results.add(sol);
            else break;
        }
        List<List<Tuple>> iter_results_as_tuples = new ArrayList<List<Tuple>>();
        for (TDP_Solution sol : iter_results) iter_results_as_tuples.add(sol.solutionToTuples_strict_order());

        assertEquals(true_result.size(), iter_results_as_tuples.size(), 
            "Incorrect size of result for " + anyk_alg.getName() + " with n=" + rel_size + ", l=" + rel_num + ", d=" + domain_size);
        assertEquals(true_result, iter_results_as_tuples, 
            "Results not the same as naive for " + anyk_alg.getName() + " with n=" + rel_size + ", l=" + rel_num + ", d=" + domain_size);
    }
}