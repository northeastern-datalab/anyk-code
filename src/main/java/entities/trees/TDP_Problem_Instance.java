package entities.trees;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

// TODO: remove number of stages (as path case) ??

/** 
 * An instance of a T-DP problem realized as a multi-stage graph of state-nodes
 * where the stages are organized in a tree structure.
 * The edges between those nodes specify the dependencies between them.
 * There is a starting node and mutiple terminal nodes (the leaves of the tree). 
 * The terminal node is not explicitly materialized - we assume that all the nodes of the last stage
 * reach it with no cost.
 * A solution to the T-DP problem is a tree from the source node to the terminal nodes.
 * We assume that the stages are indexed consistently with the tree order,
 * i.e., if Si has child Sj then j > i.
 * This is an abstract class that captures all T-DP problems.
 * An implementing subclass has to initialize the stages and the nodes by providing an implementation for
 * the abstract method {@link #bottom_up}.
 * This method has to:
 * <ul>
 * <li>Instantiate nodes as {@link entities.trees.TDP_State_Node} objects.
 * <li>Set a single node as {@link #starting_node}.
 * <li>Set a number of nodes as terminals (those that belong to leaf stages). 
 * <li>Define the tree structure by using {@link #add_stage_to_tree}.
 * <li>Instantiate weighted edges between them as {@link entities.trees.TDP_Decision}.
 * <li>Compute the minimum achievable weight per node.
 * </ul>
 * <br>
 * @author Nikolaos Tziavelis
*/
public abstract class TDP_Problem_Instance
{
    /** 
     * Single source node located at stage 0.
    */
    public TDP_State_Node starting_node;
    /** 
     * The number of stages of the T-DP problem.
     * Includes the starting stage (but not the terminal ones).
    */
    public int stages_no;
    /** 
     * The T-DP states are partitioned in stages organized in a tree. 
     * This is represented as an adjacency list.
     * Every stage has a unique index and the list element with that index gives us the indexes of its children.
    */
    private List<List<Integer>> stage_tree;
    /** 
     * An auxiliary data structure that encodes the tree with reverse edges, 
     * i.e., it gives us the parent of each stage (its index).
    */
    private List<Integer> parents;
    /** 
     * An auxiliary data structure that stores the index of a stage as a branch from its parent
     * For example stage 2 could have 3 children stages: 5, 6, 7. Then the branch index of 6 is 1.
    */
    private List<Integer> branch_index;

    /** 
     * Creates a new problem instance by constructing all states.
    */
    public TDP_Problem_Instance()
    {     
        this.stage_tree = new ArrayList<List<Integer>>();
        this.parents = new ArrayList<Integer>();
        this.branch_index = new ArrayList<Integer>();
    }

    /** 
     * Goes through all states bottom-up in the tree order, 
     * while computing the minimum achievable cost and the best decision per state.
     * After the bottom-up phase, the optimal cost is the minimum achievable cost from starting_node.
     * The solution can be reconstructed by going top-down and following best decisions.
     */
    public void bottom_up()
    {
        // Trying to compute the optimal cost of the starting node will trigger a chain of recursive calls
        // that do the same for every reachable node
        compute_opt_cost(starting_node);
    }

    /** 
     * Recursively computes the optimal achievable cost starting from some specific node.
     * The computed cost is stored inside the node.
     * @see entities.paths.TDP_State_Node#opt_cost 
     * @param node The node we start from.
     */
    private void compute_opt_cost(TDP_State_Node node)
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

            // Sums up the best decisions for all the branches
            double opt_cost = 0.0;

            for (TDP_DecisionSet decisions : node.decisions)
            {
                // If the node shares the same decisions as another one for which we have already done the computation
                // then we can just look up the best decision
                if (decisions.best_decision == null)
                {
                    // Iterate through the available decisions and find the one with the minimum achievable cost
                    double best_cost = Double.POSITIVE_INFINITY;
                    TDP_Decision best_decision = null;
                    for (TDP_Decision dec : decisions.list_of_decisions)
                    {
                        // If the optimal cost of the decision's target hasn't been computed yet
                        // then do it now
                        compute_opt_cost(dec.target);
                        if (best_decision == null || dec.compareTo(best_decision) < 0) 
                        {
                            best_decision = dec;
                            best_cost = best_decision.opt_achievable_cost();
                        }
                    }
                    // Store it
                    decisions.best_decision = best_decision;
                    opt_cost += best_cost;
                }
                else
                {
                    opt_cost += decisions.best_decision.opt_achievable_cost();
                }
            }
            node.set_opt_cost(opt_cost);
        }
        return;
    }

    /** 
     * Computes the total number of T-DP solutions to the problem instance.
     * These are trees that begin at the starting node and end at terminal nodes.
     * @return BigInteger The number of solutions.
     */
    public BigInteger count_solutions()
    {
        return compute_no_solutions(this.starting_node, new HashMap<TDP_State_Node,BigInteger>());
    }

    /** 
     * Recursively computes the total number of solutions starting from some specific node.
     * The computed cost is stored in a map passed in each call as an argument 
     * so that we don't have to recompute it.
     * @param node The node we start from.
     * @param counts A map containing the counts for all the nodes that we have called this method on. 
     * @return long The total number of T-DP solutions starting from node.
     */
    private BigInteger compute_no_solutions(TDP_State_Node node, Map<TDP_State_Node,BigInteger> counts)
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
                res = BigInteger.ONE;

                for (TDP_DecisionSet decisions : node.decisions)
                {
                    BigInteger branch_count = BigInteger.ZERO;
                    for (TDP_Decision dec : decisions.list_of_decisions)
                    {
                        branch_count = branch_count.add(compute_no_solutions(dec.target, counts));
                    }
                    res = res.multiply(branch_count);
                }
            }
            // Store the count
            counts.put(node, res);
        }

        return res;
    }

    /** 
     * Adds a stage to the tree structure given that all of its children stages are provided.
     * @param stage The index of the stage to be added.
     * @param children_stages The indexes of its children.
     */
    protected void add_parent_stage_to_tree(int stage, List<Integer> children_stages)
    {
        this.stages_no += 1;

        // If the idx of the added stage is not the next one, grow the lists we keep up to that position
        while (stage_tree.size() <= stage) stage_tree.add(null);

        stage_tree.set(stage, children_stages);
        for (int child_idx = 0; child_idx < children_stages.size(); child_idx++)
        {
            while (parents.size() <= children_stages.get(child_idx)) parents.add(null);
            parents.set(children_stages.get(child_idx), stage);
            while (branch_index.size() <= children_stages.get(child_idx)) branch_index.add(null);
            branch_index.set(children_stages.get(child_idx), child_idx);
        }
    }

    /** 
     * Adds a stage to the tree structure by specifying its parent.
     * @param stage The index of the stage to be added.
     * @param parent_stage The index of its parent.
     */
    protected void add_child_stage_to_tree(int stage, int parent_stage)
    {
        this.stages_no += 1;

        // If the idx of the added stage is not the next one, grow the lists we keep up to that position
        while (stage_tree.size() <= parent_stage) stage_tree.add(null);
        if (stage_tree.get(parent_stage) == null) stage_tree.set(parent_stage, Arrays.asList(stage));
        else stage_tree.get(parent_stage).add(stage);

        while (branch_index.size() <= stage) branch_index.add(null);
        branch_index.set(stage, stage_tree.get(parent_stage).size() - 1);

        while (parents.size() <= stage) parents.add(null);
        parents.set(stage, parent_stage);
    }

    /** 
     * @param stage The index of a T-DP stage.
     * @return int The number of its children stages.
     */
    public int get_children_no(int stage)
    {
        return this.stage_tree.get(stage).size();
    }

    /** 
     * @param stage The index of a T-DP stage.
     * @return List<Integer> The indexes of its children stages or an empty list if it is a leaf stage.
     */
    public List<Integer> get_children_stages(int stage)
    {
        return this.stage_tree.get(stage);
    }

    /** 
     * Returns the parent stage of a particular stage
     * @param stage The index of a T-DP stage.
     * @return Integer The index of its parent or null for the root (0).
     */
    public Integer get_parent_stage(int stage)
    {
        return this.parents.get(stage);
    }

    /** 
     * @param stage The index of a T-DP stage.
     * @return Integer Its branch index
     * @see #branch_index
     */
    public Integer get_branch_index(int stage)
    {
        return this.branch_index.get(stage);
    }

    /** 
     * Reverses the ordering of the stages in the tree.
     */
    public void reverse_indexing()
    {
        List<List<Integer>> old_stage_tree = this.stage_tree;
        this.stage_tree = new ArrayList<List<Integer>>();
        this.parents = new ArrayList<Integer>();
        this.branch_index = new ArrayList<Integer>();
        for (int i = 0; i < this.stages_no; i += 1)
        {
            this.stage_tree.add(new ArrayList<Integer>());
            this.parents.add(null);
            this.branch_index.add(null);
        }

        for (int new_index = 0; new_index < this.stages_no; new_index += 1)
        {
            int old_index = this.stages_no - 1 - new_index;
            for (Integer old_child : old_stage_tree.get(old_index))
            {
                int new_child = this.stages_no - 1 - old_child;
                this.stage_tree.get(new_index).add(new_child);
                this.parents.set(new_child, new_index);
                this.branch_index.set(new_child, this.stage_tree.get(new_index).size() - 1);
            }
        }
    }

    /** 
     * Reorders the stages so that their ids follow a BFS ordering in the tree.
     */
    public void enforce_bfs_ordering()
    {
        List<List<Integer>> old_stage_tree = this.stage_tree;
        this.stage_tree = new ArrayList<List<Integer>>();
        this.parents = new ArrayList<Integer>();
        this.branch_index = new ArrayList<Integer>();
        for (int i = 0; i < this.stages_no; i += 1)
        {
            this.stage_tree.add(new ArrayList<Integer>());
            this.parents.add(null);
            this.branch_index.add(null);
        }

        // Compute a mapping from the current stage indexes to the new ones
        Map<Integer, Integer> stage_mapping = new HashMap<Integer, Integer>();
        Queue<Integer> bfs_q = new ArrayDeque<Integer>();
        bfs_q.add(0);
        int new_idx = 0;
        while (!bfs_q.isEmpty())
        {
            Integer current_stage = bfs_q.poll();
            if (!stage_mapping.containsKey(current_stage))
            {
                stage_mapping.put(current_stage, new_idx);
                new_idx += 1;
            } 
            for (Integer child : old_stage_tree.get(current_stage)) bfs_q.add(child);
        }

        // Create the data structures according to the new mapping
        for (int old_index = 0; old_index < this.stages_no; old_index += 1)
        {
            int new_index = stage_mapping.get(old_index);
            for (Integer old_child : old_stage_tree.get(old_index))
            {
                int new_child = stage_mapping.get(old_child);
                this.stage_tree.get(new_index).add(new_child);
                this.parents.set(new_child, new_index);
                this.branch_index.set(new_child, this.stage_tree.get(new_index).size() - 1);
            }
        }
    }

    /** 
     * DEPRECATED: to find all solutons use the batch algorithm instead (wih DFS).
     * Returns all possible solutions (trees from starting state to terminal states) via a BFS exporation
     * @see algorithms.trees.Tree_Batch
     * @return List<TDP_Prefix_Solution>
     */
    /*
    public List<TDP_Prefix_Solution> produce_all_solutions()
    {
        // First generate all the solutions of size 1
        List<TDP_Prefix_Solution> current_prefixes = new ArrayList<TDP_Prefix_Solution>();
        for (TDP_Decision decision_to_first_stage : starting_node.get_decisions(0))
            current_prefixes.add(new TDP_Prefix_Solution(decision_to_first_stage)); 

        // For stage sg, generate all the prefixes of size sg by extending the prefixes of length sg-1 in all possible ways
        // Notice that the parent of sg is not necessarily sg-1
        List<TDP_Prefix_Solution> prev_prefixes;
        int parent, branch;
        for (int sg = 2; sg < stages_no; sg++)
        {
            prev_prefixes = current_prefixes;
            current_prefixes = new ArrayList<TDP_Prefix_Solution>();

            parent = this.get_parent_stage(sg);
            branch = this.get_branch_index(sg);

            for (TDP_Prefix_Solution shorter_prefix : prev_prefixes) 
                for (TDP_Decision possible_decision : shorter_prefix.get_latest_decision().target.get_decisions()) 
                    current_prefixes.add(new TDP_Prefix_Solution(shorter_prefix, possible_decision)); 
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
        Set<TDP_State_Node> visited = new HashSet<TDP_State_Node>();
        Queue<TDP_State_Node> queue = new ArrayDeque<TDP_State_Node>();
        TDP_State_Node curr_node;
        String nodeStr, childStr;
        queue.add(starting_node);

        System.out.println("The complete graph is:");
        while (!queue.isEmpty())
        {
            curr_node = queue.remove();
            if (visited.contains(curr_node)) continue;
            visited.add(curr_node);
            nodeStr = curr_node.toString();
            if (curr_node == starting_node) nodeStr = "s0";
            System.out.println(nodeStr + " has " + curr_node.decisions.size() + " branches");
            for (int branch = 0; branch < curr_node.decisions.size(); branch++)
            {
                System.out.println("Branch " + branch + " :");
                for (TDP_State_Node child : curr_node.get_children_of_one_branch(branch))
                {
                    nodeStr = curr_node.toString();
                    childStr = child.toString();
                    if (curr_node == starting_node) nodeStr = "s0";
                    System.out.println(nodeStr + "->" + childStr + "\t\t Cost = " + curr_node.get_decision_cost(child, branch));
                    queue.add(child);
                }   
            }
        }
    }

}
