package algorithms;

import java.util.Collections;
import java.util.List;

import org.javatuples.Pair;

import entities.Relation;
import entities.Tuple;
import entities.paths.Path_Equijoin_Query;
import entities.trees.Tree_ThetaJoin_Query;

/** 
 * ArrayList-based implementation of the Yannakakis algorithm for acyclic queries.
 * Produces all the query results in O(input + output) and then sorts them.
 * Subsequent calls to get_next() return the query results sorted incrementally.
 * @author Nikolaos Tziavelis
*/
public class YannakakisSorting
{
    // The relations that form the cycle
    List<Relation> relations;     
    // The length of the path 
    int path_length;
    // The equi-join conditions between the relations              
    List<Pair<int[], int[]>> join_conditions;         
    Relation out_relation;
    List<Tuple> all_results;
    int tuples_no;
    int current_index;
    
    // Warning: the query object, the relations, etc. will be modified
	public YannakakisSorting(Path_Equijoin_Query query)
    {
        Yannakakis yann_unsorted = new Yannakakis(query);
        this.all_results = yann_unsorted.all_results;

        Collections.sort(this.all_results);

        this.tuples_no = this.all_results.size();
        this.current_index = -1;
    }

    // The query has to be an equi-join
    // Warning: the query object, the relations, the children strucutre, etc. will be modified
	public YannakakisSorting(Tree_ThetaJoin_Query query)
    {
        Yannakakis yann_unsorted = new Yannakakis(query);
        this.all_results = yann_unsorted.all_results;

        Collections.sort(this.all_results);

        this.tuples_no = this.all_results.size();
        this.current_index = -1;
    }

    /** 
     * Computes the next Tuple that is a solution to the query in ranked order (ascending cost). 
     * Ties are broken arbitrarily.
     * @return Tuple The next best Tuple solution or null if there are no other solutions.
     */
    public Tuple get_next()
    {
        this.current_index++;
        if (current_index == this.tuples_no) return null;
        return all_results.get(current_index);
    }

    public static void main(String args[]) 
    {
        // // Run the example
        // Path_Equijoin_Query path_query = new Path_Equijoin_Query();
        // YannakakisSorting yann = new YannakakisSorting(path_query);

        // Tuple t;
        // while (true)
        // {
        //     t = yann.get_next();
        //     if (t == null) break;
        //     System.out.println(t + " Cost = " + t.cost);
        // }

        // Run an example tree
        Tree_ThetaJoin_Query tree_query = new Tree_ThetaJoin_Query(2);
        YannakakisSorting yann2 = new YannakakisSorting(tree_query);
        Tuple t2;

        while (true)
        {
            t2 = yann2.get_next();
            if (t2 == null) break;
            System.out.println(t2 + " Cost = " + t2.cost);
        }
    }
}