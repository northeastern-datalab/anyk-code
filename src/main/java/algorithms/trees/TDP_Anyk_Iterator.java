package algorithms.trees;

import algorithms.Configuration;
import entities.trees.TDP_Problem_Instance;
import entities.trees.TDP_Solution;

/** 
 * A ranked enumeration algorithm for a T-DP problem specified by a {@link entities.trees.TDP_Problem_Instance} object.
 * It is implemented as an iterator that is first initialized and then the method {@link #get_next} 
 * returns the next best T-DP solution in ranked order.
 * This is an abstract class: to instantiate it, a subclass that implements {@link #get_next} must be provided.
 * <br><br>
 * IMPORTANT: Before using this class, {@link entities.trees.TDP_Problem_Instance#bottom_up}
 * must have already been run on the T-DP instance.
 * @author Nikolaos Tziavelis
*/
public abstract class TDP_Anyk_Iterator extends TDP_Iterator
{
    // Initializes the iterator
	public TDP_Anyk_Iterator(TDP_Problem_Instance inst, Configuration conf)
    {
    	super(inst, conf);
    }

	/** 
     * Computes the next T-DP solution of {@link #instance} in ranked order (ascending cost). 
     * Ties are broken arbitrarily.
	 * @return TDP_Solution The next best T-DP solution or null if there are no other solutions.
	 */
    public abstract TDP_Solution get_next();
}