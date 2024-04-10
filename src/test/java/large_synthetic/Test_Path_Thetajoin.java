package large_synthetic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import algorithms.Configuration;
import algorithms.Naive_For_Verification;
import algorithms.paths.DP_Iterator;
import algorithms.paths.DP_Quick;
import algorithms.paths.DP_QuickPlus;
import algorithms.paths.DP_Recursive;
import algorithms.paths.DP_Unranked_Iterator;
import data.BinaryRandomPattern;
import data.Database_Query_Generator;
import entities.Join_Predicate;
import entities.Relation;
import entities.Tuple;
import entities.paths.DP_Path_ThetaJoin_Instance;
import entities.paths.DP_Problem_Instance;
import entities.paths.DP_Solution;
import entities.paths.Path_ThetaJoin_Query;

class Test_Path_Thetajoin {
    static int times_to_repeat = 2;
    // query_id, rel_size, rel_num, domain_size
    static String[] input_properties = new String[] {
            "Q1, 32, 4, 10000",
            "Q2, 50, 2, 50",
            "Q3, 50, 3, 20",
            "Q4, 50, 3, 20",
            "Q5, 50, 2, 50",
            "Q6, 100, 2, 50",
            "Q7, 20, 3, 10",
            "Q8, 50, 3, 10",
            "Q9, 30, 3, 60",
            "Q10, 100, 2, 100",
            "Q11, 30, 3, 6",
            "Q12, 100, 2, 20",
            "Q13, 50, 2, 50",
            "Q14, 50, 2, 50",
            "Q15, 50, 3, 200",
            "Q16, 100, 2, 50"
    };
    // Every input is run with the join conditions in the same index
    // The same join condition is used to join every pair of relations in the path
    static List<List<List<Join_Predicate>>> conds = Arrays.asList(
            // Single Predicates
            // Q0
            Arrays.asList(Arrays.asList(new Join_Predicate("E", 1, 0, null))),
            // Q1
            Arrays.asList(Arrays.asList(new Join_Predicate("IL", 1, 0, null))),
            // Q2
            Arrays.asList(Arrays.asList(new Join_Predicate("B", 1, 0, 3.0))),
            // Conjunctions with equality
            // Q3
            Arrays.asList(Arrays.asList(new Join_Predicate("E", 0, 1, null), new Join_Predicate("B", 1, 0, 4.0))),
            // Q4
            Arrays.asList(Arrays.asList(new Join_Predicate("E", 0, 1, null), new Join_Predicate("B", 1, 0, 2.0))),
            // Q5
            Arrays.asList(Arrays.asList(new Join_Predicate("E", 1, 0, null), new Join_Predicate("IL", 0, 1, null))),
            // Q6
            Arrays.asList(Arrays.asList(new Join_Predicate("E", 1, 0, null), new Join_Predicate("IG", 0, 1, 2.0))),
            // Q7
            Arrays.asList(Arrays.asList(new Join_Predicate("E", 1, 0, null), new Join_Predicate("IL", 0, 1, null))),
            // Q8
            Arrays.asList(Arrays.asList(new Join_Predicate("E", 1, 0, null), new Join_Predicate("IG", 0, 1, null))),
            // Q9
            Arrays.asList(Arrays.asList(new Join_Predicate("E", 1, 0, null), new Join_Predicate("IG", 0, 1, null))),
            // Non-Equalities
            // Q10
            Arrays.asList(Arrays.asList(new Join_Predicate("N", 1, 0, null))),
            // Q11
            Arrays.asList(Arrays.asList(new Join_Predicate("E", 1, 0, null), new Join_Predicate("N", 0, 1, null))),
            // Q12
            Arrays.asList(Arrays.asList(new Join_Predicate("N", 1, 0, 3.0))),
            // Conjunctions of inequalities
            // Q13
            Arrays.asList(Arrays.asList(new Join_Predicate("IL", 0, 1, null), new Join_Predicate("IL", 1, 0, null))),
            // Q14
            Arrays.asList(Arrays.asList(new Join_Predicate("IL", 0, 1, null), new Join_Predicate("B", 1, 0, 3.0))),
            // Q15
            Arrays.asList(Arrays.asList(new Join_Predicate("IG", 0, 0, null), new Join_Predicate("IL", 1, 1, null))),
            // Q16
            Arrays.asList(Arrays.asList(new Join_Predicate("N", 0, 0, null), new Join_Predicate("IL", 1, 1, 3.0))));

    static Class<?>[] anyk_algs = new Class[] {
            DP_Recursive.class,
            DP_Quick.class,
            DP_QuickPlus.class,
            DP_Unranked_Iterator.class
    };

    private static Stream<Arguments> provide_Test_Params_BinaryRandomDist() {
        Stream<Arguments> arg_stream = Stream.of();
        for (int i = 0; i < times_to_repeat; i++) {
            int j = 0;
            for (String input : input_properties) {
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
                ArrayList<ArrayList<Tuple>> true_result = Naive_For_Verification
                        .produce_all_result_tuples_path_theta(q);
                Collections.sort(true_result, new Comparator<ArrayList<Tuple>>() {
                    @Override
                    public int compare(ArrayList<Tuple> list_Of_tuples1, ArrayList<Tuple> list_Of_tuples2) {
                        double sum1 = 0.0;
                        for (Tuple t : list_Of_tuples1)
                            sum1 += t.cost;
                        double sum2 = 0.0;
                        for (Tuple t : list_Of_tuples2)
                            sum2 += t.cost;
                        return Double.valueOf(sum1).compareTo(Double.valueOf(sum2));
                    }
                });

                for (Class<?> anyk_alg : anyk_algs) {
                    arg_stream = Stream.concat(
                            Stream.of(Arguments.of(q_id, q, rel_size, rel_num, domain_size, anyk_alg, true_result)),
                            arg_stream);
                }
                j += 1;
            }
        }
        return arg_stream;
    }

    @ParameterizedTest
    @MethodSource("provide_Test_Params_BinaryRandomDist")
    void test_BinaryRandomDist(String q_id, Path_ThetaJoin_Query q, int rel_size, int rel_num, int domain_size,
            Class<?> anyk_alg, ArrayList<ArrayList<Tuple>> true_result) {
        // Run the any-k algorithm
        DP_Problem_Instance inst = new DP_Path_ThetaJoin_Instance(q, null);
        if (!anyk_alg.equals(DP_Unranked_Iterator.class))
            inst.bottom_up();
        DP_Iterator iter = null;
        try {
            iter = (DP_Iterator) anyk_alg.getDeclaredConstructor(DP_Problem_Instance.class, Configuration.class)
                    .newInstance(inst, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<DP_Solution> iter_results = new ArrayList<DP_Solution>();
        while (true) {
            DP_Solution sol = iter.get_next();
            if (sol != null)
                iter_results.add(sol);
            else
                break;
        }
        if (anyk_alg.equals(DP_Unranked_Iterator.class))
        {
            // Test if the output is the same by sorting it
            Collections.sort(iter_results, new Comparator<DP_Solution>() {
                @Override
                public int compare(DP_Solution sol1, DP_Solution sol2) 
                { 
                    return sol1.compareAgainst(sol2);
                }
                });
        }

        // Remove duplicates
        List<List<Tuple>> iter_results_as_tuples = new ArrayList<List<Tuple>>();
        for (DP_Solution sol : iter_results)
            iter_results_as_tuples.add(sol.solutionToTuples_strict_order());

        assertEquals(true_result.size(), iter_results_as_tuples.size(),
                "Incorrect size of result for " + q_id + " with " + anyk_alg.getName() + " n=" + rel_size + ", l="
                        + rel_num + ", d=" + domain_size);
        assertEquals(true_result, iter_results_as_tuples,
                "Results not the same as naive for " + q_id + " with " + anyk_alg.getName() + " n=" + rel_size + ", l="
                        + rel_num + ", d=" + domain_size);
    }
}