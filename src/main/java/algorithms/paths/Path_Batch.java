package algorithms.paths;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import algorithms.Configuration;
import entities.Tuple;
import entities.paths.DP_Decision;
import entities.paths.DP_Problem_Instance;
import entities.paths.DP_State_Node;
import entities.paths.Path_Query_Solution;

/** 
 * This algorithm produces all the solutions to a DP problem specified 
 * as a {@link entities.paths.DP_Problem_Instance} object.
 * The class has been customized for join problems only since it produces
 * {@link entities.paths.Path_Query_Solution} as results.
 * It is implemented as an iterator that is first initialized and then the method {@link #get_next} 
 * returns the next solution (in no particular order).
 * The method {@link #get_next} is implemented as DFS-style traversal on the state-space graph.
 * This implementation is conceptually the same as the two passes of the Yannakakis algorithm
 * <a href="https://dl.acm.org/doi/10.5555/1286831.1286840">https://dl.acm.org/doi/10.5555/1286831.1286840</a>.
 * <br><br>
 * IMPORTANT: Before using this class, the nodes and the edges of the DP state-space graph
 * must have already been initialized either by {@link entities.paths.DP_Problem_Instance#bottom_up}
 * or some other method.
 * @author Nikolaos Tziavelis
*/
public class Path_Batch extends DP_Anyk_Iterator
{
    /** 
     * A list that stores all the solutions to the DP problem.
    */
    public List<Path_Query_Solution> all_solutions;
    /** 
     * The index of the last returned solution by {@link #get_next}.
    */
    protected int current_index;

	public Path_Batch(DP_Problem_Instance inst, Configuration conf)
    {
        super(inst, conf);
        this.all_solutions = new ArrayList<Path_Query_Solution>();
        find_all_solutions();
        this.current_index = -1;
    }

	public Path_Batch(Path_Batch other)
    {
        super(other.instance, other.conf);
        this.all_solutions = other.all_solutions;
        this.current_index = -1;
    }

    /** 
     * Sorts the set of results in ascending cost order.
     */
    public void sort_results()
    {
        Collections.sort(this.all_solutions);
    }

    /** 
     * Computes the next DP solution of {@link #instance} in no particular order. 
     * @return Path_Query_Solution The next DP solution or null if there are no other solutions.
     */
    public Path_Query_Solution get_next()
    {
    	this.current_index++;
        if (current_index == this.all_solutions.size()) return null;
        return all_solutions.get(current_index);
    }

    private void find_all_solutions()
    {
        // Initiate a DFS from every state of the first stage
        for (DP_Decision decision_to_first_stage : instance.starting_node.get_decisions())
            DFS(decision_to_first_stage.target, new LinkedList<Tuple>());
    }

    private void DFS(DP_State_Node current_node, LinkedList<Tuple> current_tup_list)
    {
        // Add the tuple associated with the node
        if (current_node.state_info instanceof Tuple)
        {
            Tuple current_tuple = (Tuple) current_node.state_info;
            current_tup_list.add(current_tuple);
        }

        // The recursion ends at the last stage
        if (current_node.is_terminal())
        {
            Path_Query_Solution sol = new Path_Query_Solution(current_tup_list);
            this.all_solutions.add(sol);
        }
        // Else go to all the neighbors
        else
        {
            for (DP_Decision decision : current_node.get_decisions())
                DFS(decision.target, current_tup_list);
        }
        // If this node corresponds to a tuple, remove it from the list before returning
        if (current_node.state_info instanceof Tuple)
            current_tup_list.remove(current_tup_list.size() - 1);
    }
}