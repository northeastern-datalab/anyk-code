package entities.trees;

import java.util.List;

import entities.Tuple;

/** 
 * A solution to a T-DP problem is a tree from the source node to terminal nodes (one for each leaf stage) 
 * in the state-space graph.
 * Its cost is the aggregation of the costs of the edges-decisions it consists of.
 * This is an abstract class and is implemented differently according to the any-k algorithms,
 * e.g. it can be represented as a prefix or a subtree.
 * @author Nikolaos Tziavelis
*/
public abstract class TDP_Solution// implements Bulk_Comparable
{
    /** 
     * The associated cost of the solution.
    */
	protected double cost;

    public TDP_Solution()
    {
    }

    /** 
     * @return String A string representation of the states that this solution contains.
     */
    public abstract String solutionToString(); 

    /** 
     * @return List<Tuple> The list of tuples that the solution represents. 
     * The tuples in the list are always in the correct order that the query was given 
     * (from the source to the leaves).
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

    /** 
     * @return double The finalcost of this solution. 
     * If it is a partial *prefix* solution, then it is the cost that it can have when optimally expanded.
     */
    public abstract double get_final_cost();

    /** 
     * @return String
     */
    @Override
    public String toString()
    {
        return this.solutionToString();
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
}
