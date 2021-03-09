package entities.paths;

import java.util.List;

import entities.Tuple;

/** 
 * A solution to a DP problem is a path from the source node to the terminal node in the state-space graph.
 * Its cost is the aggregation of the costs of the edges-decisions it consists of.
 * This is an abstract class and is implemented differently according to the any-k algorithms,
 * e.g. it can be represented as a prefix or a suffix.
 * @author Nikolaos Tziavelis
*/
public abstract class DP_Solution// implements Bulk_Comparable
{
    /** 
     * The associated cost of the solution.
    */
    protected double cost;

    public DP_Solution()
    {
    }

    /** 
     * @return String A string representation of the states that this solution contains.
     */
    public abstract String solutionToString(); 

    /** 
     * @return List<Tuple> The list of tuples that the solution represents. 
     * The tuples in the list are always in the correct order that the query was given.
     */
    public abstract List<Tuple> solutionToTuples_strict_order();

    /** 
     * @return List<Tuple> The list of tuples that the solution represents. 
     * The order of the tuples can be arbitrary. 
     */
    public abstract List<Tuple> solutionToTuples();

    /** 
     * @return double The cost of this solution ( = aggregate of costs of its decisions)
     */
    public double get_cost()
    {
        return this.cost;
    }

    /* DEPRECATED: Bulk_Comparable interface for batch inserts to a PQ
    public void set_group(Object group)
    {
    	this.group = group;
    }	
    public Object get_group()
    {
    	return group;
    }
    */

    
    /** 
     * Two solutions are compared according to their cost.
     * This method is needed for inserting the solutions into a PQ.
     * @param other
     * @return int
     */
    public int compareAgainst(DP_Solution other)
    {
        // compareTo should return < 0 if this is supposed to be less than other, 
        // > 0 if this is supposed to be greater than other  
        // and 0 if they are supposed to be equal
    	if (this.cost < other.cost) return -1;
    	else if (this.cost > other.cost) return 1;
    	else return 0;
    }

    
    /** 
     * @return String
     */
    @Override
    public String toString()
    {
        return this.solutionToString();
    }
}
