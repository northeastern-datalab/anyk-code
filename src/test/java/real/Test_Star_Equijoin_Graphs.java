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
import algorithms.trees.TDP_All;
import algorithms.trees.TDP_Anyk_Iterator;
import algorithms.trees.TDP_Eager;
import algorithms.trees.TDP_Lazy;
import algorithms.trees.TDP_Recursive;
import algorithms.trees.TDP_Take2;
import algorithms.trees.Tree_BatchSorting;
import entities.Relation;
import entities.Tuple;
import entities.trees.Star_Equijoin_Query;
import entities.trees.TDP_BinaryStar_Equijoin_Instance;
import entities.trees.TDP_Problem_Instance;
import entities.trees.TDP_Solution;
import util.DatabaseParser;

class Test_Star_Equijoin_Graphs 
{
    // graph, number_of_nodes, star size
    // the graph must be in src/main/resources
    static String[] small_graphs = new String[] 
    { 
        "/bitcoinotc, 12, 4", 
        "/twitter, 15, 4", 
        "/bitcoinotc, 4, 6", 
        "/twitter, 8, 6" 
    };

    static Class<?>[] anyk_algs = new Class[] 
    {
        TDP_Recursive.class,
        Tree_BatchSorting.class,
        TDP_All.class,
        TDP_Eager.class,
        TDP_Take2.class,
        TDP_Lazy.class,
        //TDP_Quick.class
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
            int star_size = Integer.parseInt(input_as_string_arr[2]);
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
            for (int i = 0; i < star_size; i++) rs.add(r);
            Star_Equijoin_Query q = new Star_Equijoin_Query(rs);

            // Compute the true result with a naive method
            ArrayList<ArrayList<Tuple>> true_result = null;
            if (star_size == 4) true_result = Naive_For_Verification.produce_all_result_tuples_4_star_2attrs(rs);
            else if (star_size == 6) true_result = Naive_For_Verification.produce_all_result_tuples_6_star_2attrs(rs);
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
                arg_stream = Stream.concat(Stream.of(Arguments.of(q, g, num_nodes, star_size, anyk_alg, true_result)), arg_stream);
            } 
        }
        return arg_stream;
    }

    @ParameterizedTest
    @MethodSource("provide_Small_Graphs")                       
    void test_With_Naive(Star_Equijoin_Query q, String graph, int num_nodes, int star_size, Class<?> anyk_alg, ArrayList<ArrayList<Tuple>> true_result) 
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

        assertEquals(true_result.size(), iter_results.size(), 
            "Incorrect size of result for " + anyk_alg.getName() + " with " + graph + ", l=" + star_size);
        // Test if the output is the same (only check for cost equality)
        for (int j = 0; j < true_result.size(); j++)
        {
            String true_cost = String.format ("%.5f", compute_cost(true_result.get(j)));
            String returned_cost = String.format ("%.5f", iter_results.get(j).get_cost());
            assertEquals(true_cost, returned_cost, 
                "Results not the same as naive for " + anyk_alg.getName() + " with " + graph + ", l=" + star_size);
        }    
    }

    private static double compute_cost(List<Tuple> tups)
    {
        double s = 0.0;
        for (Tuple t : tups) s += t.cost;
        return s;
    }
}