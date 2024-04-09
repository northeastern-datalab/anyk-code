package factorization;

import java.util.List;

import entities.State_Node;
import entities.paths.DP_State_Node;
import entities.trees.TDP_State_Node;

/**
 * This class is responsible for connecting the nodes and creating the graph structure.
 * The static variables of this class have to be set prior to using it.
 * @author Nikolaos Tziavelis
*/
public class Node_Connector 
{
    /** 
     * "DP" or "T-DP".
     */
    public static String problem_setting = "T-DP";
    /** 
     * For T-DP, we need to know the number of branches of the parent.
     */
    public static int branch = 0;

    /** 
     * Creates a new intermediate node.
     * @param A string identifier for the new node.
     * @return State_Node The new node.
     */
    public static State_Node create_intermediate_node(String intermediate_string)
    {
        State_Node res = null;
        if (problem_setting.equals("T-DP")) res = new TDP_State_Node(1, intermediate_string);
        else res = new DP_State_Node(intermediate_string);
        return res;
    }

    /** 
     * Connects a layer of nodes belonging to a (parent/left) relation to an intermediate node. 
     * If the intermediate node doesn't already exist, instantiates a new one and returns it.
     * @param left The parent/left layer of nodes that act as the source.
     * @param right The intermediate node that acts as the target.
     * @param intermediate_string A string for the intermediate node (if one of the first two arguments is null).
     * @return State_Node The new intermediate node materialized from this method (can be null if no node is materialized).
     */
    public static State_Node connect_left_to_intermediate(List<? extends State_Node> left, State_Node right, String intermediate_string)
    {
        State_Node res = null;
        if (right == null) 
        {
            res = create_intermediate_node(intermediate_string);
            right = res;
        }
        for (State_Node node : left)
        {
            if (problem_setting.equals("T-DP")) ((TDP_State_Node) node).add_decision(branch, (TDP_State_Node) right, 0.0);
            else ((DP_State_Node) node).add_decision((DP_State_Node) right, 0.0);
        }
        return res;
    }

    /** 
     * Connects an intermediate node to a layer of nodes belonging to a (child/right) relation. 
     * If the intermediate node doesn't already exist, instantiates a new one and returns it.
     * @param left The intermediate node that acts as the source. 
     * @param right The child/right layer of nodes that act as the target.
     * @param intermediate_string A string for the intermediate node (if one of the first two arguments is null).
     * @return State_Node The new intermediate node materialized from this method (can be null if no node is materialized).
     */
    public static State_Node connect_intermediate_to_right(State_Node left, List<? extends State_Node> right, String intermediate_string)
    {
        State_Node res = null;
        if (left == null)
        {
            res = create_intermediate_node(intermediate_string);
            left = res;
        }
        for (State_Node node : right)
        {
            if (problem_setting.equals("T-DP")) ((TDP_State_Node) left).add_decision(0, (TDP_State_Node) node, node.toTuple().cost);
            else ((DP_State_Node) left).add_decision((DP_State_Node) node, node.toTuple().cost);
        }
        return res;
    }

    /** 
     * Connects an intermediate node to another itermediate node. 
     * If the intermediate nodes don't already exist, instantiates new ones and returns them.
     * Returns the target node if both are materialized.
     * @param left The intermediate node that acts as the source. 
     * @param right The intermediate node that acts as the target. 
     * @param intermediate_string A string for the intermediate node (if one of the first two arguments is null).
     * @return State_Node The new intermediate node materialized from this method (can be null if no node is materialized).
     */
    public static State_Node connect_intermediate_nodes(State_Node left, State_Node right, String intermediate_string)
    {
        State_Node res = null;
        if (left == null)
        {
            res = create_intermediate_node(intermediate_string);
            left = res;
        }
        if (right == null)
        {
            res = create_intermediate_node(intermediate_string);
            right = res;
        }
        if (problem_setting.equals("T-DP")) ((TDP_State_Node) left).add_decision(0, (TDP_State_Node) right, 0.0);
        else ((DP_State_Node) left).add_decision((DP_State_Node) right, 0.0);
        return res;
    }
}
