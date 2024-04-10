package algorithms.trees;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import algorithms.Configuration;
import entities.trees.TDP_Decision;
import entities.trees.TDP_DecisionSet;
import entities.trees.TDP_Prefix_Solution;
import entities.trees.TDP_Problem_Instance;
import entities.trees.TDP_Solution;
import entities.trees.TDP_State_Node;
import entities.trees.TDP_Thetajoin_Instance;
import entities.trees.Tree_ThetaJoin_Query;

/** 
 * An UNranked enumeration algorithm for a T-DP problem specified by a {@link entities.paths.TDP_Problem_Instance} object.
 * It is implemented as an iterator that is first initialized and then the method {@link #get_next} 
 * returns the next T-DP solution in an arbitrary order.
 * Unranked enumeration is implemented as a Lawler procedure without any PQs,
 * thus yielding constant delay (if the number of stages is constant).
 * @author Nikolaos Tziavelis
*/
public class TDP_Unranked_Iterator extends TDP_Iterator
{
	/** 
	 * Maintains all the prefix solutions that are not yet expanded.
	*/
	protected Queue<TDP_Prefix_Solution> global_q;
	/** 
	 * Stores the solution returned in the previous call of {@link #get_next}.
	 * This is required because candidates generated from the k'th solution 
	 * are found and inserted into the list in the (k+1)'th call.
	*/
	protected TDP_Prefix_Solution latest_solution;
	/** 
	 * The stage in which {@link #latest_solution} made a sidetrack (by going to a successor).
	 * It is the same as the length of the prefix that we popped in the previous call of {@link #get_next},
	 * that was then expanded to {@link #latest_solution}.
	 * By convention, latest_sidetrack_stage of the first solution is 1.
	*/	
	protected int latest_sidetrack_stage;
    /** 
     * An identifier for the object (the class provides a setter/getter for that).
    */
	public String name;

	/** 
	 * Initializes the iterator
	*/
	public TDP_Unranked_Iterator(TDP_Problem_Instance inst, Configuration conf)
    {
		super(inst, conf);
    	this.latest_solution = null;
		this.latest_sidetrack_stage = -1;
        // Initialize the global queue with an empty prefix (that contains only the starting node)
		this.global_q = new ArrayDeque<TDP_Prefix_Solution>();
    	// The prefix we start with contains only the first decision to go from starting_node to stage 1
    	// That way, we guarantee that in the second call we start taking successor solutions from stage 1
    	if (!this.instance.starting_node.get_decisions(0).isEmpty())	// Corner case: if no path can reach the terminal node, leave the queue empty
    	{
            // Stage 0 always has only one branch (index 0) that corresponds to stage 1
	    	TDP_Prefix_Solution starting_prefix = new TDP_Prefix_Solution(inst.starting_node.get_decisions(0).get(0));
	    	this.global_q.add(starting_prefix);    		
		}
    }

	/** 
     * Computes the next T-DP solution of {@link #instance} in an arbitrary order.
	 * @return TDP_Solution The next T-DP solution or null if there are no other solutions.
	 */
    public TDP_Solution get_next()
    {
    	TDP_Prefix_Solution curr, new_candidate, popped_solution, expanded_solution;

    	// If no latest_solution has been stored, then this is the first call to this method
    	// In that case, the queue contains only one prefix solution (the empty one) 
    	// In all other cases, we first need to generate successor solutions  	
    	if (latest_solution != null)
    	{
    		// We have to generate successor solutions for all stages which are between 
    		// the last stage and the stage where the latest solution made its final sidetrack (indicated by its length)
    		curr = latest_solution;
    		for (int sg = this.stages_no - 1; sg >= latest_sidetrack_stage; sg--)
    		{
    			// In stage sg, we take the successors of decisions between sg-1 and sg
    			// We consider the successors of the latest decision and generate one solution for each one
    			for (TDP_Decision succ : get_successors(curr.get_latest_decision()))
    			{
    				new_candidate = curr.create_successor(succ);
                    global_q.add(new_candidate);
				}
    			// Shorten the current solution by cutting off one decision
    			curr = curr.get_shorter();
			}
    	}

    	// Pop the best solution from the global queue
		popped_solution = global_q.poll();
		// If null is returned, then we have enumerated all solutions
		if (popped_solution == null) return null;
		// Record its length so that we know from which stage onwards 
		// we have to generate successor solutions in the next call
		latest_sidetrack_stage = popped_solution.length;
		// Expand the prefix solution to a full solution
		expanded_solution = expand(popped_solution);
		// Record it so that we consider its successor solutions in the next call
		latest_solution = expanded_solution;

		return expanded_solution;
	}

	/** 
	 * Expands the prefix solution into a full solution by following the first possible decision at each node.
	 * The full solution reaches a terminal node for each leaf stage.
	 * @param pref A prefix solution to be expanded.
	 * @return TDP_Prefix_Solution A new object that is a full solution.
	 */
    protected TDP_Prefix_Solution expand(TDP_Prefix_Solution pref)
    {
		int next_stage, parent_stage, branch_idx;
    	TDP_Prefix_Solution current;
		TDP_Decision next_best_decision;
		TDP_State_Node parent_node;
		current = pref;
		List<TDP_State_Node> node_list = pref.solutionToNodes_strict_order();
		// Recall that stage 0 contains the starting node which is not encoded in the solutions
    	while (current.length < this.stages_no - 1)
    	{
			next_stage = current.length + 1;
			// Find the parent state for the next decision
			parent_stage = this.instance.get_parent_stage(next_stage);
			// The starting state is not included so all stages are shifted by one
			parent_node = node_list.get(parent_stage - 1);
			branch_idx = this.instance.get_branch_index(next_stage);
			// Follow the best decision from the parent node
    		next_best_decision = parent_node.get_decisions(branch_idx).get(0);
			current = new TDP_Prefix_Solution(current, next_best_decision);
			// Add the latest node to the list
			node_list.add(next_best_decision.target);
    	}
    	// assert String.format("%.4f", current.future_cost).equals(String.format("%.4f", current.get_cost())); // Precision may be a problem here, hence the precision cutoff
		return current;
    }

	/** 
	 * Initialization of the data structures needed at each DP_DesicionSet
	 * in order to compute a (total) order among decisions.
	 * @param decisions The set of decisions whose data structures will be initialized.
	 */
    public void initialize_partial_order(TDP_DecisionSet decisions)
    {
        // Initialize the successor lists
        for (TDP_Decision dec : decisions.list_of_decisions)
            dec.successors = new ArrayList<TDP_Decision>(1);

            // Store a pointer to the next decision in the sorted list
            // The list of successors will contain only one element
            ArrayList<TDP_Decision> list_of_decisions = decisions.list_of_decisions;
            for (int i = 0; i < (list_of_decisions.size() - 1); i++)
                list_of_decisions.get(i).successors.add(list_of_decisions.get(i + 1));            
            // The successor list of the last decision will be empty
    }

	/** 
	 * Find the successors of a given decision among the other decisions in the {@link entities.trees.TDP_DecisionSet}.
	 * The way that successors are computed depends on the variant (subclass).
	 * @param dec The decision whose successors will be computed.
	 */
    public List<TDP_Decision> get_successors(TDP_Decision dec)
    {
        TDP_DecisionSet decision_set = dec.belongs_to();
        if (!decision_set.partial_order_computed)
        {
            initialize_partial_order(decision_set);
            decision_set.partial_order_computed = true;
        }
        return dec.successors;
    }

	public static void main(String args[]) 
    {
        // Run the example
        Tree_ThetaJoin_Query example_query = new Tree_ThetaJoin_Query(1);
        TDP_Thetajoin_Instance instance = new TDP_Thetajoin_Instance(example_query, null);
		// instance.bottom_up();
        TDP_Solution solution;
        TDP_Unranked_Iterator iter = new TDP_Unranked_Iterator(instance, null);
        for (int k = 1; k <= 1000; k++)
        {
            solution = iter.get_next();
            if (solution == null) break;
            else System.out.println(solution);
        }
    }
}