package entities.trees;

import java.util.ArrayList;
import java.util.PriorityQueue;

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
     * PQ data structure used by {@link algorithms.trees.TDP_Recursive}.
    */
    public PriorityQueue<TDP_Subtree_Solution> pq_rec;
    /** 
     * Needed for {@link algorithms.trees.TDP_Recursive} (includes a decision from this set).
    */
    public TDP_Subtree_Solution rea_best_subtree;

    public TDP_DecisionSet()
    {
        list_of_decisions = new ArrayList<TDP_Decision>();
        partial_order_computed = false;
        pq_rec = null;
        rea_best_subtree = null;
    }

    /** 
     * Adds a decision to the set.
     * If the minimum achievable cost with this decision is better than the current ones, 
     * update best_decision.
     * @param new_decision Decision to be added to the set.
     * @return int If best_decision is updated, returns 1, else returns 0.
     */
    public int add(TDP_Decision new_decision)
    {
        list_of_decisions.add(new_decision);
        if (this.best_decision == null)
        {
            this.best_decision = new_decision;
            return 1;
        } 
        else if (new_decision.compareTo(this.best_decision) < 0) 
        {
            best_decision = new_decision;
            return 1;
        }
        return 0;
    }
}
