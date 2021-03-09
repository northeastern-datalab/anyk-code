package algorithms.trees;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import entities.trees.TDP_Decision;
import entities.trees.TDP_DecisionSet;
import entities.trees.TDP_Prefix_Solution;
import entities.trees.TDP_Problem_Instance;
import entities.trees.TDP_Solution;
import entities.trees.TDP_State_Node;

// TODO: make the algorithm independent of stages_no (as path case)

/** 
 * Implementation of Anyk-Part for T-DP, a ranked enumeration algorithm that relies on the Lawler procedure.
 * The different variants are implemented as subclasses, each one implementing the abstract methods differently.
 * The algorithm is almost identical to the path case ({@link algorithms.paths.DP_Part})
 * and simply imposes an ordering to the stages of the tree, 
 * according to which prefixes are generated, successors are taken, etc.<br>
 * Brief description of the algorithm: 
 * A global PQ holds candidate solutions that are incomplete (prefixes)
 * but their future cost can be computed in constant time after the T-DP bottom-up phase.
 * In each iteration a prefix is popped, expanded and returned as the top-k'th solution.
 * To get the top-(k+1)'th solution, 
 * we generate new candidates from the last returned solution by taking its successors at each stage.
 * The successors are computed according to a partial order on the decisions available at each state.
 * Each variant has a different way of computing a partial order among the available decisions.
 * This partial order is the one that specifies the successors.
 * The variants are concrete subclasses of this abstract class and share the core method {@link #get_next}.
 * @see <a href="https://doi.org/10.1287/mnsc.18.7.401">Lawler procedure</a>
 * @author Nikolaos Tziavelis
*/
public abstract class TDP_Part extends TDP_Anyk_Iterator
{
	/** 
	 * Maintains all the prefix solutions that are candidates for the next best solution.
	*/
	protected PriorityQueue<TDP_Prefix_Solution> global_pq;
	/** 
	 * Stores the solution returned in the previous call of {@link #get_next}.
	 * This is required because candidates generated from the top-k'th solution 
	 * are found and inserted into the PQ in the (k+1)'th call.
	*/
	protected TDP_Prefix_Solution latest_solution;
	/** 
	 * The stage in which {@link #latest_solution} made a sidetrack (by going to a successor).
	 * It is the same as the length of the prefix that we popped in the previous call of {@link #get_next},
	 * that was then expanded to {@link #latest_solution}.
	 * By convention, latest_sidetrack_stage of the top-1 solution is 1.
	*/	
	protected int latest_sidetrack_stage;
	/** 
	 * Used to specify different implementations of {@link #global_pq}.
	*/
	protected String heap_type;

	/** 
	 * @param inst The T-DP problem to run any-k on.
	 * @param heap_type A parameter that is used to select an implementation for the global PQ. 
	 * 					By default (null), a binary heap is used.
	*/
	public TDP_Part(TDP_Problem_Instance inst, String heap_type)
    {
    	super(inst);
    	this.latest_solution = null;
		this.latest_sidetrack_stage = -1;
		// Choose a heap implementation
		/*
		if (heap_type == null)
		{
			if (stages_no < 10) this.heap_type = "Lib_BHeap";
			else this.heap_type = "Lib_BHeap_Bulk";
		}
		*/
		this.heap_type = heap_type;
		// Just use a simple binary heap

    	// Initialize the global PQ with an empty prefix (that contains only the starting node)
		// this.global_pq = new Priority_Queue<DP_Prefix_Solution>(this.heap_type);
		this.global_pq = new PriorityQueue<TDP_Prefix_Solution>();
    	// The prefix we start with contains only the best decision to go from starting_node to stage 1
    	// That way, we guarantee that for top-2 we start taking successor solutions from stage 1
    	if (instance.starting_node.get_subtree_opt_cost() != Double.POSITIVE_INFINITY)	// Corner case: if no path can reach the terminal node, leave the pq empty
    	{
            // Stage 0 always has only one branch (index 0) that corresponds to stage 1
            TDP_Decision best_from_start = instance.starting_node.get_best_decision(0);
	    	TDP_Prefix_Solution starting_prefix = new TDP_Prefix_Solution(best_from_start);
	    	this.global_pq.add(starting_prefix);    		
		}
    }

    // Returns the next solution in the ranked order
    // Returns null if there are no other solutions
    public TDP_Solution get_next()
    {
    	TDP_Prefix_Solution curr, new_candidate, popped_solution, expanded_solution;
    	List<TDP_Prefix_Solution> new_candidates = new ArrayList<TDP_Prefix_Solution>();

    	// If no latest_solution has been stored, then this is the first call to this method
    	// In that case, the PQ contains only one prefix solution (the empty one) 
    	// which when expanded will give the top-1 solution  
    	// In all other cases, we first need to generate successor solutions  	
    	if (latest_solution != null)
    	{
    		// We have to generate successor solutions for all stages which are between 
    		// the last stage and the stage where the latest solution made its final sidetrack (indicated by its length)
    		curr = latest_solution;
    		for (int sg = stages_no; sg >= latest_sidetrack_stage; sg--)
    		{
    			// In stage sg, we take the successors of decisions between sg-1 and sg
    			// We consider the successors of the latest decision and generate one solution for each one
    			for (TDP_Decision succ : get_successors(curr.get_latest_decision()))
    			{
    				new_candidate = curr.create_successor(succ);
    				new_candidates.add(new_candidate);
				}
    			// Shorten the current solution by cutting off one decision
    			curr = curr.get_shorter();
			}
			// If the PQ is empty heapify instead of pushing
			// Especially helpful in the second iteration of TDP_Min
			//System.out.println("Adding to the PQ: ");
			//for (TDP_Prefix_Solution cand : new_candidates) System.out.println("\t\t" + cand.solutionToTuples_strict_order());
            if (global_pq.isEmpty())
                // global_pq = new Priority_Queue<DP_Prefix_Solution>(heap_type, new_candidates);
                global_pq = new PriorityQueue<TDP_Prefix_Solution>(new_candidates);
            else
                // global_pq.bulk_push(new_candidates);
                add_candidates(new_candidates);
    	}

    	// Pop the best solution from the global PQ
		// popped_solution = global_pq.pop();
		popped_solution = global_pq.poll();
		// If null is returned, then we have enumerated all solutions
		if (popped_solution == null) return null;
		//System.out.println("Popped " + popped_solution.solutionToTuples_strict_order() + " with future cost = " + popped_solution.get_future_cost());
		// Record its length so that we know from which stage onwards 
		// we have to generate successor solutions in the next call
		latest_sidetrack_stage = popped_solution.length;
		// Expand the prefix solution to a full solution
		expanded_solution = expand(popped_solution);
		//System.out.println("Expanded to " + expanded_solution.solutionToTuples_strict_order());
		// Record it so that we consider its successor solutions in the next call
		latest_solution = expanded_solution;

		return expanded_solution;
	}

	/** 
	 * Expands the prefix solution into a full solution by following an optimal sequence of decisions.
	 * The full solution reaches a terminal node for each leaf stage.
	 * 
	 * @param pref A prefix solution to be expanded optimally.
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
    	while (current.length != instance.stages_no)
    	{
			next_stage = current.length + 1;
			// Find the parent state for the next decision
			parent_stage = instance.get_parent_stage(next_stage);
			// The starting state is not included so all stages are shifted by one
			parent_node = node_list.get(parent_stage - 1);
			branch_idx = instance.get_branch_index(next_stage);
			// Follow the best decision from the parent node
    		next_best_decision = parent_node.get_best_decision(branch_idx);
			current = new TDP_Prefix_Solution(current, next_best_decision);
			// Add the latest node to the list
			node_list.add(next_best_decision.target);
    	}
    	// assert String.format("%.4f", current.future_cost).equals(String.format("%.4f", current.get_cost())); // Precision may be a problem here, hence the precision cutoff
		return current;
    }
	
	private void add_candidates(List<TDP_Prefix_Solution> cands)
	{
		for (TDP_Prefix_Solution cand : cands)
			global_pq.add(cand);
	}

	/** 
	 * Initialization of the data structures needed at each TDP_DesicionSet
	 * in order to compute a partial order among decisions.
	 * @param decisions The set of decisions whose data structures will be initialized.
	 */
    public abstract void initialize_partial_order(TDP_DecisionSet decisions);

	/** 
	 * Find the successors of a given decision among the other decisions in the {@link entities.trees.TDP_DecisionSet}.
	 * The way that successors are computed depends on the variant (subclass).
	 * @param dec The decision whose successors will be computed.
	 */
    public abstract List<TDP_Decision> get_successors(TDP_Decision dec);
}