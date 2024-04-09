package algorithms.trees;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import algorithms.Configuration;
import entities.trees.Star_Equijoin_Query;
import entities.trees.TDP_Decision;
import entities.trees.TDP_Problem_Instance;
import entities.trees.TDP_Solution;
import entities.trees.TDP_Star_Equijoin_Instance;
import entities.trees.TDP_State_Node;
import entities.trees.Tree_Query_Solution;

/** 
 * This algorithm produces all the solutions to a T-DP problem specified 
 * as a {@link entities.trees.TDP_Problem_Instance} object.
 * The class has been customized for join problems only since it produces
 * {@link entities.trees.Tree_Query_Solution} as results.
 * It is implemented as an iterator that is first initialized and then the method {@link #get_next} 
 * returns the next solution (in no particular order).
 * The method {@link #get_next} is implemented as DFS-style traversal on the state-space graph.
 * This implementation is conceptually the same as the two passes of the Yannakakis algorithm
 * <a href="https://dl.acm.org/doi/10.5555/1286831.1286840">https://dl.acm.org/doi/10.5555/1286831.1286840</a>.
 * <br><br>
 * IMPORTANT: Before using this class, the nodes and the edges of the T-DP state-space graph
 * must have already been initialized either by {@link entities.trees.TDP_Problem_Instance#bottom_up}
 * or some other method.
 * @author Nikolaos Tziavelis
*/
public class Tree_Batch extends TDP_Anyk_Iterator
{
    /** 
     * A list that stores all the solutions to the T-DP problem.
    */
    List<Tree_Query_Solution> all_solutions;
    /** 
     * The index of the last returned solution by {@link #get_next}.
    */
    int current_index;

	public Tree_Batch(TDP_Problem_Instance inst, Configuration conf)
    {
        super(inst, conf);
        this.all_solutions = new ArrayList<Tree_Query_Solution>();
        find_all_solutions();
        this.current_index = -1;
    }

    /** 
     * Computes the next T-DP solution of {@link #instance} in no particular order. 
     * @return Tree_Query_Solution The next T-DP solution or null if there are no other solutions.
     */
    public Tree_Query_Solution get_next()
    {
    	this.current_index++;
        if (current_index == this.all_solutions.size()) return null;
        return all_solutions.get(current_index);
    }

    private void find_all_solutions()
    {
        // Initiate a DFS from every state of the first stage
        for (TDP_Decision decision_to_first_stage : instance.starting_node.get_decisions(0))
            DFS(decision_to_first_stage.target, new LinkedList<TDP_State_Node>());
    }

    private void DFS(TDP_State_Node current_node, LinkedList<TDP_State_Node> current_node_list)
    {
        // Add the tuple associated with the node
        current_node_list.add(current_node);

        // The recursion ends at the last stage
        if (current_node_list.size() == instance.stages_no - 1)
        {
            Tree_Query_Solution sol = new Tree_Query_Solution(current_node_list);
            this.all_solutions.add(sol);
        }
        // Else go to the next stage
        else
        {
            // Since this is TDP, the states of the next stage that we visit are determined by the corresponding
            // parent of the next stage
            int next_stage = current_node_list.size() + 1;
            int parent_stage = instance.get_parent_stage(next_stage);
            TDP_State_Node parent_node = current_node_list.get(parent_stage - 1);
            int branch_idx = instance.get_branch_index(next_stage);
            for (TDP_Decision decision : parent_node.get_decisions(branch_idx))
                DFS(decision.target, current_node_list);
        }
        // Remove the tuple before returning
        current_node_list.remove(current_node_list.size() - 1);
    }

    public static void main(String args[]) 
    {
        // Run the example
        Star_Equijoin_Query example_query = new Star_Equijoin_Query();
        TDP_Star_Equijoin_Instance instance = new TDP_Star_Equijoin_Instance(example_query);
        instance.bottom_up();
        TDP_Anyk_Iterator iter = new Tree_BatchSorting(instance, null);
        
        System.out.println("All solutions:");
        TDP_Solution sol;
        while ((sol = iter.get_next()) != null)
            System.out.println(sol.solutionToString() + "  Cost = " + sol.get_cost());
    }

}