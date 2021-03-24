package algorithms.trees;

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
public abstract class TDP_Anyk_Iterator
{
    /** 
     * The T-DP problem to run any-k on.
    */
	TDP_Problem_Instance instance; 
    /** 
     * The number of stages of the TDP problem (partitions of states organized in a tree).
    */
	int stages_no;    
    /** 
     * An identifier for the object (the class provides a setter/getter for that).
    */             
	String name;

    /** 
     * @return String
     */
	public String get_name()
	{
		return this.name;
	}

    /** 
     * @param nam
     */
	public void set_name(String nam)
	{
		this.name = nam;
	}

    // Initializes the iterator
	public TDP_Anyk_Iterator(TDP_Problem_Instance inst)
    {
    	this.instance = inst;
    	this.stages_no = inst.stages_no;
    }

	/** 
     * Computes the next T-DP solution of {@link #instance} in ranked order (ascending cost). 
     * Ties are broken arbitrarily.
	 * @return TDP_Solution The next best T-DP solution or null if there are no other solutions.
	 */
    public abstract TDP_Solution get_next();

    /** 
     * @return String The name of this class (used for help messages)
     */
    public static String getName()
    {
        String className = Thread.currentThread().getStackTrace()[2].getClassName(); 
        return className;
    }
}