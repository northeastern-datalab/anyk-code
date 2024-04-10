package experiments;

import java.util.ArrayList;
import java.util.List;

import algorithms.Configuration;
import algorithms.paths.DP_Unranked_Iterator;
import entities.Join_Predicate;
import entities.Relation;
import entities.paths.DP_Path_ThetaJoin_Instance;
import entities.paths.DP_Problem_Instance;
import entities.paths.DP_Solution;
import entities.paths.Path_ThetaJoin_Query;
import util.DatabaseParser;
import util.Measurements;

public class Binary_Join_Unranked
{
    public static void main(String args[]) 
    {
        // ======= Initialize parameters =======

        // Set the following parameters accordingly

        // Path to input file
        String input_file = "test.in";
        // Length of path query (2 for binary joins)
        // int l = 2;
        // Maximum number of values returned (set to Integer.MAX_VALUE for no limit)
        int max_k = 10000;
        // Frequency of time measurements (set to 1 to time each and every result)
        int sample_rate = 1;

        // Leave these parameters as is
        Configuration conf = new Configuration();
        conf.set_initialization_laziness(false);
        String factorization_method = null;


        // ======= Read the input =======
        DatabaseParser db_parser = new DatabaseParser(null);
        List<Relation> database = db_parser.parse_file(input_file);
        // In case of a self-join, use the following line to copy a single relation l times
        // for (int i = 1; i < l; i++) database.add(database.get(0));


        // ======= Set the query =======
        Path_ThetaJoin_Query query = new Path_ThetaJoin_Query(database);
        List<Join_Predicate> ps = new ArrayList<Join_Predicate>();
        // In the next lines, set the join predicates between the relations
        // Assume that S is the relation "on the left" in the path and T is the relation "on the right"
        // Some examples follow:
        // Third column of S equal to third column of T
        // ps.add(new Join_Predicate("E", 2, 2, null));
        // Second column of S less than first column of T
        ps.add(new Join_Predicate("IL", 1, 0, null));
        // Fourth column of S greater than fourth column of T PLUS 2
        // ps.add(new Join_Predicate("IG", 3, 3, 2));
        // First column of S not equal to second column of T
        // ps.add(new Join_Predicate("N", 0, 1, null));

        // This sets the join condition between all joining relations in the path to be exactly the same
        query.set_join_conditions_as_conjunction(ps);

        
        // ======= Run =======
        Measurements measurements = null;

        // Start the clock
        measurements = new Measurements(sample_rate, max_k);
        // Run unranked enumeration on the theta-join query
        DP_Problem_Instance instance = new DP_Path_ThetaJoin_Instance(query, factorization_method);
        DP_Unranked_Iterator iter_unranked = new DP_Unranked_Iterator(instance, null);
        DP_Solution solution;
        for (int k = 1; k <= max_k; k++)
        {
            solution = iter_unranked.get_next();
            if (solution == null) break;
            else measurements.add_k(solution.solutionToTuples());
        }
        // Finalize and print everyting 
        measurements.print();
        return;
    }
}