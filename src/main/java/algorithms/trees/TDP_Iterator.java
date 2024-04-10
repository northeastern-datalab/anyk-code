package algorithms.trees;

import algorithms.Configuration;
import entities.trees.TDP_Problem_Instance;
import entities.trees.TDP_Solution;

/** 
 * An enumeration algorithm for a T-DP problem specified by a {@link entities.trees.TDP_Problem_Instance} object.
 * It is implemented as an iterator that is first initialized and then the method {@link #get_next} 
 * returns the next T-DP solution.
 * This is an abstract class: to instantiate it, a subclass that implements {@link #get_next} must be provided.
 * @author Nikolaos Tziavelis
*/
public abstract class TDP_Iterator
{
    /** 
     * The T-DP problem to run any-k on.
    */
	TDP_Problem_Instance instance; 
    /** 
     * An object that contains various configuration parameters for the algorithm.
    */
    public Configuration conf;
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
	public TDP_Iterator(TDP_Problem_Instance inst, Configuration conf)
    {
    	this.instance = inst;
    	this.stages_no = inst.stages_no;
        this.conf = conf;
    }

	/** 
     * Computes the next T-DP solution of {@link #instance}. 
     * Ties are broken arbitrarily.
	 * @return TDP_Solution The next T-DP solution or null if there are no other solutions.
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