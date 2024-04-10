package algorithms.paths;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import algorithms.Configuration;
import entities.paths.DP_Decision;
import entities.paths.DP_DecisionSet;
import entities.paths.DP_Path_ThetaJoin_Instance;
import entities.paths.DP_Prefix_Solution;
import entities.paths.DP_Problem_Instance;
import entities.paths.DP_Solution;
import entities.paths.Path_ThetaJoin_Query;

/** 
 * An UNranked enumeration algorithm for a DP problem specified by a {@link entities.paths.DP_Problem_Instance} object.
 * It is implemented as an iterator that is first initialized and then the method {@link #get_next} 
 * returns the next DP solution in an arbitrary order.
 * Unranked enumeration is implemented as a Lawler procedure without any PQs,
 * thus yielding constant delay (if the number of stages is constant).
 * @author Nikolaos Tziavelis
*/
public class DP_Unranked_Iterator extends DP_Iterator
{
	/** 
	 * Maintains all the prefix solutions that are not yet expanded.
	*/
	protected Queue<DP_Prefix_Solution> global_q;
	/** 
	 * Stores the solution returned in the previous call of {@link #get_next}.
	 * This is required because candidates generated from the k'th solution 
	 * are found and inserted into the list in the (k+1)'th call.
	*/
	protected DP_Prefix_Solution latest_solution;
	/** 
	 * The stage in which {@link #latest_solution} made a sidetrack (by going to a successor).
	 * It is the same as the length of the prefix that we popped in the previous call of {@link #get_next},
	 * that was then expanded to {@link #latest_solution}.
	 * By convention, latest_sidetrack_stage of the first solution is 1.
	*/	
	protected int latest_sidetrack_stage;

    /** 
     * @return String The name of this class (used for help messages)
     */
    public static String getName()
    {
        String className = Thread.currentThread().getStackTrace()[2].getClassName(); 
        return className;
    }

    // Initializes the iterator
	public DP_Unranked_Iterator(DP_Problem_Instance inst, Configuration conf)
    {
    	super(inst, conf);
    	this.latest_solution = null;
		this.latest_sidetrack_stage = -1;
    	// Initialize the global queue with an empty prefix (that contains only the starting node)
		this.global_q = new ArrayDeque<DP_Prefix_Solution>();
    	// The prefix we start with contains only the first decision to go from starting_node to stage 1
    	// That way, we guarantee that in the second call we start taking successor solutions from stage 1
    	if (!this.instance.starting_node.get_decisions().isEmpty())	// Corner case: if no path can reach the terminal node, leave the queue empty
    	{
	    	DP_Prefix_Solution starting_prefix = new DP_Prefix_Solution(this.instance.starting_node.get_decisions().get(0));
	    	this.global_q.add(starting_prefix);    		
		}
    }

	/** 
     * Computes the next DP solution of {@link #instance} in an arbitrary order.
	 * @return DP_Solution The next DP solution or null if there are no other solutions.
	 */
    public DP_Solution get_next()
    {
    	DP_Prefix_Solution curr, new_sol, popped_solution, expanded_solution;

    	// If no latest_solution has been stored, then this is the first call to this method
    	// In that case, the queue contains only one prefix solution (the empty one).
    	// In all other cases, we first need to generate successor solutions.  	
    	if (latest_solution != null)
    	{
    		// We have to generate successor solutions for all stages which are between 
    		// the last stage and the stage where the latest solution made its final sidetrack (indicated by its length).
			curr = latest_solution;
			int last_stage = curr.length;
			for (int sg = last_stage; sg >= latest_sidetrack_stage; sg--)
    		{
   			    // In stage sg, we take the successors of decisions between sg-1 and sg.
    			// Consider the successors of the latest decision and generate one solution for each one.
    			for (DP_Decision succ : get_successors(curr.get_latest_decision()))
    			{
    				new_sol = curr.create_successor(succ);
					global_q.add(new_sol);
				}
    			// Shorten the current solution by cutting off one decision
    			curr = curr.get_shorter();
			}
        }

    	// Pop a solution from the global queue
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
	 * The full solution reaches a terminal node.
	 * @param pref A prefix solution to be expanded.
	 * @return DP_Prefix_Solution A new object that is a full solution.
	 */
    protected DP_Prefix_Solution expand(DP_Prefix_Solution pref)
    {
    	DP_Prefix_Solution current;
    	DP_Decision next_decision;
    	current = pref;
    	while (!current.latest_decision.target.is_terminal())
    	{
    		next_decision = current.latest_decision.target.get_decisions().get(0);
    		current = new DP_Prefix_Solution(current, next_decision);
		}
   	    return current;
    }

	/** 
	 * Initialization of the data structures needed at each DP_DesicionSet
	 * in order to compute a (total) order among decisions.
	 * @param decisions The set of decisions whose data structures will be initialized.
	 */
    public void initialize_partial_order(DP_DecisionSet decisions)
    {
        // Initialize the successor lists
        for (DP_Decision dec : decisions.list_of_decisions)
        	dec.successors = new ArrayList<DP_Decision>(1);

        // For each node, we store a pointer to the next decision
        // The list of successors will contain only one element
        ArrayList<DP_Decision> list_of_decisions = decisions.list_of_decisions;
        for (int i = 0; i < (list_of_decisions.size() - 1); i++)
            list_of_decisions.get(i).successors.add(list_of_decisions.get(i + 1));            
        // The successor list of the last decision will be empty
    }

	/** 
	 * Find the successor of a given decision among the other decisions in the {@link entities.paths.DP_DecisionSet}.
	 * @param dec The decision whose successor will be returned.
     * @return DP_Decision The successor decisions according to the (total) order.
	 */
    public List<DP_Decision> get_successors(DP_Decision dec)
    {
        DP_DecisionSet decision_set = dec.belongs_to();
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
        Path_ThetaJoin_Query example_query = new Path_ThetaJoin_Query(1);
        DP_Path_ThetaJoin_Instance instance = new DP_Path_ThetaJoin_Instance(example_query, null);
        DP_Solution solution;
        DP_Unranked_Iterator iter = new DP_Unranked_Iterator(instance, null);
        for (int k = 1; k <= 1000; k++)
        {
            solution = iter.get_next();
            if (solution == null) break;
            else System.out.println(solution);
        }
    }
}