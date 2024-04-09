package entities.trees;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Stack;

/** 
 * Class that represents a set of decisions (edges) in the tree-staged graph of T-DP.
 * The source is not specified so that the object may be shared by multiple sources.
 * Contains data structures used by various any-k algorithms (initially null).
 * @author Nikolaos Tziavelis
 * @see entities.trees.TDP_State_Node#share_decisions
*/
public class TDP_DecisionSet
{
    /** 
     * The decisions stored as a list.
    */
    public ArrayList<TDP_Decision> list_of_decisions;
    /** 
     * The decision that can lead to the optimal cost. It is computed during the bottom-up phase.
    */
    public TDP_Decision best_decision;   

    /** 
     * For Anyk-Part variants: flag is set to true when we are done computing successors for this set of decisions.
     * Useful when multiple parents share the same DecisionSet because only one parent has to compute the successor order.
     * @see algorithms.trees.TDP_Part#get_successors
    */
    public boolean partial_order_computed;
    /** 
     * A priority queue that is used to incrementally sort the set of decisions by {@link algorithms.trees.TDP_Lazy}.
    */ 
    public PriorityQueue<TDP_Decision> pq_lazysort;
    /** 
     * A stack that stores the indexes of the pivot elements used by {@link algorithms.paths.TDP_Quick}.
    */    
    public Stack<Integer> pivot_stack;
    /** 
     * The index in the sorted list of decisions of the next element to be found by {@link algorithms.paths.TDP_Quick}.
    */ 
    public int next_idx;
    /** 
     * For {@link algorithms.trees.TDP_PartPlus}: 
	 * Stores the sorted order of suffixes (each suffix is a list of decisions) starting from a DecisionSet.
	 * Note that a decision from the DecisionSet itself *is* contained in the suffix.
	*/
    public List<TDP_Suffix_Solution> sorted_suffixes;
    /** 
	 * For {@link algorithms.trees.TDP_PartPlus}: 
     * A list of prefixes that are subscribed and waiting for the next suffix to be inserted in the sorted list.
     * Note that a decision from the DecisionSet itself is *not* contained in the prefix.
	*/
    public List<TDP_Prefix_Solution_Follower> subscribers;  

    /** 
     * PQ data structure used by {@link algorithms.trees.TDP_Recursive}.
    */
    public PriorityQueue<TDP_Subtree_Solution> pq_rec;
    /** 
     * Needed for {@link algorithms.trees.TDP_Recursive} (includes a decision from this set).
    */
    public TDP_Subtree_Solution rec_best_subtree;

    public TDP_DecisionSet()
    {
        this.list_of_decisions = new ArrayList<TDP_Decision>();
        this.partial_order_computed = false;
        this.pq_rec = null;
        this.rec_best_subtree = null;
    }

    /** 
     * @param new_decision Decision to be added to the set.
     */
    public void add(TDP_Decision new_decision)
    {
        this.list_of_decisions.add(new_decision);
    }
}
