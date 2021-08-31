package algorithms;

/** 
 * This class contains parameters that configure the execution of any-k algorithms.
 * Currently, only supported for DP (paths).
 * @author Nikolaos Tziavelis
*/
public class Configuration 
{
	/** 
	 * Used to specify different implementations of priority queue.
     * By default (null), a binary heap is used.
	*/	
    public String heap_type = null;
	/** 
	 * Used to specify whether the any-k algorithms initialize their data structures lazily
     * (i.e., the first time they are accessed).
     * By default, it is set to true.
	*/	
    public boolean initialization_laziness = true;
    

    public Configuration(){}

    public void set_heap_type(String ht)
    {
        this.heap_type = ht;
    }

    public void set_initialization_laziness(boolean il)
    {
        this.initialization_laziness = il;
    }
}
