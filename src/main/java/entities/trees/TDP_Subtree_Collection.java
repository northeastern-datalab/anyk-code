package entities.trees;

import java.util.ArrayList;
import java.util.List;

/** 
 * A collection of subtree solutions encoded as one solution plus a pointer to a shorter prefix.
 * The class is used by {@link algorithms.trees.TDP_Recursive} to represent solutions of the same level of the tree 
 * that hang from the same node.
 * When many instances of this class are created, they have a shared prefix structure
 * and their instantiation is done in O(1) with the third constructor even if their size can be O(|stages|).
 * @author Nikolaos Tziavelis
*/
public class TDP_Subtree_Collection
{
    /** 
     * A prefix containing a subtree collection with @{link #size} - 1 solutions.
    */
    public TDP_Subtree_Collection prefix;
    /** 
     * The solution at the end of the collection, appended as a prefix.
    */
    public TDP_Subtree_Solution last_solution;
    /** 
     * The total cost of the solutions in the collection.
    */
    public Double cost;
    /** 
     * The number of solutions in the collection.
    */
    public int size;

    /** 
     * Constructor for an empty collection of solutions.
     * Useful for the leaves of the tree.
     * A subtree solution with one decision leading to a leaf node is represented
     * as one decision plus an empty collection of subtrees from there 
     * (in @{link entities.trees.TDP_Subtree_Solution}).
    */
    public TDP_Subtree_Collection()
    {
        this.prefix = null;
        this.last_solution = null;
        this.cost = 0.0;
        this.size = 0;
    }

    /** 
     * Constructor for a collection with only one solution.
     * Useful for initializing the Lawler procedure in each junction of the tree.
     * In that case, the argument is the optimal subtree from the first child and 
     * the collection will later be expanded optimally 
     * (by appending the rest of the optimal subtrees) 
     * when the solution containing this collection is popped from the Lawler PQ.
    */
    public TDP_Subtree_Collection(TDP_Subtree_Solution sol)
    {
        this.prefix = null;
        this.last_solution = sol;
        this.cost = sol.cost;
        this.size = 1;
    }

    /** 
     * Constructor that creates a new collection from a prefix concatenated with one more solution.
     * Works in O(1).
     * Useful for instantiating all the Lawler prefixes except the first one.
    */
    public TDP_Subtree_Collection(TDP_Subtree_Collection pref, TDP_Subtree_Solution sol)
    {
        this.prefix = pref;
        this.last_solution = sol;
        this.cost = pref.cost + sol.cost;
        this.size = pref.size + 1;
    }
    
    /** 
     * Returns a list of all the solutions in this collection by traversing the pointers to prefixes.
     * @return List<TDP_Subtree_Solution> The solutions as a list.
     */
    public List<TDP_Subtree_Solution> get_solutions()
    {
        List<TDP_Subtree_Solution> res = new ArrayList<TDP_Subtree_Solution>();
        TDP_Subtree_Collection current = this;
        // It could be that the collection is empty, i.e., even the last solution is null
        // In that case, return an empty list
        if (last_solution != null)
        {
            // Else traverse all the pointers back to shorter prefixes
            while (current != null)
            {
                // Add all the encountered solutions to the result
                res.add(current.last_solution);
                current = current.prefix;
            }
        }
        return res;   
    }

    /** 
     * A successor solution has the same size but replaces the last solution with an alternative.
     * The alternative solution is given as a parameter.
     * @param alt_solution The alternative solution.
     * @return TDP_Subtree_Collection A new object representing the collection with the alternative solution.
     */
    public TDP_Subtree_Collection create_successor(TDP_Subtree_Solution alt_solution)
    {
        // We have to check if this prefix has length 1 to avoid null pointers
        if (this.size == 1) return new TDP_Subtree_Collection(alt_solution);
        // If the length is > 1, then use the other constructor to replace the last solution
        else return new TDP_Subtree_Collection(this.prefix, alt_solution);
    }
}
