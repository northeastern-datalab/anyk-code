package entities.paths;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Stack;

/** 
 * Class that represents a set of decisions (outgoing edges in the DP graph), each one leading to a different target.
 * The source is not specified so that the object may be shared by multiple sources.
 * Contains data structures used by various any-k algorithms (initially null).
 * @author Nikolaos Tziavelis
 * @see entities.paths.DP_State_Node#share_decisions
*/
public class DP_DecisionSet
{
    /** 
     * The decisions stored as a list.
    */
    public ArrayList<DP_Decision> list_of_decisions;
    /** 
     * The decision that can lead to the optimal cost. It is computed during the bottom-up phase.
    */
    public DP_Decision best_decision;

    /** 
     * For Anyk-Part variants: flag is set to true when we are done computing successors for this set of decisions.
     * Useful when multiple parents share the same DecisionSet because only one parent has to compute the successor order.
     * @see algorithms.paths.DP_Part#get_successors
    */
    public boolean partial_order_computed;
    /** 
     * A priority queue that is used to incrementally sort the set of decisions by {@link algorithms.paths.DP_Lazy}.
    */    
    public PriorityQueue<DP_Decision> pq_lazysort;
    /** 
     * A stack that stores the indexes of the pivot elements used by {@link algorithms.paths.DP_Quick}.
    */    
    public Stack<Integer> pivot_stack;
    /** 
     * The index in the sorted list of decisions of the next element to be found by {@link algorithms.paths.DP_Quick}.
    */ 
    public int next_idx;
    /** 
     * For {@link algorithms.paths.DP_PartPlus}: 
	 * Stores the sorted order of suffixes (each suffix is a list of decisions) starting from a DecisionSet.
	 * Note that a decision from the DecisionSet itself *is* contained in the suffix.
	*/
    public List<DP_Suffix_Solution> sorted_suffixes;
    /** 
	 * For {@link algorithms.paths.DP_PartPlus}: 
     * A list of prefixes that are subscribed and waiting for the next suffix to be inserted in the sorted list.
     * Note that a decision from the DecisionSet itself is *not* contained in the prefix.
	*/
    public List<DP_Prefix_Solution_Follower> subscribers;  

    /** 
     * PQ data structure used by {@link algorithms.paths.DP_Recursive}.
    */
    public PriorityQueue<DP_Suffix_Solution> pq_rec;
    /** 
     * Needed for {@link algorithms.paths.DP_Recursive} (includes a decision from this set).
    */
    public DP_Suffix_Solution rec_best_suffix;

    public DP_DecisionSet()
    {
        this.list_of_decisions = new ArrayList<DP_Decision>();
        this.best_decision = null;
        this.partial_order_computed = false;
        this.pq_rec = null;
        this.rec_best_suffix = null;
        this.sorted_suffixes = null;
        this.subscribers = null;
    }
    
    /** 
     * @param new_decision Decision to be added to the set.
     */
    public void add(DP_Decision new_decision)
    {
        list_of_decisions.add(new_decision);
    }
}
