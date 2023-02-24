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
 * where the join condition is a conjunction of equalities/inequalities/non-equalities/bands.
 * The resulting representation creates 1 intermediate layer of nodes between the relations.
 * If we have p conjuncts (not counting equalities), factorizing the conjunction takes O(n(logn)^p) time and space.
 * @author Nikolaos Tziavelis
*/
public class Binary_Partitioning
{
    /** 
     * An identifier for the intermediate nodes.
     */
    static long rec_step_id;
    /** 
     * Given two DP stages that correspond to the tuples of two relations 
     * and a list that contains a conjunction of join conditions,
     * constructs an efficient representation of the join results.
     * @param left The first relation/stage as a list of DP state-nodes.
     * @param right The second relation/stage as a list of DP state-nodes.
     * @param ps A conjunction of predicates with all the equalities at the start of the list.
     */
    public static void factorize_conjunction(List<DP_State_Node> left, List<DP_State_Node> right, List<Join_Predicate> ps)
    {
        rec_step_id = 0;
        // First take care of the equalities that precede all the other conditions
        List<Join_Predicate> equalities = null, rest = null; 
        int eq_idx = 0;
        for(eq_idx = 0; eq_idx < ps.size(); eq_idx++)
        {
            if (!ps.get(eq_idx).type.equals("E"))
            {
                break;
            }
        }
        equalities = ps.subList(0, eq_idx);
        rest = ps.subList(eq_idx, ps.size());
        
        for (Pair<List<DP_State_Node>,List<DP_State_Node>> equality_partition : 
                Equality.split_by_equality(left, right, equalities))
        {
            List<DP_State_Node> left_partition = equality_partition.getValue0();
            List<DP_State_Node> right_partition = equality_partition.getValue1();

            factorize_next_condition(left_partition, right_partition, rest);
        }
    }

    /** 
     * Performs flow control by choosing the appropriate method according to the predicate.
     * If no conditions remain, it means that the given tuples satisfy all the predicates.
     * In that case, this method connects them with an intermediate node.
     * @param left The first relation/stage as a list of DP state-nodes.
     * @param right The second relation/stage as a list of DP state-nodes.
     * @param ps A nconjunction of (inequality/non-equality/band) predicates. 
     */
    private static void factorize_next_condition(List<DP_State_Node> left, List<DP_State_Node> right, List<Join_Predicate> ps)
    {
        if (ps.isEmpty())
        {
            // Connect left to right
            // First materialize the intermediate node
            rec_step_id += 1;
            DP_State_Node intermediate_node = new DP_State_Node("X" + rec_step_id);
            // Then connect the two sublists with the intermedate node
            for (DP_State_Node l : left) l.add_decision(intermediate_node, 0.0);
            for (DP_State_Node r : right) intermediate_node.add_decision(r, r.toTuple().cost);
        }
        else
        {
            Join_Predicate next = ps.get(0);
            List<Join_Predicate> rest = ps.subList(1, ps.size());

            // Sort before calling the recursive partitioning algorithm
            // To maintain the old sorted order (if this is not the first predicate),
            // create a new sorted list
            List<DP_State_Node> copy_left = new ArrayList<DP_State_Node>(left);
            Common.sort_stage(copy_left, next.attr_idx_1);
            List<DP_State_Node> copy_right = new ArrayList<DP_State_Node>(right);
            Common.sort_stage(copy_right, next.attr_idx_2);

            if (next.type.equals("IL") || next.type.equals("IG"))
            {
                partition_inequality_rec(copy_left, copy_right, next, rest);
            } 
            else if (next.type.equals("N"))
            {
                // Handle the non-equality as two inequalities
                Join_Predicate less_than = new Join_Predicate("IL", next.attr_idx_1, next.attr_idx_2, next.parameter);
                partition_inequality_rec(copy_left, copy_right, less_than, rest);
                Join_Predicate greater_than = new Join_Predicate("IG", next.attr_idx_1, next.attr_idx_2, next.parameter);
                partition_inequality_rec(copy_left, copy_right, greater_than, rest);
            }
            else if (next.type.equals("B"))
            {
                // Translate the band into multiple inequalities
                for (Triplet<Join_Predicate,List<DP_State_Node>,List<DP_State_Node>> ineq_group : 
                    Band.band_grouping(copy_left, copy_right, next))
                {
                    partition_inequality_rec(ineq_group.getValue1(), ineq_group.getValue2(), ineq_group.getValue0(), rest);
                }
            }
            else
            {
                System.err.println("Join condition currently unsupported!");
                System.exit(1);
            }
        }
    }

    /** 
     * A recursive method for encoding the query results of an inequality join.
     * It works by creating 2 partitions, connecting the appropriate halfs and
     * <ul>
     * <li>recursively calling itself for each partition</li>
     * <li>recursively calling @link{factorize_next_condition} for the next join predicate</li>
     * </ul>
     * Before calling this method, the nodes have to be sorted by the predicate attributes.
     * @param left The first (sorted) subset of a relation/stage.
     * @param right The second (sorted) subset of a relation/stage.
     * @param inequality A single inequality predicate.
     * @param remaining A list of other predicates.
     */
    private static void partition_inequality_rec(List<DP_State_Node> left, List<DP_State_Node> right, 
        Join_Predicate inequality, List<Join_Predicate> remaining)
    {
        List<DP_State_Node> l1, l2, r1, r2;

        int distinct_cnt = Common.count_distinct_vals(left, right, inequality.attr_idx_1, inequality.attr_idx_2, inequality.parameter);
        // Base Case
        if (distinct_cnt <= 1) return;
        // Make 2 partitions
        List<Pair<List<DP_State_Node>,List<DP_State_Node>>> inequality_partitions = 
            Common.split_by_distinct(left, right, inequality, 2, distinct_cnt / 2);
        l1 = inequality_partitions.get(0).getValue0();
        l2 = inequality_partitions.get(1).getValue0();
        r1 = inequality_partitions.get(0).getValue1();
        r2 = inequality_partitions.get(1).getValue1();
        // Connect the 2 partitions according to the inequality type
        // If we have any predicates left, then we need to check them first
        // factorize_next_condition takes care of that
        if (inequality.type.equals("IL"))
        {
            // For a less-than connect l1 to r2
            if (!l1.isEmpty() && !r2.isEmpty())
            {
                factorize_next_condition(l1, r2, remaining);
            }
        }
        else if (inequality.type.equals("IG"))
        {
            // For a greater-than connect l2 to r1
            if (!l2.isEmpty() && !r1.isEmpty())
            {
                factorize_next_condition(l2, r1, remaining);
            }     
        }
        else
        {
            System.err.println("Was expecting an inequality condition!");
            System.exit(1);
        }
        // Recursive call for each of the 2 partitions
        if (!l1.isEmpty() && !r1.isEmpty()) // for efficiency, not needed for correctness
            partition_inequality_rec(l1, r1, inequality, remaining);
        if (!l2.isEmpty() && !r2.isEmpty()) // for efficiency, not needed for correctness
            partition_inequality_rec(l2, r2, inequality, remaining);
    }
}
