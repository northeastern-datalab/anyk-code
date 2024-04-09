package factorization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javatuples.Pair;

import entities.Join_Predicate;
import entities.State_Node;
import util.Common;

/** 
 * Contains methods that are useful for handling equality conditions.
 * @author Nikolaos Tziavelis
*/
public class Equality
{
    /** 
     * Given two (T-)DP stages that correspond to the tuples of two relations 
     * and a list of equality join conditions,
     * constructs an efficient representation of the join results.
     * The method works be grouping tuples with the same join values together.
     * The resulting representation creates 1 intermediate layer of nodes between the relations.
     * This method needs O(n) time and space (with some assumption on hashing).
     * @param left The first relation/stage as a list of DP state-nodes.
     * @param right The second relation/stage as a list of DP state-nodes.
     * @param ps A list of equality predicates between the two relations/stages.
     */
    public static void factorize_equality(List<? extends State_Node> left, List<? extends State_Node> right, List<Join_Predicate> ps)
    {
        int intmd_cnt = 0;
        // First take care of the equalities that precede the single inequality condition
        for (Pair<List<? extends State_Node>,List<? extends State_Node>> equality_partition : 
                Equality.split_by_equality(left, right, ps))
        {
            // Left and right partition share the same join key
            List<? extends State_Node> left_partition = equality_partition.getValue0();
            List<? extends State_Node> right_partition = equality_partition.getValue1();

            // Connect the two partitions with an intermediate node
            State_Node intermediate_node = Node_Connector.connect_left_to_intermediate(left_partition, null, "I" + intmd_cnt);
            intmd_cnt += 1;
            Node_Connector.connect_intermediate_to_right(intermediate_node, right_partition, null);
        }
    }

    /** 
     * Given a list of equality conditions between the attributes of the left-right tuples,
     * this method partitions the tuples in disjoint components based on equality.
     * @param left  The nodes that correspond to the first relation.
     * @param right The nodes that correspond to the second relation.
     * @param equalities A list of equality predicates.
     * @return List<Pair<List<State_Node>, List<State_Node>>> A partitioning of the nodes as a list of left-right pairs.
     */
    public static List<Pair<List<? extends State_Node>,List<? extends State_Node>>> 
        split_by_equality(List<? extends State_Node> left, List<? extends State_Node> right, List<Join_Predicate> equalities)
    {
        List<Pair<List<? extends State_Node>,List<? extends State_Node>>> res = new ArrayList<Pair<List<? extends State_Node>,List<? extends State_Node>>>();

        if (equalities.isEmpty())
        {
            res.add(new Pair<List<? extends State_Node>,List<? extends State_Node>>(left, right));
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
            HashMap<List<Double>, List<State_Node>> left_hash = Common.hash_stage(left, join_attributes_left);
            HashMap<List<Double>, List<State_Node>> right_hash = Common.hash_stage(right, join_attributes_right);
            // Find the lists from both hash tables that share the same key
            // Iterate through the entries of the left hash table
            for (Map.Entry<List<Double>, List<State_Node>> hash_entry : left_hash.entrySet()) 
            {
                List<Double> join_key = hash_entry.getKey();
                List<State_Node> node_list_left = hash_entry.getValue();
            
                // Look up the joining values on the right hash table
                List<State_Node> node_list_right = right_hash.get(join_key);
                if (node_list_right != null)
                {
                    res.add(new Pair<List<? extends State_Node>,List<? extends State_Node>>(node_list_left, node_list_right));
                }
            }
        }
        return res;
    }
}
