package entities.trees;

import java.util.ArrayList;
import java.util.List;

// TODO: remove number of stages (as path case)

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
 * Note: Contrary to the classes in {@link entities.paths}, the bottom-up phase here is not decoupled
 * from the graph construction: these happen simultaneously
 * (e.g. see {@link entities.trees.TDP_DecisionSet#add}).
 * @author Nikolaos Tziavelis
*/
public abstract class TDP_Problem_Instance
{
    /** 
     * Single source node located at stage 0.
    */
    public TDP_State_Node starting_node;
    /** 
     * The T-DP states are partitioned in stages.
    */
    List<List<TDP_State_Node>> stages;
    /** 
     * The number of stages of the T-DP problem.
     * Includes the starting stage (but not the terminal ones).
    */
    public int stages_no;
    /** 
     * A data structure (similar to an adjacency list) that stores the tree structure of the problem.
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
     * @param num_of_stages The number of the stages the T-DP problem will have.
     *                      Includes the starting stage (but not the terminal ones).
    */
    public TDP_Problem_Instance(int num_of_stages)
    {
        stages = new ArrayList<List<TDP_State_Node>>();

        // Tree
        stage_tree = new ArrayList<List<Integer>>(num_of_stages);
        parents = new ArrayList<Integer>(num_of_stages);
        branch_index = new ArrayList<Integer>(num_of_stages);
        for (int sg = 0; sg < num_of_stages; sg++)
        {
            stage_tree.add(new ArrayList<Integer>());
            parents.add(null);
            branch_index.add(null);
        }
    }

    /** 
     * The bottom-up phase of T-DP creates a T-DP state/node for each tuple. 
     * Every relation maps to a stage of T-DP.
     * The starting node is artificial and the terminal nodes are implicit
     * (To avoid the unnecessary overhead, we omit the terminal nodes).
     * The cost of transitioning to a tuple is the cost of that tuple.
     * After the bottom-up phase, the optimal cost is the minimum achievable cost from starting_node.
     * The optimal (top-1) solution can be reconstructed by going top-down and following best decisions.
     */
    public abstract void bottom_up();

    /** 
     * Adds a stage to the tree and updates all the necessary data structures.
     * @param stage The index of the stage to be added.
     * @param children_stages THe indexes of its children.
     */
    protected void add_stage_to_tree(Integer stage, List<Integer> children_stages)
    {
        stage_tree.set(stage, children_stages);
        for (int child_idx = 0; child_idx < children_stages.size(); child_idx++)
        {
            parents.set(children_stages.get(child_idx), stage);
            branch_index.set(children_stages.get(child_idx), child_idx);
        }
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
     * Use this after the bottom-up phase to print all the feasible decisions between the state nodes.
     * Feasible decisions = edges that lead to a node that can reach the terminal states.
     */
    public void print_edges()
    {
        int branches;
        List<TDP_State_Node> states;
        System.out.println("The complete graph is:");
        for (int sg = 0; sg <= stages_no; sg++)
        {
            states = this.stages.get(sg);
            for (TDP_State_Node node : states)
            {
                branches = node.num_branches;
                System.out.println(node + " has " + branches + " branches");
                for (int i = 0; i < branches; i++)
                {
                    System.out.println("Branch " + i + " :");
                    for (TDP_State_Node child : node.get_children_of_one_branch(i))
                    {
                        System.out.println(node + "->" + child + "\t\t Cost = " + node.get_decision_cost(child, i));
                    }   
                }
            }
        }
    }

}
