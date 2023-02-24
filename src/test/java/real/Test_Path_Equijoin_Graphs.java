package real;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import algorithms.Configuration;
import algorithms.Naive_For_Verification;
import algorithms.paths.DP_All;
import algorithms.paths.DP_Anyk_Iterator;
import algorithms.paths.DP_Eager;
import algorithms.paths.DP_Lazy;
import algorithms.paths.DP_Quick;
import algorithms.paths.DP_Recursive;
import algorithms.paths.DP_Take2;
import algorithms.paths.Path_BatchHeap;
import algorithms.paths.Path_BatchSorting;
import entities.Relation;
import entities.Tuple;
import entities.paths.DP_Path_Equijoin_Instance;
import entities.paths.DP_Problem_Instance;
import entities.paths.DP_Solution;
import entities.paths.Path_Equijoin_Query;
import util.DatabaseParser;

class Test_Path_Equijoin_Graphs 
{
    // graph, number_of_nodes, path length
    // the graph must be in src/main/resources
    static String[] small_graphs = new String[] 
    { 
        "/bitcoinotc, 15, 4", 
        "/twitter, 20, 4", 
        "/bitcoinotc, 5, 6", 
        "/twitter, 7, 6" 
    };
    // static String[] large_graphs = new String[] 
    // { 
    //     "/bitcoinotc, 40, 4", 
    //     "/twitter, 60, 4"
    // };
    static Class<?>[] anyk_algs = new Class[] 
    {
        DP_Recursive.class,
        Path_BatchSorting.class,
        Path_BatchHeap.class,
        DP_All.class,
        DP_Eager.class,
        DP_Take2.class,
        DP_Lazy.class,
        DP_Quick.class
    };

    private static Stream<Arguments> provide_Small_Graphs() 
    {
        Stream<Arguments> arg_stream = Stream.of();

        for (String input : small_graphs)
        {
            // Set up the query
            String[] input_as_string_arr = input.split(", ");
            String g = input_as_string_arr[0];
            int num_nodes = Integer.parseInt(input_as_string_arr[1]);
            int path_length = Integer.parseInt(input_as_string_arr[2]);
            InputStream inp = Test_Path_Equijoin_Graphs.class.getResourceAsStream(g); 
            DatabaseParser db_parser = new DatabaseParser(null);
            List<Relation> database = db_parser.parse_file(inp);
            Relation r = database.get(0);

            // Prune relation
            ListIterator<Tuple> lit = r.tuples.listIterator();
            while (lit.hasNext())
            {
                Tuple t = lit.next();
                if(t.values[0] > num_nodes || t.values[1] > num_nodes) lit.remove();

            }
            List<Relation> rs = new ArrayList<Relation>();
            for (int i = 0; i < path_length; i++) rs.add(r);
            Path_Equijoin_Query q = new Path_Equijoin_Query(rs);
            q.set_join_conditions(new int[]{1}, new int[]{0});

            // Compute the true result with a naive method
            ArrayList<ArrayList<Tuple>> true_result = null;
            if (path_length == 4) true_result = Naive_For_Verification.produce_all_result_tuples_4_path_2attrs(rs);
            else if (path_length == 6) true_result = Naive_For_Verification.produce_all_result_tuples_6_path_2attrs(rs);
            else 
            {
                System.err.println("I need a naive implementation for that l");
                System.exit(1);
            }
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
                arg_stream = Stream.concat(Stream.of(Arguments.of(q, g, num_nodes, path_length, anyk_alg, true_result)), arg_stream);
            } 
        }
        return arg_stream;
    }

    // private static Stream<Arguments> provide_Large_Graphs() 
    // {
    //     Stream<Arguments> arg_stream = Stream.of();

    //     for (String input : large_graphs)
    //     {
    //         // Set up the query
    //         String[] input_as_string_arr = input.split(", ");
    //         String g = input_as_string_arr[0];
    //         int num_nodes = Integer.parseInt(input_as_string_arr[1]);
    //         int path_length = Integer.parseInt(input_as_string_arr[2]);
    //         InputStream inp = Test_Path_Equijoin_Graphs.class.getResourceAsStream(g); 
    //         DatabaseParser db_parser = new DatabaseParser(null);
    //         List<Relation> database = db_parser.parse_file(inp);
    //         Relation r = database.get(0);

    //         // Prune relation
    //         ListIterator<Tuple> lit = r.tuples.listIterator();
    //         while (lit.hasNext())
    //         {
    //             Tuple t = lit.next();
    //             if(t.values[0] > num_nodes || t.values[1] > num_nodes) lit.remove();

    //         }
    //         List<Relation> rs = new ArrayList<Relation>();
    //         for (int i = 0; i < path_length; i++) rs.add(r);
    //         Path_Equijoin_Query q = new Path_Equijoin_Query(rs);
    //         q.set_join_conditions(new int[]{1}, new int[]{0});

    //         System.out.println("Relation Size = " + r.get_size());

    //         // Compute the true result with one of the any-k algorithms (Eager here)
    //         DP_Problem_Instance inst = new DP_Path_Equijoin_Instance(q);
    //         inst.bottom_up();
    //         DP_Anyk_Iterator iter = new DP_Eager(inst, null);
    //         List<DP_Solution> iter_results = new ArrayList<DP_Solution>();
    //         while (true)
    //         {
    //             DP_Solution sol = iter.get_next();
    //             if (sol != null) iter_results.add(sol);
    //             else break;
    //         }
    //         List<List<Tuple>> iter_results_as_tuples = new ArrayList<List<Tuple>>();
    //         for (DP_Solution sol : iter_results) iter_results_as_tuples.add(sol.solutionToTuples_strict_order());

    //         for (Class<?> anyk_alg : anyk_algs)
    //         {
    //             arg_stream = Stream.concat(Stream.of(Arguments.of(q, g, num_nodes, path_length, anyk_alg, iter_results_as_tuples)), arg_stream);
    //         } 
    //     }
    //     return arg_stream;
    // }

    // @ParameterizedTest
    // @MethodSource("provide_Large_Graphs")                       
    // void test_Without_Naive(Path_Equijoin_Query q, String graph, int num_nodes, int path_length, Class<?> anyk_alg, ArrayList<ArrayList<Tuple>> some_result) 
    // {
    //     System.out.println("Testing Large with " + anyk_alg + " " + graph + " " + path_length);
    //     // Run the any-k algorithm
    //     DP_Problem_Instance inst = new DP_Path_Equijoin_Instance(q);
    //     inst.bottom_up();
    //     DP_Anyk_Iterator iter = null;
    //     try{
    //         iter = (DP_Anyk_Iterator) anyk_alg.getDeclaredConstructor(DP_Problem_Instance.class, Configuration.class).newInstance(inst, null);
    //     }
    //     catch (Exception e) {
    //         e.printStackTrace();
    //     }
    //     List<DP_Solution> iter_results = new ArrayList<DP_Solution>();
    //     while (true)
    //     {
    //         DP_Solution sol = iter.get_next();
    //         if (sol != null) iter_results.add(sol);
    //         else break;
    //     }
    //     List<List<Tuple>> iter_results_as_tuples = new ArrayList<List<Tuple>>();
    //     for (DP_Solution sol : iter_results) iter_results_as_tuples.add(sol.solutionToTuples_strict_order());

    //     assertEquals(some_result.size(), iter_results.size(), 
    //         "Incorrect size of result for " + anyk_alg.getName() + " with " + q + ", l=" + path_length);
    //     assertEquals(some_result, iter_results_as_tuples, 
    //         "Results not the same as naive for " + anyk_alg.getName() + " with " + q + ", l=" + path_length);
    // }

    @ParameterizedTest
    @MethodSource("provide_Small_Graphs")                       
    void test_With_Naive(Path_Equijoin_Query q, String graph, int num_nodes, int path_length, Class<?> anyk_alg, ArrayList<ArrayList<Tuple>> true_result) 
    {
        // Run the any-k algorithm
        DP_Problem_Instance inst = new DP_Path_Equijoin_Instance(q);
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
        List<List<Tuple>> iter_results_as_tuples = new ArrayList<List<Tuple>>();
        for (DP_Solution sol : iter_results) iter_results_as_tuples.add(sol.solutionToTuples_strict_order());

        assertEquals(true_result.size(), iter_results.size(), 
            "Incorrect size of result for " + anyk_alg.getName() + " with " + graph + ", l=" + path_length);
        // Test if the output is the same (only check for cost equality)
        for (int j = 0; j < true_result.size(); j++)
        {
            String true_cost = String.format ("%.5f", compute_cost(true_result.get(j)));
            String returned_cost = String.format ("%.5f", iter_results.get(j).get_cost());
            assertEquals(true_cost, returned_cost, 
                "Results not the same as naive for " + anyk_alg.getName() + " with " + graph + ", l=" + path_length);
        }    
    }

    private static double compute_cost(List<Tuple> tups)
    {
        double s = 0.0;
        for (Tuple t : tups) s += t.cost;
        return s;
    }
}