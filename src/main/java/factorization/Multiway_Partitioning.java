package factorization;

import java.util.ArrayList;
import java.util.List;

import org.javatuples.Pair;
import org.javatuples.Triplet;

import entities.Join_Predicate;
import entities.paths.DP_State_Node;
import util.Common;

/** 
 * Contains methods for efficiently representing (or factorizing) the query results of a join between 2 relations
 * where the join condition is an arbitrary number of equalities and a single inequality/non-equality/band predicate.
 * The methods rely on partitioning the domain values.
 * The resulting representation creates 2 intermediate layers of nodes between the relations.
 * Compared to @link{factorization.Conjunction}, the methods of this class use O(nloglogn) space instead of O(nlogn).
 * The running time for all methods is O(nlogn) since we always sort the relations.
 * @author Nikolaos Tziavelis
*/
public class Multiway_Partitioning
{
    /** 
     * Given two DP stages that correspond to the tuples of two relations 
     * and a list that contains many equalities and a single band,
     * constructs an efficient representation of the join results.
     * The method groups the tuples based on epsilon-intervals such that it can then translate the band condition
     * into a conjunction of inequalities that can be handled independently and efficiently.
     * After grouping the tuples appropriatedly, it uses the 
     * inequality partitioning algorithm @link{#partition_inequality_rec}.
     * @param left The first relation/stage as a list of DP state-nodes.
     * @param right The second relation/stage as a list of DP state-nodes.
     * @param ps A list of equality predicates and a single band predicate at the end of the list.
     */
    public static void factorize_band(List<DP_State_Node> left, List<DP_State_Node> right, List<Join_Predicate> ps)
    {
        Join_Predicate band = ps.get(ps.size() - 1);
        // First take care of the equalities that precede the single non-equality condition
        for (Pair<List<DP_State_Node>,List<DP_State_Node>> equality_partition : 
                Equality.split_by_equality(left, right, ps.subList(0, ps.size() - 1)))
        {
            List<DP_State_Node> left_partition = equality_partition.getValue0();
            List<DP_State_Node> right_partition = equality_partition.getValue1();

            // Sort before translating the band and calling the recursive partitioning algorithm
            Common.sort_stage(left_partition, band.attr_idx_1);
            Common.sort_stage(right_partition, band.attr_idx_2);
          
            for (Triplet<Join_Predicate,List<DP_State_Node>,List<DP_State_Node>> ineq_group : 
                Band.band_grouping(left_partition, right_partition, band))
            {
                partition_inequality_rec(ineq_group.getValue1(), ineq_group.getValue2(), ineq_group.getValue0());
            }
        }
    }

    /** 
     * Given two DP stages that correspond to the tuples of two relations 
     * and a list that contains many equalities and a single non-equality,
     * constructs an efficient representation of the join results.
     * The method translates the non-equality as a disjunction of two inequalities and then uses the 
     * inequality partitioning algorithm @link{#partition_inequality_rec}.
     * @param left The first relation/stage as a list of DP state-nodes.
     * @param right The second relation/stage as a list of DP state-nodes.
     * @param ps A list of equality predicates and a single non-equality predicate at the end of the list.
     */
    public static void factorize_nonequality(List<DP_State_Node> left, List<DP_State_Node> right, List<Join_Predicate> ps)
    {
        Join_Predicate nonequality = ps.get(ps.size() - 1);
        // First take care of the equalities that precede the single non-equality condition
        for (Pair<List<DP_State_Node>,List<DP_State_Node>> equality_partition : 
                Equality.split_by_equality(left, right, ps.subList(0, ps.size() - 1)))
        {
            List<DP_State_Node> left_partition = equality_partition.getValue0();
            List<DP_State_Node> right_partition = equality_partition.getValue1();

            // Sort before calling the recursive partitioning algorithm
            Common.sort_stage(left_partition, nonequality.attr_idx_1);
            Common.sort_stage(right_partition, nonequality.attr_idx_2);

            // Handle the non-equality as two inequalities
            Join_Predicate less_than = new Join_Predicate("IL", nonequality.attr_idx_1, nonequality.attr_idx_2, nonequality.parameter);
            partition_inequality_rec(left_partition, right_partition, less_than);
            Join_Predicate greater_than = new Join_Predicate("IG", nonequality.attr_idx_1, nonequality.attr_idx_2, nonequality.parameter);
            partition_inequality_rec(left_partition, right_partition, greater_than);
        }
    }

    /** 
     * Given two DP stages that correspond to the tuples of two relations 
     * and a list that contains many equalities and a single inequality,
     * constructs an efficient representation of the join results.
     * The method relies on a recursive procedure that creates multiple partitions in each step
     * and only needs O(nloglogn) space.
     * @param left The first relation/stage as a list of DP state-nodes.
     * @param right The second relation/stage as a list of DP state-nodes.
     * @param ps A list of equality predicates and a single inequality predicate at the end of the list.
     */
    public static void factorize_inequality(List<DP_State_Node> left, List<DP_State_Node> right, List<Join_Predicate> ps)
    {
        Join_Predicate inequality = ps.get(ps.size() - 1);
        // First take care of the equalities that precede the single inequality condition
        for (Pair<List<DP_State_Node>,List<DP_State_Node>> equality_partition : 
                Equality.split_by_equality(left, right, ps.subList(0, ps.size() - 1)))
        {
            List<DP_State_Node> left_partition = equality_partition.getValue0();
            List<DP_State_Node> right_partition = equality_partition.getValue1();

            // Sort before calling the recursive partitioning algorithm
            Common.sort_stage(left_partition, inequality.attr_idx_1);
            Common.sort_stage(right_partition, inequality.attr_idx_2);

            partition_inequality_rec(left_partition, right_partition, inequality);
        }
    }

    /** 
     * A recursive method for encoding the query results of an inequality join.
     * It works by creating multiple partitions, connecting them appropriately and
     * recursively calling the same method for each partition.
     * Before calling this method, the nodes have to be sorted by the predicate attributes.
     * @param left The first (sorted) subset of a relation/stage.
     * @param right The second (sorted) subset of a relation/stage.
     * @param inequality A single inequality predicate.
     */
    private static void partition_inequality_rec(List<DP_State_Node> left, List<DP_State_Node> right, Join_Predicate inequality)
    {
        String rec_step_id = String.valueOf(left.size() + right.size());    // Used for naming the intermediate nodes
        List<DP_State_Node> left_i, right_i, left_j, right_j;

        int distinct_cnt = Common.count_distinct_vals(left, right, inequality.attr_idx_1, inequality.attr_idx_2, inequality.parameter);
        // Base Case
        if (distinct_cnt <= 1) return;
        // Make sqrt(distinct_vals) partitions
        List<Pair<List<DP_State_Node>,List<DP_State_Node>>> inequality_partitions = 
            Common.split_by_distinct(left, right, inequality, (int) Math.ceil(Math.sqrt(distinct_cnt)), (int) Math.floor(Math.sqrt(distinct_cnt)));
        // Initialize the intermediate stages
        List<DP_State_Node> intermediate_stage_1 = new ArrayList<DP_State_Node>(inequality_partitions.size());
        List<DP_State_Node> intermediate_stage_2 = new ArrayList<DP_State_Node>(inequality_partitions.size());
        for (int i = 0; i <inequality_partitions.size(); i++)
        {
            intermediate_stage_1.add(null);
            intermediate_stage_2.add(null);
        }
        // Go through each partition
        for (int i = 0; i < inequality_partitions.size(); i++)
        {
            left_i = inequality_partitions.get(i).getValue0();
            right_i = inequality_partitions.get(i).getValue1(); 

            // Connect the partition according to the inequality type
            // Materialize the intermediate nodes and their connections to left/right
            // only if they aren't dangling. We need to check for two conditions:
            // (1) the current partition is not empty
            // (2) the partitions that are less/greater than it are non-empty
            if (inequality.type.equals("IL"))
            {
                if (!right_i.isEmpty())
                {
                    // For a less-than connect all the previous left partitions to the current right partition
                    for (int j = 0; j <= i - 1; j++)
                    {
                        left_j = inequality_partitions.get(j).getValue0();
                        if (!left_j.isEmpty())
                        {
                            // Connect left_j to right_i
                            // First check if their intermediate nodes have been created
                            if (intermediate_stage_1.get(j) == null)
                            {
                                // Materialize the intermediate node
                                DP_State_Node intermediate_node_1 = new DP_State_Node("X" + rec_step_id + "_" + j);
                                intermediate_stage_1.set(j, intermediate_node_1);
                                // Connect left partition j to the new intermediate node
                                for (DP_State_Node l : left_j) l.add_decision(intermediate_node_1, 0.0);
                            }
                            if (intermediate_stage_2.get(i) == null)
                            {
                                // Materialize the intermediate node
                                DP_State_Node intermediate_node_2 = new DP_State_Node("Y" + rec_step_id + "_" + i);
                                intermediate_stage_2.set(i, intermediate_node_2);
                                // Connect right partition i to the new intermediate node
                                for (DP_State_Node r : right_i) intermediate_node_2.add_decision(r, r.toTuple().cost);
                            }
                            // Now connect the two intermediate nodes
                            intermediate_stage_1.get(j).add_decision(intermediate_stage_2.get(i), 0.0);
                        }
                    }
                }
            }
            else if (inequality.type.equals("IG"))
            {
                if (!left_i.isEmpty())
                {
                    // For a greater-than connect the current left partition to all the previous right partitions
                    for (int j = 0; j <= i - 1; j++)
                    {
                        right_j = inequality_partitions.get(j).getValue1();
                        if (!right_j.isEmpty())
                        {
                            // Connect left_i to right_j
                            // First check if their intermediate nodes have been created
                            if (intermediate_stage_1.get(i) == null)
                            {
                                // Materialize the intermediate node
                                DP_State_Node intermediate_node_1 = new DP_State_Node("X" + rec_step_id + "_" + i);
                                intermediate_stage_1.set(i, intermediate_node_1);
                                // Connect left partition i to the new intermediate node
                                for (DP_State_Node l : left_i) l.add_decision(intermediate_node_1, 0.0);
                            }
                            if (intermediate_stage_2.get(j) == null)
                            {
                                // Materialize the intermediate node
                                DP_State_Node intermediate_node_2 = new DP_State_Node("Y" + rec_step_id + "_" + j);
                                intermediate_stage_2.set(j, intermediate_node_2);
                                // Connect right partition j to the new intermediate node
                                for (DP_State_Node r : right_j) intermediate_node_2.add_decision(r, r.toTuple().cost);
                            }
                            // Now connect the two intermediate nodes
                            intermediate_stage_1.get(i).add_decision(intermediate_stage_2.get(j), 0.0);
                        }
                    }
                }                
            }
            else
            {
                System.err.println("Was expecting an inequality condition!");
                System.exit(1);
            }
            // Recursive call
            if (!left_i.isEmpty() && !right_i.isEmpty()) // for efficiency, not needed for correctness
                partition_inequality_rec(left_i, right_i, inequality);
        }
    }
}
