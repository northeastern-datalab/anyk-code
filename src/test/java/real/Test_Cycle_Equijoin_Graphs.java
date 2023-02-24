package real;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import algorithms.Naive_For_Verification;
import algorithms.cycles.SimpleCycle_Anyk_Iterator;
import entities.Relation;
import entities.Tuple;
import entities.cycles.SimpleCycle_Equijoin_Query;
import util.DatabaseParser;

class Test_Cycle_Equijoin_Graphs 
{
    // graph, number_of_nodes, star size
    // the graph must be in src/main/resources
    static String[] small_graphs = new String[] 
    { 
        "/bitcoinotc, 15, 4", 
        "/twitter, 20, 4", 
        "/bitcoinotc, 4, 6", 
        "/twitter, 8, 6" 
    };

    static String[] anyk_algs = new String[]{"BatchSorting", "Recursive", "Eager", "Lazy", "Take2", "All", "Quick"};

    private static Stream<Arguments> provide_Small_Graphs() 
    {
        Stream<Arguments> arg_stream = Stream.of();

        for (String input : small_graphs)
        {
            // Set up the query
            String[] input_as_string_arr = input.split(", ");
            String g = input_as_string_arr[0];
            int num_nodes = Integer.parseInt(input_as_string_arr[1]);
            int cycle_length = Integer.parseInt(input_as_string_arr[2]);
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
            for (int i = 0; i < cycle_length; i++) rs.add(r);
            SimpleCycle_Equijoin_Query q = new SimpleCycle_Equijoin_Query(rs);

            // Compute the true result with a naive method
            ArrayList<Tuple> true_result = null;
            if (cycle_length == 4) true_result = Naive_For_Verification.produce_all_result_tuples_4_cycle_2attrs(rs);
            else if (cycle_length == 6) true_result = Naive_For_Verification.produce_all_result_tuples_6_cycle_2attrs(rs);
            else 
            {
                System.err.println("I need a naive implementation for that l");
                System.exit(1);
            }
            Collections.sort(true_result);

            for (String anyk_alg : anyk_algs)
            {
                arg_stream = Stream.concat(Stream.of(Arguments.of(q, g, num_nodes, cycle_length, anyk_alg, true_result)), arg_stream);
            } 
        }
        return arg_stream;
    }

    @ParameterizedTest
    @MethodSource("provide_Small_Graphs")                       
    void test_With_Naive(SimpleCycle_Equijoin_Query q, String graph, int num_nodes, int cycle_length, String anyk_alg, ArrayList<Tuple> true_result) 
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
            "Incorrect size of result for " + anyk_alg + " with " + graph + ", l=" + cycle_length);
        // Test if the output is the same (only check for cost equality)
        for (int j = 0; j < true_result.size(); j++)
        {
            String true_cost = String.format ("%.5f", true_result.get(j).cost);
            String returned_cost = String.format ("%.5f", iter_results.get(j).cost);
            assertEquals(true_cost, returned_cost, 
                "Results not the same as naive for " + anyk_alg + " with " + graph + ", l=" + cycle_length);
        }     
    }
}