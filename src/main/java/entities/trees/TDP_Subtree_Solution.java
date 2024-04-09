package entities.trees;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import entities.Tuple;

/** 
 * A subtree representation of T-DP solutions that allows for sharing common subtrees. 
 * Each subtree solution consists of one decision and a collection of other subtree solutions from the target state
 * (one for each child in the stage tree).
 * @see algorithms.trees.TDP_Recursive
 * @author Nikolaos Tziavelis
*/
public class TDP_Subtree_Solution extends TDP_Solution implements Comparable<TDP_Subtree_Solution>
{
    /** 
     * The first decision contained in the solution. 
     * It leads to a node from which the other subtrees start.
    */
    private TDP_Decision parent_decision;
    /** 
     * A collection of subtree solutions starting from the target of {@link #parent_decision}.
     * This collection of subtrees is stored with a "shared prefix" structure.
     * @see entities.trees.TDP_Subtree_Collection
    */
    private TDP_Subtree_Collection subtrees; 
     /** 
     * Used by {@link algorithms.trees.TDP_Recursive}.
     * Since that algorithm uses the Lawler procedure on the possible subtrees,
     * every time we want to take the successors of a solution, we need to remember from which stage to start.
     * It is equal to the index of the last solution of the prefix (before we expand it).
    */   
    public int last_sidetrack;
     /** 
     * Used by {@link algorithms.trees.TDP_Recursive}.
     * A next pointer points to the next best subtree that has the same origin.
    */
    public TDP_Subtree_Solution next;

    /** 
     * Constructor for initializing a subtree solution of size 1.
     * All the solutions leading to a leaf node are instantiated here.
     * @param dec The only decision that this solution will contain.
    */
    public TDP_Subtree_Solution(TDP_Decision dec)
    {
        this.parent_decision = dec;
        this.subtrees = new TDP_Subtree_Collection();
        this.cost = dec.cost;
        // If the subtrees are empty, then we don't need Lawler
        this.last_sidetrack = Integer.MAX_VALUE;
        this.next = null;
    }

    /** 
     * Instantiates a subtree solution of length greater than 1 
     * from a given decision and the subsequent subtree solutions.
    */
    public TDP_Subtree_Solution(TDP_Decision parent_decision, TDP_Subtree_Collection subtrees)
    {
        this.parent_decision = parent_decision;
        this.subtrees = subtrees;
        // The cost of the subtree solution is the cost of the decision plus the cost of the subtree solutions
        // Because of the Lawler-style prefix structure of the subtree solutions, 
        // the collection might be incomplete at this point
        // In that case, we assume that they will be expanded optimally and we use their future optimal cost
        //System.out.println("Creating " + this.solutionToString());
        this.cost = parent_decision.cost + subtrees.cost + parent_decision.target.lawler_future_costs[subtrees.size];
        // The subtree collection will be expanded and reassigned by Lawler
        // This field will show us the stages for which we need to take successors
        this.last_sidetrack = subtrees.size - 1;
        this.next = null;
        //System.out.println("The cost of " + this.solutionToString() + " is computed as " + 
        //parent_decision.cost + " + " + subtrees.cost + " + " + future_cost);
    }

    /** 
     * @return List<TDP_Subtree_Solution> The list of subtrees without the first (parent) decision.
     * Note: If the subtrees haven't been expanded yet, then the list will only be a prefix.
     */
    public List<TDP_Subtree_Solution> get_shorter_subtree_solutions()
    {
    	return this.subtrees.get_solutions();
    }
    
    /** 
     * @return TDP_Decision The first (parent) decision of this subtree solution.
     */
    public TDP_Decision get_parent_decision()
    {
    	return this.parent_decision;
    }

    /** 
     * @return TDP_Subtree_Collection The collection of subtree solutions after the first (parent) decision.
     */
    public TDP_Subtree_Collection get_shorter_subtree_collection()
    {
    	return this.subtrees;
    }
    
    /** 
     * Sets the collection of subtree solutions after the parent decision.
     * Useful for reassigning the subtrees after they have been expanded 
     * by the Lawler procedure of {@link algorithms.trees.TDP_Recursive}.
     */
    public void set_shorter_subtree_collection(TDP_Subtree_Collection subtrees)
    {
        this.subtrees = subtrees;
    }

    public double get_final_cost()
    {
        return this.cost;
    }

    /** 
     * Uses a DFS traversal.
     * @return String A string representation of the solution. 
     */
    public String solutionToString()
    {
        List<TDP_Subtree_Solution> children_solutions;
    	StringBuilder builder = new StringBuilder();
        TDP_Subtree_Solution current;
        Deque<TDP_Subtree_Solution> stack = new ArrayDeque<TDP_Subtree_Solution>();
        stack.push(this);
        // Traverse the pointers to shorter suffixes and append the encountered tuples to the end of the string
    	while (!stack.isEmpty())
    	{
            current = stack.pop();
            builder.append(current.parent_decision.target.toString());
            // Push to the stack in reverse order to preserve the ordering
            children_solutions = current.get_shorter_subtree_solutions();
            for (TDP_Subtree_Solution child : children_solutions) stack.push(child);
    	}
    	return builder.toString();
    }

    /** 
     * Uses a DFS traversal.
     * @return List<Tuple> The list of tuples that the solution represents.
     */
    public List<Tuple> solutionToTuples()
    {
        List<TDP_Subtree_Solution> children_solutions;
        List<Tuple> res = new ArrayList<Tuple>();
        TDP_Subtree_Solution current;
        Deque<TDP_Subtree_Solution> stack = new ArrayDeque<TDP_Subtree_Solution>();
        stack.push(this);
        // Traverse the pointers to shorter suffixes and append the encountered tuples to the end of the string
    	while (!stack.isEmpty())
    	{
            current = stack.pop();
            if (current.parent_decision.target.state_info instanceof Tuple)
            {
                res.add((Tuple) current.parent_decision.target.state_info);
            }
            children_solutions = current.get_shorter_subtree_solutions();
            for (TDP_Subtree_Solution child : children_solutions) stack.push(child);
    	}
        return res;
    }

    public List<Tuple> solutionToTuples_strict_order()
    {
        // Since a prefix representation is used for the subtrees, the solutions are traversed in a reverse order
        // producing the correct order overall.
        return this.solutionToTuples();
    }

    /** 
     * @param other
     * @return int
     */
    // Two suffix solutions are compared according to their cost
    // This method is needed for inserting the suffix solutions into a PQ
    @Override
    public int compareTo(TDP_Subtree_Solution other)
    {
        // compareTo should return < 0 if this is supposed to be less than other, 
        // > 0 if this is supposed to be greater than other  
        // and 0 if they are supposed to be equal
    	if (this.cost < other.cost) return -1;
    	else if (this.cost> other.cost) return 1;
        else
        {
            // !!! We want consistent tie-breaking that agrees with the bottom_up phase
            // and the best_decisions that have been computed
            if (this.parent_decision.hashCode() < other.parent_decision.hashCode()) return -1;
            else return 1;
        }
    }
}
