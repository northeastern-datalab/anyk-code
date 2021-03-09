package entities.trees;

import java.util.ArrayList;

/** 
 * A T-DP decision that corresponds to an edge in the tree-staged graph of T-DP.
 * The source is not specified so that the object may be shared by multiple sources.
 * @author Nikolaos Tziavelis
*/
public class TDP_Decision implements Comparable<TDP_Decision>
{
    /** 
     * The cost incurred immediately by this decision.
    */
    public double cost;
    /** 
     * The state we end up in by following this decision.
    */
    public TDP_State_Node target;
    /** 
     * The set of decisions that this decision belongs to.
    */
    public TDP_DecisionSet decision_set;
    /** 
     * The successors are decisions in the same stage and from the same parent.
     * They are given by a partial order which specifies which decisions 
     * we have to consider for getting the next best solution.
     * Used by {@link algorithms.trees.TDP_Part}.
    */
    public ArrayList<TDP_Decision> successors;

    /** 
     * Instantiates a new decision with a given cost.
     * We only know the resulting state of a decision (and not its origin) 
     * because it may be shared by multiple parents.
     * @see entities.trees.TDP_State_Node#share_decisions
    */
    public TDP_Decision(TDP_State_Node resulting_state, double immediate_cost, TDP_DecisionSet decision_set)
    {
        this.cost = immediate_cost;
        this.target = resulting_state;
        this.decision_set = decision_set;
    }

    /** 
     * @return double The minimum achievable cost if we take this decision.
     */
    public double opt_achievable_cost()
    {
        return this.cost + this.target.get_subtree_opt_cost();
    }

    
    /** 
     * Two decisions from the same parent state are compared according to the minimum achievable cost 
     * starting from the parent and (as a first step) taking each decision.
     * @param other
     * @return int
     */
    @Override
    public int compareTo(TDP_Decision other)
    {
        // compareTo should return < 0 if this is supposed to be less than other, 
        // > 0 if this is supposed to be greater than other  
        // and 0 if they are supposed to be equal
        double this_cost = this.cost + this.target.get_subtree_opt_cost();
        double other_cost = other.cost + other.target.get_subtree_opt_cost();
        if (this_cost < other_cost) return -1;
        else if (this_cost > other_cost) return 1;
        else
        {
            // If the costs are equal, break ties consistently
            if (this.hashCode() < other.hashCode()) return -1;
            else return 1;
        }
    }
    
    /** 
     * @return TDP_DecisionSet The set of decisions that this decision belongs to.
     */
    public TDP_DecisionSet belongs_to()
    {
        return decision_set;
    }

    /** 
     * Mainly for debugging.
     * @return String
     */
    @Override
    public String toString()
    {
        return target.toString();
    }
}
