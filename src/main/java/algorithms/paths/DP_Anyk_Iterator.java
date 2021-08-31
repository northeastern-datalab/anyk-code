package algorithms.paths;

import algorithms.Configuration;
import entities.paths.DP_Problem_Instance;
import entities.paths.DP_Solution;

/** 
 * A ranked enumeration algorithm for a DP problem specified by a {@link entities.paths.DP_Problem_Instance} object.
 * It is implemented as an iterator that is first initialized and then the method {@link #get_next} 
 * returns the next best DP solution in ranked order.
 * This is an abstract class: to instantiate it, a subclass that implements {@link #get_next} must be provided.
 * <br><br>
 * IMPORTANT: Before using this class, {@link entities.paths.DP_Problem_Instance#bottom_up}
 * must have already been run on the DP instance.
 * @author Nikolaos Tziavelis
*/
public abstract class DP_Anyk_Iterator
{
    /** 
     * The DP problem to run any-k on.
    */
    public DP_Problem_Instance instance;
    /** 
     * The DP problem to run any-k on.
    */
    public Configuration conf;
    /** 
     * An identifier for the object (the class provides a setter/getter for that).
    */
	public String name;

    /** 
     * @return String
     */
    public String get_name()
	{
		return this.name;
	}

    /** 
     * @param name
     */
    public void set_name(String name)
	{
		this.name = name;
	}

    // Initializes the iterator
	public DP_Anyk_Iterator(DP_Problem_Instance inst, Configuration conf)
    {
    	this.instance = inst;
        this.conf = conf;
    }

	/** 
     * Computes the next DP solution of {@link #instance} in ranked order (ascending cost). 
     * Ties are broken arbitrarily.
	 * @return DP_Solution The next best DP solution or null if there are no other solutions.
	 */
    public abstract DP_Solution get_next();

    /** 
     * @return String The name of this class (used for help messages)
     */
    public static String getName()
    {
        String className = Thread.currentThread().getStackTrace()[2].getClassName(); 
        return className;
    }
}