package entities.paths;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

/** 
 * An instance of a DP problem realized as a multi-stage graph of state-nodes 
 * where the stages are organized in a path (chain).
 * The edges between those nodes specify the dependencies between them.
 * There is a starting node and a terminal node. 
 * The terminal node is not explicitly materialized - we assume that all the nodes of the last stage
 * reach it with no cost.
 * A solution to the DP problem is a path from the source node to the terminal node.
 * This is an abstract class that captures all DP problems.
 * An implementing subclass has to do the following in its constuctor: 
 * <ul>
 * <li>Instantiate nodes as {@link entities.paths.DP_State_Node} objects.
 * <li>Set a single node as {@link #starting_node}.
 * <li>Set a number of nodes as terminals (those that belong to the last stage) 
 * with @link{entities.paths.DP_State_Node#set_to_terminal}.
 * <li>Instantiate weighted edges between them as {@link entities.paths.DP_Decision}.
 * </ul>
 * The {@link #bottom_up} method computes the minimum achievable weight per state/node.
 * By default, it works recursively, but can be specified to work iteratively with {@link #set_bottom_up_implementation}.
 * @author Nikolaos Tziavelis
*/
public abstract class DP_Problem_Instance
{
    /** 
     * Single source (there is also a single target but no need to represent it).
    */
    public DP_State_Node starting_node;
    /** 
     * Can be "recursive" or "iterative".
    */
    public String bottom_up_implementation = "recursive";

    /** 
     * Creates a new problem instance by constructing all states from the last stage to the first stage. 
    */
    public DP_Problem_Instance(){}

    /** 
     * Chooses how the bottom-up phase will be executed.
     * @param impl A String that can be "recursive" or "iterative".
    */
    public void set_bottom_up_implementation(String impl)
    {
        this.bottom_up_implementation = impl;
    }

    /** 
     * Goes through all states from the last stage to the first stage, 
     * while computing the minimum achievable cost and the best decision per state.
     * After the bottom-up phase, the optimal cost is the minimum achievable cost from starting_node.
     * The solution can be reconstructed by going top-down and following best decisions.
     */
    public void bottom_up()
    {
        if (bottom_up_implementation.equals("recursive")) bottom_up_rec();
        else if (bottom_up_implementation.equals("iterative")) bottom_up_iter();
        else
        {
            System.err.println("Bottom-up implementation not recognized!");
            System.exit(1);
        }
    }

    /** 
     * Recursive implementation of bottom-up phase.
     * More efficient if the depth of the recursion (i.e., the number of stages) is small.
     */
    public void bottom_up_rec()
    {
        // Trying to compute the optimal cost of the starting node will trigger a chain of recursive calls
        // that do the same for every reachable node
        compute_opt_cost(starting_node);
    }

    /** 
     * Recursively computes the optimal achievable cost starting from some specific node.
     * The computed cost is stored inside the node.
     * @see entities.paths.DP_State_Node#opt_cost 
     * @param node The node we start from.
     */
    private void compute_opt_cost(DP_State_Node node)
    {
        if (node.get_opt_cost() != Double.POSITIVE_INFINITY) return;
        else
        {
            // The optimal cost hasn't been computed yet
            // If this is a terminal node, then its cost is zero
            if (node.is_terminal())
            {
                node.set_opt_cost(0.0);        
                return;        
            }
            DP_DecisionSet decisions = node.decisions;
            // If the node shares the same decisions as another one for which we have already done the computation
            // then just look up the best decision
            if (decisions.best_decision != null)
            {
                node.set_opt_cost(decisions.best_decision.get_opt_cost());
            }
            else
            {
                // Iterate through the available decisions and find the one with the minimum achievable cost
                double best_cost = Double.POSITIVE_INFINITY;
                DP_Decision best_decision = null;
                for (DP_Decision dec : decisions.list_of_decisions)
                {
                    // If the optimal cost of the decision's target hasn't been computed yet
                    // then do it now
                    compute_opt_cost(dec.target);
                    if (best_decision == null || dec.compareTo(best_decision) < 0) 
                    {
                        best_decision = dec;
                        best_cost = best_decision.get_opt_cost();
                    }
                        
                }
                // Store it
                decisions.best_decision = best_decision;
                node.set_opt_cost(best_cost);
            }
        }
        return;
    }

    /** 
     * Iterative implementation of bottom-up phase.
     * Makes two passes, one to compute an appropriate order and then another in that order
     * to compute the optimal weights.
     * Not affected by a large number of stages.
     */
    public void bottom_up_iter()
    {
        List<DP_State_Node> nodes_topological_order = topological_order();
        bottom_up_compute(nodes_topological_order);
    }

    /** 
     * Produces a topological order of the state space graph.
     * The method works with an iterative DFS traversal.
     * @return A list of the vertices in topological order.
     */
    private List<DP_State_Node> topological_order()
    {
        DP_State_Node cur_node, child;

        // The visited nodes will be returned in a stack
        List<DP_State_Node> res = new ArrayList<DP_State_Node>();
        // The DFS procedure needs a stack for a non-recursive implementation
        Stack<DP_State_Node> dfs_stack = new Stack<DP_State_Node>();
        // We also need to record which nodes have been visited
        Set<DP_State_Node> visited = new HashSet<DP_State_Node>();
        // Initialize by going to the starting node
        dfs_stack.push(starting_node);

        while (!dfs_stack.isEmpty())
        {
            cur_node = dfs_stack.pop();

            if (visited.contains(cur_node))
            {
                // This node has already been visited
                // Since we popped it again, it means that we have also visited all of its children
                // So now is the time to add it to the topological sort order
                res.add(cur_node);
            }
            else
            {
                visited.add(cur_node);
                // Add the node again to the stack so that we know when we are done visiting its children
                dfs_stack.push(cur_node);
                // Add its children to the stack
                for (DP_Decision edge : cur_node.get_decisions())
                {
                    child = edge.target;
                    if (!visited.contains(child)) dfs_stack.push(child);
                }
            }
        }
        return res;
    }

    /** 
     * Given a topological order of the nodes s.t. a parent appears only if all of its children have already appeared
     * computes the optimal value for each node.
     * @param nodes_topological_order A bottom-up ordering of the state nodes.
     */
    private void bottom_up_compute(List<DP_State_Node> nodes_topological_order)
    {
        for (DP_State_Node cur_node : nodes_topological_order)
        {
            DP_DecisionSet decisions = cur_node.decisions;

            // If this is a terminal node, then its cost is zero
            if (cur_node.is_terminal()) cur_node.set_opt_cost(0.0);        
            // If the node shares the same decisions as another one for which we have already done the computation
            // then just look up the best decision
            else if (decisions.best_decision != null)
            {
                cur_node.set_opt_cost(decisions.best_decision.get_opt_cost());
            }
            else
            {
                // Iterate through the available decisions and find the one with the minimum achievable cost
                double best_cost = Double.POSITIVE_INFINITY;
                DP_Decision best_decision = null;
                for (DP_Decision dec : decisions.list_of_decisions)
                {
                    if (best_decision == null || dec.compareTo(best_decision) < 0) 
                    {
                        best_decision = dec;
                        best_cost = best_decision.get_opt_cost();
                    }
                }
                // Store it
                decisions.best_decision = best_decision;
                cur_node.set_opt_cost(best_cost);
            }
        }
    }

    /** 
     * Computes the total number of DP solutions to the problem instance.
     * These are paths that begin at the starting node and end at some terminal node.
     * With the current equi-join implementation, this has a quadratic cost for @link{entities.DP_Path_Equijoin_Instance}.
     * For @link{entities.DP_Path_ThetaJoin_Instance} where the intermediate nodes are explicitly materialized,
     * the cost is linear in the size of the graph.
     * @return BigInteger The number of solutions = paths in the state-space graph.
     */
    public BigInteger count_solutions()
    {
        return compute_no_solutions(this.starting_node, new HashMap<DP_State_Node,BigInteger>());
    }

    /** 
     * Recursively computes the total number of solutions starting from some specific node.
     * The computed cost is stored in a map passed in each call as an argument 
     * so that we don't have to recompute it.
     * @param node The node we start from.
     * @param counts A map containing the counts for all the nodes that we have called this method on. 
     * @return long The total number of DP solutions starting from node.
     */
    private BigInteger compute_no_solutions(DP_State_Node node, Map<DP_State_Node,BigInteger> counts)
    {
        BigInteger res;
        if ((res = counts.get(node)) == null)
        {
            // The number of solutions hasn't been computed yet
            // If this is a terminal node, then its number of solutions is 1
            if (node.is_terminal())
            {
                res = BigInteger.ONE;     
            }
            else
            {
                res = BigInteger.ZERO;
                DP_DecisionSet decisions = node.decisions;
                // Iterate through the available decisions and add up their counts
                for (DP_Decision dec : decisions.list_of_decisions)
                {
                    res = res.add(compute_no_solutions(dec.target, counts));
                }
            }
            // Store the count
            counts.put(node, res);
        }
        return res;
    }

    /** 
     * Computes the total size of the DP state graph as the sum of nodes (states) and edges (decisions) with a DFS traversal.
     * @return BigInteger The size of the DP graph (state space).
     */
    public BigInteger graph_size()
    {
        BigInteger res = BigInteger.ZERO;
        DP_State_Node cur_node, child;

        // The DFS procedure needs a stack for a non-recursive implementation
        Stack<DP_State_Node> dfs_stack = new Stack<DP_State_Node>();
        // We also need to record which nodes have been visited
        Set<DP_State_Node> visited = new HashSet<DP_State_Node>();
        // Initialize by going to the starting node
        dfs_stack.push(starting_node);

        while (!dfs_stack.isEmpty())
        {
            cur_node = dfs_stack.pop();
            visited.add(cur_node);

            // Increment the counter by one for the node
            res = res.add(BigInteger.ONE);
            // Increment the counter by one for each outgoing edge
            for (int i = 0; i < cur_node.get_decisions().size(); i++)
                res = res.add(BigInteger.ONE);         

            // Add its children to the stack
            for (DP_Decision edge : cur_node.get_decisions())
            {
                child = edge.target;
                if (!visited.contains(child)) dfs_stack.push(child);
            }
        }
        return res;
    }
    
    /** 
     * DEPRECATED: to find all solutons use the batch algorithm instead (wih DFS).
     * Returns all possible solutions (paths from starting state to terminal state) via a BFS exporation
     * @see algorithms.paths.Path_Batch
     * @return List<DP_Prefix_Solution>
     */
    /*
    public List<DP_Prefix_Solution> produce_all_solutions()
    {
        // First generate all the prefixes of length 1
        List<DP_Prefix_Solution> current_prefixes = new ArrayList<DP_Prefix_Solution>();
        for (DP_Decision decision_to_first_stage : starting_node.get_decisions())
            current_prefixes.add(new DP_Prefix_Solution(decision_to_first_stage)); 

        // For stage sg, generate all the prefixes of length sg by extending the prefixes of length sg-1 in all possible ways
        List<DP_Prefix_Solution> prev_prefixes;
        for (int sg = 2; sg <= stages_no; sg++)
        {
            prev_prefixes = current_prefixes;
            current_prefixes = new ArrayList<DP_Prefix_Solution>();
            for (DP_Prefix_Solution shorter_prefix : prev_prefixes) 
                for (DP_Decision possible_decision : shorter_prefix.get_latest_decision().target.get_decisions()) 
                    current_prefixes.add(new DP_Prefix_Solution(shorter_prefix, possible_decision)); 
        }
        return current_prefixes;
    }
    */

    /** 
     * Prints the entire constructed graph.
     * Useful for debugging.
     */
    public void print_edges()
    {
        Set<DP_State_Node> visited = new HashSet<DP_State_Node>();
        Queue<DP_State_Node> queue = new ArrayDeque<DP_State_Node>();
        DP_State_Node curr_node;
        String nodeStr, childStr;
        queue.add(starting_node);

        System.out.println("The complete graph is:");
        while (!queue.isEmpty())
        {
            curr_node = queue.remove();
            if (visited.contains(curr_node)) continue;
            visited.add(curr_node);
            for (DP_State_Node child : curr_node.get_children())
            {
                nodeStr = curr_node.toString();
                childStr = child.toString();
                if (curr_node == starting_node) nodeStr = "s0";
                System.out.println(nodeStr + "->" + childStr + "\t\t Cost = " + curr_node.get_decision_cost(child));
                queue.add(child);
            }              
        }
    }
}
