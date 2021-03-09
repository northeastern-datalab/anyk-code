package entities.paths;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.javatuples.Pair;

import entities.Relation;
import entities.Tuple;
import util.Common;

/** 
 * An equi-join query where the relational atoms are organized in a path.
 * The join conditions between the relations are specified as a list of pairs -
 * each pair consists of two arrays of indexes that join.
 * E.g. the pair ([1, 3], [2, 0]) for two relations R, S encodes the join condition
 * R[1] = S[2] AND R[3] = S[0] 
 * @author Nikolaos Tziavelis
*/
public class Path_Equijoin_Query
{
    /** 
     * The number of relations.
    */
    public int length;
    /** 
     * The relations that are part of the query.
    */
    public List<Relation> relations;
    /** 
     * join_conditions[i] refers to the join between relation i and i+1.
     * The number of join conditions is l - 1 for a path of length l.
     * For example, if we have R1(A, B, C) - R2(B, D, A) then left_join_attrs=[0, 1] and right_join_attrs=[2, 0].
    */
    public List<Pair<int[], int[]>> join_conditions;  

    /** 
     * A path query initialized with a single relation.
    */
    public Path_Equijoin_Query(Relation r)
    {
        this.relations = new ArrayList<Relation>();
        this.relations.add(r);
        this.length = 1;
    }

    /** 
     * Inserts a relation to the path (on the right).
     * @param r The relation to be inserted.
     */
    public void insert(Relation r)
    {
        this.relations.add(r);
        this.length++;
    }

    /** 
     * A path query initialized with a collection of relations.
    */
    public Path_Equijoin_Query(Collection<Relation> rs)
    {
        this.relations = new ArrayList<Relation>();
        for (Relation r : rs)
            this.relations.add(r);
        this.length = rs.size();
    }
    
    
    /** 
     * Sets the same join conditions between all the relations in the path.
     * @param left_join_attrs the indexes of the attributes of the left relation to be joined.
     * @param right_join_attrs the indexes of the attributes of the right relation to be joined.
     */
    public void set_join_conditions(int[] left_join_attrs, int[] right_join_attrs)
    {
        this.join_conditions = new ArrayList<Pair<int[], int[]>>();
        Pair<int[], int[]> join_condition = new Pair<int[], int[]>(left_join_attrs, right_join_attrs);
        for (int i = 0; i < this.length - 1; i++)
            join_conditions.add(join_condition);
    }

    /** 
     * Sets the join conditions for the query.
     * @param jcs A list that contains one entry for each pair of relations that joins.
     */
    public void set_join_conditions(List<Pair<int[], int[]>> jcs)
    {
        this.join_conditions = jcs;
    }

    /** 
     * Returns whether there is any valid solution at all.
     * Does not construct any data structures and does not do any comparisons.
     * Just looks for a valid path from start to end.
     * @return boolean
     */
    public boolean boolean_query()
    {
        Relation relation;
        int[] join_attributes_right, join_attributes_left;
        ArrayList<Double> join_values_right, join_values_left;
        int relation_index;

        // The number of stages is equal to the length of the path query
        int l = this.length;

        // Maintain a list of tuples that can be reached from the terminal node
        List<Tuple> reachable_tuples = new ArrayList<Tuple>();

        // Initially all tuples of the last stage are reachable
        for (Tuple t : this.relations.get(l - 1).tuples)
            reachable_tuples.add(t);

        // Go through the rest of the relations bottom-up
        for (int sg = l - 1; sg >= 1; sg--)
        {
            relation_index = sg - 1;    // because indexing of relations starts with 0 
            relation = this.relations.get(relation_index);

            // Hash the reachable tuples to be able to look them up efficiently
            HashSet<ArrayList<Double>> right_hash = new HashSet<ArrayList<Double>>();
            // Only hash the join values, not the tuples themselves
            join_attributes_right = this.join_conditions.get(relation_index).getValue1();
            for (Tuple t : reachable_tuples)
            {
                join_values_right = Common.createSublist(t.values, join_attributes_right);
                right_hash.add(join_values_right);
            }

            // Now for each tuple in the left relation, check if it matches with the right hash
            reachable_tuples = new ArrayList<Tuple>();
            join_attributes_left = this.join_conditions.get(relation_index).getValue0();
            for (Tuple t : relation.tuples)
            {
                join_values_left = Common.createSublist(t.values, join_attributes_left);
                if (right_hash.contains(join_values_left)) 
                    reachable_tuples.add(t);
            }

            // If no tuple is reachable in this relation then there is no solution
            // that can reach the starting node
            if (reachable_tuples.size() == 0) return false;
        }

        // We are at the first stage and some tuple is reachable
        // so a valid solution exists
        return true;
    }

    /** 
     * Returns a small example for debugging purposes.
     */
    public Path_Equijoin_Query()
    {
        /*
                  R1                        R2                      R3                             R4
                 ----                      ----                    ----                           ----
            A1  |  A2  |  A3            A3  |  A4             A4  |  A5  |  A6  |  A7           A6  |  A7 
            -------------------       -------------          -------------------------         ------------
            1   |   1  |  1  (10)       1   |   5  (10)       2   |   1  |  3   |  1  (5)       3   |   1  (5) 
            2   |   2  |  2  (1)        3   |   2  (5)        2   |   2  |  3   |  1  (35)      3   |   2  (10)  
            3   |   3  |  3  (5)                              5   |   5  |  3   |  2  (10)      7   |   7  (1) 

            k = 1:    (3, 3, 3), (3, 2), (2, 1, 3, 1), (3, 1) Cost = 20
            k = 2:    (1, 1, 1), (1, 5), (5, 5, 3, 2), (3, 2) Cost = 40
            k = 3:    (3, 3, 3), (3, 2), (2, 2, 3, 1), (3, 1) Cost = 50
        */

        double[] valArray;
        String[] stringArray;
        Relation r;
        this.relations = new ArrayList<Relation>();
        this.join_conditions = new ArrayList<Pair<int[], int[]>>();

        stringArray = new String[]{"A1", "A2", "A3"};
        r = new Relation("R1", stringArray);
        valArray = new double[]{1, 1, 1};
        r.insert(new Tuple(valArray, 10.0, r));
        valArray = new double[]{2, 2, 2};
        r.insert(new Tuple(valArray, 1.0, r));
        valArray = new double[]{3, 3, 3};
        r.insert(new Tuple(valArray, 5.0, r));
        this.relations.add(r);

        stringArray = new String[]{"A3", "A4"};
        r = new Relation("R2", stringArray);
        valArray = new double[]{1, 5};
        r.insert(new Tuple(valArray, 10.0, r));
        valArray = new double[]{3, 2};
        r.insert(new Tuple(valArray, 5.0, r));
        this.relations.add(r);        
        this.join_conditions.add(new Pair<int[], int[]>(new int[]{2}, new int[]{0}));

        stringArray = new String[]{"A4", "A5", "A6", "A7"};
        r = new Relation("R3", stringArray);
        valArray = new double[]{2, 1, 3, 1};
        r.insert(new Tuple(valArray, 5.0, r));
        valArray = new double[]{2, 2, 3, 1};
        r.insert(new Tuple(valArray, 35.0, r));
        valArray = new double[]{5, 5, 3, 2};
        r.insert(new Tuple(valArray, 10.0, r));
        this.relations.add(r);  
        this.join_conditions.add(new Pair<int[], int[]>(new int[]{1}, new int[]{0}));

        stringArray = new String[]{"A6", "A7"};
        r = new Relation("R4", stringArray);
        valArray = new double[]{3, 1};
        r.insert(new Tuple(valArray, 5.0, r));
        valArray = new double[]{3, 2};
        r.insert(new Tuple(valArray, 10.0, r));
        valArray = new double[]{7, 7};
        r.insert(new Tuple(valArray, 1.0, r));
        this.relations.add(r);   
        this.join_conditions.add(new Pair<int[], int[]>(new int[]{2, 3}, new int[]{0, 1}));

        this.length = 4;
    }    
}
