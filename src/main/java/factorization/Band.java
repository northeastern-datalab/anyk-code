package factorization;

import java.util.ArrayList;
import java.util.List;

import org.javatuples.Triplet;

import entities.Join_Predicate;
import entities.State_Node;
import util.Common;

/** 
 * Contains methods that are useful for handling band conditions.
 * @author Nikolaos Tziavelis
*/
public class Band 
{
    /** 
     * Given two (T-)DP stages that correspond to the tuples of two relations 
     * and a band condition, translates the band into multiple inequalities that give the same result.
     * The resulting inequalities cover different (and possibly overlapping) groups of left-right tuples.
     * These can then be handled independently.
     * The two relations are assumed to be already sorted.
     * @param left The first (sorted) relation/stage as a list of DP state-nodes.
     * @param right The second (sorted) relation/stage as a list of DP state-nodes.
     * @param band A band join predicate between the two relations.
     * @return A list of (inequality, left', right') triples where the inequality predicate covers left'-right' tuples
     */
    public static List<Triplet<Join_Predicate, List<? extends State_Node>, List<? extends State_Node>>> 
        band_grouping(List<? extends State_Node> left, List<? extends State_Node> right, Join_Predicate band)
    {
        int left_group_start_idx, left_group_end_idx;
        double right_group_start_val, right_group_end_val;
        List<Triplet<Join_Predicate, List<? extends State_Node>, List<? extends State_Node>>> res = 
            new ArrayList<Triplet<Join_Predicate, List<? extends State_Node>, List<? extends State_Node>>>();

        // Split the right one into groups based on epsilon-intervals
        for (List<? extends State_Node> right_group : split_by_epsilon(right, band.attr_idx_2, band.parameter))
        {
            right_group_start_val = right_group.get(0).toTuple().values[band.attr_idx_2];
            right_group_end_val = right_group.get(right_group.size() - 1).toTuple().values[band.attr_idx_2];
            // Find the limits of the left group that will be handled with a greater-than condition
            Join_Predicate greater_than = new Join_Predicate("IG", band.attr_idx_1, band.attr_idx_2, -1.0 * band.parameter);
            left_group_start_idx = Common.binary_search_min(left, band.attr_idx_1, right_group_start_val - band.parameter, true);
            left_group_end_idx = Common.binary_search_max(left, band.attr_idx_1, right_group_start_val + band.parameter, false);
            if (left_group_start_idx != -1 && left_group_end_idx != -1)
            {
                List<? extends State_Node> left_group_greater = left.subList(left_group_start_idx, left_group_end_idx + 1);
                res.add(new Triplet<Join_Predicate, List<? extends State_Node>, List<? extends State_Node>>
                    (greater_than, left_group_greater, right_group));   
            }
            // Find the limits of the left group that will be handled with a less-than condition
            Join_Predicate less_than = new Join_Predicate("IL", band.attr_idx_1, band.attr_idx_2, band.parameter);
            left_group_start_idx = Common.binary_search_min(left, band.attr_idx_1, right_group_start_val + band.parameter, true);
            left_group_end_idx = Common.binary_search_max(left, band.attr_idx_1, right_group_end_val + band.parameter, true);
            if (left_group_start_idx != -1 && left_group_end_idx != -1)
            {
                List<? extends State_Node> left_group_less = left.subList(left_group_start_idx, left_group_end_idx + 1);   
                res.add(new Triplet<Join_Predicate, List<? extends State_Node>, List<? extends State_Node>>
                    (less_than, left_group_less, right_group));        
            }
        }            
        return res;
    }    

    /** 
     * Splits the tuples of a DP stage that corresponds to the tuples of a relations 
     * into groups according to an attribute of a band condition and its epsilon.
     * The tuples have to be sorted beforehand.
     * The resulting partitions satisy the following:
     * <ul>
     * <li>Each partition contains a range of values bounded by epsilon (inclusive).</li>
     * <li>Each partition contains the maximum number of tuples possible.</li>
     * <li>The partitions are sorted both locally and globally.</li>
     * </ul>
     * The resulting list doesn't allocate new lists of nodes.
     * Instead, views of the original list as sublists are used.
     * @param stage The (sorted) subset of a relation/stage.
     * @param attr_idx The index of the attribute we use for spliiting.
     * @param epsilon The epsilon parameter of a band predicate.
     * @return List<List<State_Node>> A partitioning of the nodes as a list of lists.
     */
    public static List<List<? extends State_Node>> split_by_epsilon(List<? extends State_Node> stage, int attr_idx, double epsilon)
    {
        List<List<? extends State_Node>> res = new ArrayList<List<? extends State_Node>>();
        int current_idx = 0;
        double current_val = stage.get(0).toTuple().values[attr_idx];
        int group_start_idx = 0;
        double group_start_value = current_val;
        current_idx += 1;

        while (current_idx < stage.size())
        {
            current_val = stage.get(current_idx).toTuple().values[attr_idx];
            // Check if it is outside the epsilon interval
            if (current_val > group_start_value + epsilon)
            {
                // End the previous group and start a new one
                res.add(stage.subList(group_start_idx, current_idx));
                group_start_idx = current_idx;
                group_start_value = current_val;
            }
            // Look at the next node
            current_idx += 1;
        }
        // Insert the last group to the result
        res.add(stage.subList(group_start_idx, current_idx));
        return res;
    }
}
