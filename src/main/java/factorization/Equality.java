package factorization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javatuples.Pair;

import entities.Join_Predicate;
import entities.Tuple;
import entities.paths.DP_State_Node;
import util.Common;

/** 
 * Contains methods that are useful for handling equality conditions.
 * @author Nikolaos Tziavelis
*/
public class Equality
{
    /** 
     * Given two DP stages that correspond to the tuples of two relations 
     * and a list of equality join conditions,
     * constructs an efficient representation of the join results.
     * The method works be grouping tuples with the same join values together.
     * The resulting representation creates 1 intermediate layer of nodes between the relations.
     * This method needs O(n) time and space (with some assumption on hashing).
     * @param left The first relation/stage as a list of DP state-nodes.
     * @param right The second relation/stage as a list of DP state-nodes.
     * @param ps A list of equality predicates between the two relations/stages.
     */
    public static void factorize_equality(List<DP_State_Node> left, List<DP_State_Node> right, List<Join_Predicate> ps)
    {
        int intmd_cnt = 0;
        // First take care of the equalities that precede the single inequality condition
        for (Pair<List<DP_State_Node>,List<DP_State_Node>> equality_partition : 
                Equality.split_by_equality(left, right, ps))
        {
            // Left and right partition share the same join key
            List<DP_State_Node> left_partition = equality_partition.getValue0();
            List<DP_State_Node> right_partition = equality_partition.getValue1();

            // Connect the two partitions with an intermediate node
            DP_State_Node intermediate_node = new DP_State_Node("I" + intmd_cnt);
            intmd_cnt += 1;
            for (DP_State_Node l : left_partition) 
            {
                l.add_decision(intermediate_node, 0.0);
            }
            for (DP_State_Node r : right_partition) 
            {
                double cost = ((Tuple) r.state_info).cost;
                intermediate_node.add_decision(r, cost);
            }
        }
    }

    /** 
     * Given a list of equality conditions between the attributes of the left-right tuples,
     * this method partitions the tuples in disjoint components based on equality.
     * @param left  The nodes that correspond to the first relation.
     * @param right The nodes that correspond to the second relation.
     * @param equalities A list of equality predicates.
     * @return List<Pair<List<DP_State_Node>, List<DP_State_Node>>> A partitioning of the nodes as a list of left-right pairs.
     */
    public static List<Pair<List<DP_State_Node>,List<DP_State_Node>>> 
        split_by_equality(List<DP_State_Node> left, List<DP_State_Node> right, List<Join_Predicate> equalities)
    {
        List<Pair<List<DP_State_Node>,List<DP_State_Node>>> res = new ArrayList<Pair<List<DP_State_Node>,List<DP_State_Node>>>();

        if (equalities.isEmpty())
        {
            res.add(new Pair<List<DP_State_Node>,List<DP_State_Node>>(left, right));
        }
        else
        {
            // Turn the equality join conditions into int arrays of indexes
            int[] join_attributes_left = new int[equalities.size()];
            int[] join_attributes_right = new int[equalities.size()];
            for (int i = 0; i < equalities.size(); i++)
            {
                if (equalities.get(i).type != "E")
                {
                    System.err.println("Was expecting equality predicate!");
                    System.exit(1);
                }
                join_attributes_left[i] = equalities.get(i).attr_idx_1;
                join_attributes_right[i] = equalities.get(i).attr_idx_2;
            }
            // Hash both stages
            HashMap<List<Double>, List<DP_State_Node>> left_hash = Common.hash_stage(left, join_attributes_left);
            HashMap<List<Double>, List<DP_State_Node>> right_hash = Common.hash_stage(right, join_attributes_right);
            // Find the lists from both hash tables that share the same key
            // Iterate through the entries of the left hash table
            for (Map.Entry<List<Double>, List<DP_State_Node>> hash_entry : left_hash.entrySet()) 
            {
                List<Double> join_key = hash_entry.getKey();
                List<DP_State_Node> node_list_left = hash_entry.getValue();
            
                // Look up the joining values on the right hash table
                List<DP_State_Node> node_list_right = right_hash.get(join_key);
                if (node_list_right != null)
                {
                    res.add(new Pair<List<DP_State_Node>,List<DP_State_Node>>(node_list_left, node_list_right));
                }
            }
        }
        return res;
    }
}
