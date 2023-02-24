package algorithms.paths;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.javatuples.Pair;

import entities.Relation;
import entities.Tuple;
import entities.paths.Path_Equijoin_Query;
import util.Common;

/** 
 * ArrayList-based implementation of the Yannakakis algorithm for path queries.
 * Produces all the query results in O(input + output) and then sorts them.
 * Subsequent calls to get_next() return the query results sorted incrementally.
 * @author Nikolaos Tziavelis
*/
public class Path_YannakakisSorting
{
    // The relations that form the cycle
    List<Relation> relations;     
    // The length of the path 
    int path_length;
    // The equi-join conditions between the relations              
    List<Pair<int[], int[]>> join_conditions;         
    Relation out_relation;
    int tuples_no;
    int current_index;
    
	public Path_YannakakisSorting(Path_Equijoin_Query query)
    {
        this.relations = query.relations;
        this.path_length = query.length;
        this.join_conditions = query.join_conditions;

        out_relation = compute_all_results();

        Collections.sort(out_relation.tuples);

        this.tuples_no = out_relation.get_size();
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
        Tuple ret = out_relation.get(current_index);
        return ret;
    }

    /** 
     * Computes all the query results with 2 sweeps: 
     * one bottom-up with semi-joins and one top-down that joins the relations.
     * @return Relation A new object that contains the output tuples.
     */
    private Relation compute_all_results()
    {
        Relation reduced_relation;

        // Bottom-up sweep: start from the end of the path and go up to the first relation R_0
        for (int i = path_length - 2; i >= 0; i--)
        {
            // In iteration i, semi-join reduce R_i with R_(i+1) and replace it
            reduced_relation = semi_join(relations.get(i), relations.get(i + 1), join_conditions.get(i));
            relations.set(i, reduced_relation);
        }

        //System.out.println("Bottom up phase complete");
        //for (Relation r : relations) System.out.println(r);


        // Top-down sweep: start from the beginning of the path and go to the last relation
        Relation res = relations.get(0);
        // To keep track of how the current result joins with the next relation during the top-down sweep
        // we maintain an offset for the joining attributes
        // The attributes of all joined relations will be added to the relation res at the end of its schema
        // Thus the offset we maintain shows how many attributes have been added to res
        int attr_offset = 0;
        Pair<int[], int[]> initial_jc, new_jc;
        int[] initial_jc_left, initial_jc_right, new_jc_left;
        for (int i = 1; i <= path_length - 1; i++)
        {
            //System.out.println("Joining res with " + relations.get(i).relation_id);

            // Add the attribute offset to the join condition (affects the left relation)
            initial_jc = join_conditions.get(i - 1);
            initial_jc_left = initial_jc.getValue0();
            initial_jc_right = initial_jc.getValue1();
            new_jc_left = new int[initial_jc_left.length];
            for (int j = 0; j < initial_jc_left.length; j++) 
            {
                new_jc_left[j] = initial_jc_left[j] + attr_offset;
            }
            new_jc = new Pair<int[], int[]>(new_jc_left, initial_jc_right);

            /*
            System.out.println("Initial join conditions :");
            System.out.println("First= " + Arrays.toString(initial_jc.getValue0()) + 
                "  -  Second= " + Arrays.toString(initial_jc.getValue1()));
            System.out.println("New join conditions = ");
            System.out.println("First= " + Arrays.toString(new_jc.getValue0()) + 
                "  -  Second= " + Arrays.toString(new_jc.getValue1()));
            */

            // In iteration i, join the current result with R_i
            res = join(res, relations.get(i), new_jc);
            // Set the offset to the number of attributes that res has except the newly added ones
            attr_offset = res.schema.length - relations.get(i).schema.length;

            //System.out.println("Attribute offset is now " + attr_offset);
            //System.out.println(res);

        }

        return res;
    }

    
    /** 
     * Computes the semi-join of two relations ri and rj.
     * The result consists of all the tuples of R_i that have a joining pair in R_j.
     * @param ri
     * @param rj
     * @param join_condition
     * @return Relation A new object that contains all the tuples of ri that have a joining pair in rj.
     */
    private Relation semi_join(Relation ri, Relation rj, Pair<int[], int[]> join_condition)
    {
        ArrayList<Double> join_values_j, join_values_i;

        // First create a hash set with the tuples of R_j
        // For the semi-join, we don't need to remember for each join value exactly the tuples of R_j
        // only that specific join values exist
        HashSet<ArrayList<Double>> hash_j = new HashSet<ArrayList<Double>>();
        // The keys will be the join attributes of R_j stored in the join_condition pair
        // The indexes of those attributes are the second value of the pair
        int[] join_attributes_j = join_condition.getValue1();
        for (Tuple t : rj.tuples)
        {
            join_values_j = Common.createSublist(t.values, join_attributes_j);
            hash_j.add(join_values_j);
        }

        // Now create a new relation in the place of R_i that contains only the tuples
        // that have a match in our hash set
        Relation res = new Relation(ri.relation_id, ri.schema);
        // The joining attributes of R_i are found by the indexes stored in
        // the first entry of the join_condition pair
        int[] join_attributes_i = join_condition.getValue0();
        // Go through the tuples of R_i and filter them
        for (Tuple t : ri.tuples)
        {
            join_values_i = Common.createSublist(t.values, join_attributes_i);
            if (hash_j.contains(join_values_i))
            {
                res.insert(t);
            }
        }        
        return res;
    }

    
    /** 
     * Computes the join of two relations ri and rj.
     * @param ri
     * @param rj
     * @param join_condition
     * @return Relation A new object that contains all the matching pairs between ri and rj based on the join condition.
     */
    private Relation join(Relation ri, Relation rj, Pair<int[], int[]> join_condition)
    {
        ArrayList<Double> join_values_j, join_values_i;
        ArrayList<Tuple> tuple_list_same_key;
        Tuple new_tuple;

        // First create a hash table with the tuples of R_j
        // For each join value, we maintain a list of all the tuples of R_j that share this join value
        HashMap<ArrayList<Double>, ArrayList<Tuple>> hash_j = new HashMap<ArrayList<Double>, ArrayList<Tuple>>();
        // The keys will be the join attributes of R_j stored in the join_condition pair
        // The indexes of those attributes are the second value of the pair
        int[] join_attributes_j = join_condition.getValue1();
        for (Tuple t : rj.tuples)
        {
            join_values_j = Common.createSublist(t.values, join_attributes_j);

            tuple_list_same_key = hash_j.get(join_values_j);
            if (tuple_list_same_key == null)
            {
                // Key not present in hash table
                // Initialize a list with the current tuple and add it to the hash table
                tuple_list_same_key = new ArrayList<Tuple>();
                tuple_list_same_key.add(t);
                hash_j.put(join_values_j, tuple_list_same_key);
            }
            else
            {
                // We have already hashed a tuple associated with the same join attribute values
                // Just add the new tuple to the list
                tuple_list_same_key.add(t);
            }
        }

        // Now create a new relation in the place of R_i that contains all the tuple pairs (their concatenation)
        // that are matching based on our hash table
        Relation res = new Relation("R_out", Common.concatenate_string_arrays(ri.schema, rj.schema));

        // The joining attributes of R_i are found by the indexes stored in
        // the first entry of the join_condition pair
        int[] join_attributes_i = join_condition.getValue0();
        // Go through the tuples of R_i and join them with their matches in the hash table
        for (Tuple t : ri.tuples)
        {
            join_values_i = Common.createSublist(t.values, join_attributes_i);

            tuple_list_same_key = hash_j.get(join_values_i);
            if (tuple_list_same_key != null)
            {
                // There is a matching tuple list in the hash table
                for (Tuple t_joining : tuple_list_same_key)
                {
                    // Join the tuples
                    new_tuple = new Tuple(Common.concatenate_double_arrays(t.values, t_joining.values), t.cost + t_joining.cost, res);
                    res.insert(new_tuple);
                }
            }

        }        
        return res;
    }

    public static void main(String args[]) 
    {
        // Run the example
        Path_Equijoin_Query example_query = new Path_Equijoin_Query();
        Path_YannakakisSorting yann = new Path_YannakakisSorting(example_query);

        Tuple t;
        while (true)
        {
            t = yann.get_next();
            if (t == null) break;
            System.out.println(t + " Cost = " + t.cost);
        }
    }
}