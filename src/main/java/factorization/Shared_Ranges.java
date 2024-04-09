package factorization;

import java.util.Arrays;
import java.util.List;

import org.javatuples.Pair;

import entities.Join_Predicate;
import entities.State_Node;
import util.Common;

/** 
 * Contains methods for efficiently representing (or factorizing) the query results of a join between 2 relations
 * where the join condition is an arbitrary number of equalities and a single inequality predicate.
 * The methods rely on sharing the same substructure for numbers that are smaller/larger.
 * The resulting representation creates a number of intermediate layers that can be O(n).
 * The space requirement is also O(n).
 * The running time is O(nlogn) since we sort the relations.
 * @author Nikolaos Tziavelis
*/
public class Shared_Ranges
{
    /** 
     * Given two DP stages that correspond to the tuples of two relations 
     * and a list that contains many equalities and a single inequality,
     * constructs an efficient representation of the join results.
     * The method relies on creating a hierarchy of nodes where each one is contained in the next.
     * @param left The first relation/stage as a list of DP state-nodes.
     * @param right The second relation/stage as a list of DP state-nodes.
     * @param ps A list of equality predicates and a single inequality predicate at the end of the list.
     */
    public static void factorize_inequality(List<? extends State_Node> left, List<? extends State_Node> right, List<Join_Predicate> ps)
    {
        Join_Predicate inequality = ps.get(ps.size() - 1);
        // First take care of the equalities that precede the single inequality condition
        for (Pair<List<? extends State_Node>,List<? extends State_Node>> equality_partition : 
                Equality.split_by_equality(left, right, ps.subList(0, ps.size() - 1)))
        {
            List<? extends State_Node> left_partition = equality_partition.getValue0();
            List<? extends State_Node> right_partition = equality_partition.getValue1();

            // Sort before calling the recursive partitioning algorithm
            Common.sort_stage(left_partition, inequality.attr_idx_1);
            Common.sort_stage(right_partition, inequality.attr_idx_2);

            share_inequality(left_partition, right_partition, inequality);
        }
    }

    /** 
     * A method for encoding the query results of an inequality join.
     * It works by creating a hierarchy of intermediate nodes connected in a chain.
     * Each of those nodes corresponds to a specific range of domain values.
     * Before calling this method, the nodes have to be sorted by the predicate attributes.
     * @param left The first (sorted) subset of a relation/stage.
     * @param right The second (sorted) subset of a relation/stage.
     * @param inequality A single inequality predicate.
     */
    private static void share_inequality(List<? extends State_Node> left, List<? extends State_Node> right, Join_Predicate inequality)
    {
        State_Node cur_node, cur_intermediate, new_intermediate;
        double cur_val;
        int left_idx = 0;
        int right_idx = 0;
        int attr_idx_left = inequality.attr_idx_1;
        int attr_idx_right = inequality.attr_idx_2;
        boolean visited_right;
        int intermediate_cnt = 0;

        if (left.size() == 0 || right.size() == 0) return;

        if (inequality.type.equals("IL"))
        {
            // Start with the first node from left
            cur_node = left.get(0);
            cur_val = cur_node.toTuple().values[attr_idx_left];
            // Skip all those from right that are less than or equal to that because they are not connected to anything
            // Always add the predicate offset to the right values
            while (right.get(right_idx).toTuple().values[attr_idx_right] + inequality.parameter <= cur_val)
            {
                right_idx += 1;
                if (right_idx >= right.size()) return;
            }
            cur_intermediate = Node_Connector.create_intermediate_node(String.valueOf(intermediate_cnt));
            intermediate_cnt += 1;

            // Both indexes now point to the nodes that should next be accessed
            // Start an iterative procedure that goes node by node
            visited_right = false;
            boolean read_left = false;
            boolean read_right = false;
            while (right_idx < right.size())
            {
                // Decide whether to read the next node from left or right
                if (left_idx >= left.size()) 
                    read_right = true;
                else if (left.get(left_idx).toTuple().values[attr_idx_left] < right.get(right_idx).toTuple().values[attr_idx_right] + inequality.parameter)
                    read_left = true;
                else
                    read_right = true;

                if (read_left)
                {
                    cur_node = left.get(left_idx);
                    if (visited_right)
                    {
                        // Create a new intermediate node and connect old intermediate to new intermediate
                        new_intermediate = Node_Connector.connect_intermediate_nodes(cur_intermediate, null, String.valueOf(intermediate_cnt));
                        intermediate_cnt += 1;
                        // Connect current node to the new intermediate
                        Node_Connector.connect_intermediate_nodes(cur_node, new_intermediate, null);

                        cur_intermediate = new_intermediate;
                        visited_right = false;
                    }
                    else
                    {
                        // Connect this node to the current intermediate one
                        Node_Connector.connect_intermediate_nodes(cur_node, cur_intermediate, null);
                    }
                    left_idx += 1;
                }
                else if (read_right)
                {
                    cur_node = right.get(right_idx);
                    visited_right = true;
                    // Connect current intermediate to this node
                    Node_Connector.connect_intermediate_to_right(cur_intermediate, Arrays.asList(cur_node), null);
                    right_idx += 1;
                }
                else
                {
                    System.err.println("Internal logic error!");
                    System.exit(1);
                }

                read_left = false;
                read_right = false;
            }

        }
        else if (inequality.type.equals("IG"))
        {
            System.err.println("Greater than currently unsupported!");
            System.exit(1);
        }
        else
        {
            System.err.println("Was expecting an inequality condition!");
            System.exit(1);
        }
    }
}
