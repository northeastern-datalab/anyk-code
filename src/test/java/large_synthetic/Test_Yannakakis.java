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

import algorithms.Naive_For_Verification;
import algorithms.YannakakisSorting;
import data.BinaryRandomPattern;
import data.Database_Query_Generator;
import entities.Join_Predicate;
import entities.Relation;
import entities.Tuple;
import entities.paths.Path_Equijoin_Query;
import entities.trees.Tree_ThetaJoin_Query;

class Test_Yannakakis
{
    static int times_to_repeat = 2;
    // rel_size, rel_num, domain_size
    static String[] input_properties = new String[] 
    { 
        "50, 4, 50", 
        "50, 4, 300", 
        "20, 4, 8"
    };

    private static Stream<Arguments> provide_Test_Params_Path_BinaryRandomDist() 
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

                arg_stream = Stream.concat(Stream.of(Arguments.of(q, rel_size, rel_num, domain_size, true_result)), arg_stream);

            }
        }
        return arg_stream;
    }

    private static Stream<Arguments> provide_Test_Params_Tree_BinaryRandomDist() 
    {
        Stream<Arguments> arg_stream = Stream.of();

        // Star Query
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
                Tree_ThetaJoin_Query q = new Tree_ThetaJoin_Query();
                q.add_to_tree_wConjunction(db.get(0), 0, -1, null);
                for (int j = 1; j < rel_num; j++) q.add_to_tree_wConjunction(db.get(j), j, 0, Arrays.asList(new Join_Predicate("E", 0, 0, null))); 

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

                arg_stream = Stream.concat(Stream.of(Arguments.of(q, rel_size, rel_num, domain_size, true_result)), arg_stream);
            }
        }

        // OneBranch Query
        for (int i = 0; i < times_to_repeat; i++)
        {
            for (String input : input_properties)
            {
                String[] input_as_string_arr = input.split(", ");
                int rel_size = Integer.parseInt(input_as_string_arr[0]);
                int rel_num = Integer.parseInt(input_as_string_arr[1]);
                int domain_size = Integer.parseInt(input_as_string_arr[2]);

                // Set up the query
                Database_Query_Generator gen = new BinaryRandomPattern(rel_size, rel_num, domain_size, "onebranch");
                gen.create();
                List<Relation> db = gen.get_database();
                Tree_ThetaJoin_Query q = new Tree_ThetaJoin_Query();
                q.add_to_tree_wConjunction(db.get(0), 0, -1, null);
                for (int j = 1; j < rel_num - 1; j++) q.add_to_tree_wConjunction(db.get(j), j, j - 1, Arrays.asList(new Join_Predicate("E", 1, 0, null))); 
                q.add_to_tree_wConjunction(db.get(rel_num - 1), rel_num - 1, rel_num - 3, Arrays.asList(new Join_Predicate("E", 0, 0, null))); 

                // Compute the true result with a naive method
                ArrayList<ArrayList<Tuple>> true_result = Naive_For_Verification.produce_all_result_tuples_4_onebranch_2attrs(db);
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

                arg_stream = Stream.concat(Stream.of(Arguments.of(q, rel_size, rel_num, domain_size, true_result)), arg_stream);
            }
        }

        // Path Query
        for (int i = 0; i < times_to_repeat; i++)
        {
            for (String input : input_properties)
            {
                String[] input_as_string_arr = input.split(", ");
                int rel_size = Integer.parseInt(input_as_string_arr[0]);
                int rel_num = Integer.parseInt(input_as_string_arr[1]);
                int domain_size = Integer.parseInt(input_as_string_arr[2]);

                // Set up the query
                Database_Query_Generator gen = new BinaryRandomPattern(rel_size, rel_num, domain_size, "path");
                gen.create();
                List<Relation> db = gen.get_database();
                Tree_ThetaJoin_Query q = new Tree_ThetaJoin_Query();
                q.add_to_tree_wConjunction(db.get(0), 0, -1, null);
                for (int j = 1; j < rel_num; j++) q.add_to_tree_wConjunction(db.get(j), j, j - 1, Arrays.asList(new Join_Predicate("E", 1, 0, null))); 

                // Compute the true result with a naive method
                ArrayList<ArrayList<Tuple>> true_result = Naive_For_Verification.produce_all_result_tuples_4_path_2attrs(db);
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

                arg_stream = Stream.concat(Stream.of(Arguments.of(q, rel_size, rel_num, domain_size, true_result)), arg_stream);
            }
        }

        // Small debug query
        Tree_ThetaJoin_Query q = new Tree_ThetaJoin_Query(2);
        // True result
        ArrayList<List<Tuple>> true_result = new ArrayList<List<Tuple>>();
        true_result.add(Arrays.asList(new Tuple(new double[]{4, 5, 6}, 1, null), 
                                        new Tuple(new double[]{4, 2}, 1, null),
                                        new Tuple(new double[]{5, 1}, 1, null),
                                        new Tuple(new double[]{6, 2}, 1, null)));
        true_result.add(Arrays.asList(new Tuple(new double[]{7, 8, 9}, 5, null), 
                                        new Tuple(new double[]{7, 3}, 2, null),
                                        new Tuple(new double[]{8, 2}, 3, null),
                                        new Tuple(new double[]{9, 1}, 10, null)));
        true_result.add(Arrays.asList(new Tuple(new double[]{7, 8, 9}, 5, null), 
                                        new Tuple(new double[]{7, 3}, 2, null),
                                        new Tuple(new double[]{8, 3}, 4, null),
                                        new Tuple(new double[]{9, 1}, 10, null)));
        true_result.add(Arrays.asList(new Tuple(new double[]{7, 8, 9}, 5, null), 
                                        new Tuple(new double[]{7, 3}, 2, null),
                                        new Tuple(new double[]{8, 2}, 3, null),
                                        new Tuple(new double[]{9, 3}, 20, null)));
        true_result.add(Arrays.asList(new Tuple(new double[]{7, 8, 9}, 5, null), 
                                        new Tuple(new double[]{7, 3}, 2, null),
                                        new Tuple(new double[]{8, 3}, 4, null),
                                        new Tuple(new double[]{9, 3}, 20, null)));
        true_result.add(Arrays.asList(new Tuple(new double[]{4, 0, 0}, 100, null), 
                                        new Tuple(new double[]{4, 2}, 1, null),
                                        new Tuple(new double[]{0, 0}, 0, null),
                                        new Tuple(new double[]{0, 0}, 0, null)));
        true_result.add(Arrays.asList(new Tuple(new double[]{4, 5, 6}, 1, null), 
                                        new Tuple(new double[]{4, 0}, 100, null),
                                        new Tuple(new double[]{5, 1}, 1, null),
                                        new Tuple(new double[]{6, 2}, 1, null)));
        true_result.add(Arrays.asList(new Tuple(new double[]{4, 0, 0}, 100, null), 
                                        new Tuple(new double[]{4, 0}, 100, null),
                                        new Tuple(new double[]{0, 0}, 0, null),
                                        new Tuple(new double[]{0, 0}, 0, null)));


        arg_stream = Stream.concat(Stream.of(Arguments.of(q, -1, -1, -1, true_result)), arg_stream);

        return arg_stream;
    }

    @ParameterizedTest
    @MethodSource("provide_Test_Params_Path_BinaryRandomDist")                                        
    void test_PathBinaryRandomDist(Path_Equijoin_Query q, int rel_size, int rel_num, int domain_size, ArrayList<ArrayList<Tuple>> true_result) 
    {
        // Run Yannakakis
        YannakakisSorting iter = new YannakakisSorting(q);
        List<Tuple> iter_results = new ArrayList<Tuple>();
        while (true)
        {
            Tuple sol = iter.get_next();
            if (sol != null) iter_results.add(sol);
            else break;
        }
        
        assertEquals(true_result.size(), iter_results.size(), 
            "Incorrect size of result with n=" + rel_size + ", l=" + rel_num + ", d=" + domain_size);
        // Test if the output is the same (only check for cost equality)
        for (int j = 0; j < true_result.size(); j++)
        {
            String true_cost = String.format ("%.5f", compute_cost(true_result.get(j)));
            String returned_cost = String.format ("%.5f", iter_results.get(j).get_cost());
            assertEquals(true_cost, returned_cost, 
                "Results not the same as naive with n=" + rel_size + ", l=" + rel_num + ", d=" + domain_size);
        }  
    }

    @ParameterizedTest
    @MethodSource("provide_Test_Params_Tree_BinaryRandomDist")                                        
    void test_TreeBinaryRandomDist(Tree_ThetaJoin_Query q, int rel_size, int rel_num, int domain_size, ArrayList<ArrayList<Tuple>> true_result) 
    {
        // Run Yannakakis
        YannakakisSorting iter = new YannakakisSorting(q);
        List<Tuple> iter_results = new ArrayList<Tuple>();
        while (true)
        {
            Tuple sol = iter.get_next();
            if (sol != null) iter_results.add(sol);
            else break;
        }
        
        assertEquals(true_result.size(), iter_results.size(), 
            "Incorrect size of result with n=" + rel_size + ", l=" + rel_num + ", d=" + domain_size);
        // Test if the output is the same (only check for cost equality)
        for (int j = 0; j < true_result.size(); j++)
        {
            String true_cost = String.format ("%.5f", compute_cost(true_result.get(j)));
            String returned_cost = String.format ("%.5f", iter_results.get(j).get_cost());
            assertEquals(true_cost, returned_cost, 
                "Results not the same as naive with n=" + rel_size + ", l=" + rel_num + ", d=" + domain_size);
        }  
    }

    private static double compute_cost(List<Tuple> tups)
    {
        double s = 0.0;
        for (Tuple t : tups) s += t.cost;
        return s;
    }
}