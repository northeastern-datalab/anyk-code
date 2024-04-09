package entities.paths;

import java.util.ArrayList;
import java.util.List;

import entities.Tuple;

/** 
 * A DP solution for problems that are equi-join path queries.
 * The solution thus consists of database tuples.
 * Implements the abstract methods of {@link entities.paths.DP_Solution}.
 * Used by {@link algorithms.paths.Path_Batch}.
 * @author Nikolaos Tziavelis
*/
public class Path_Query_Solution extends DP_Solution implements Comparable<Path_Query_Solution>
{
    public List<Tuple> tuple_list;

    public Path_Query_Solution(List<Tuple> tup_list)
    {
        // Copy the list of tuples
        this.tuple_list = new ArrayList<Tuple>();
        this.cost = 0;
        for (Tuple t : tup_list)
        {
            this.cost += t.cost;
            this.tuple_list.add(t);
        }
    }

    public double get_final_cost()
    {
        return this.cost;
    }

    /** 
     * @return String
     */
    public String solutionToString()
    {
        return tuple_list.toString();
    }
    
    /** 
     * @return List<Tuple>
     */
    public List<Tuple> solutionToTuples_strict_order()
    {
        return tuple_list;
    }
    
    /** 
     * @return List<Tuple>
     */
    public List<Tuple> solutionToTuples()
    {
        return tuple_list;
    }

    /** 
     * @param other
     * @return int
     */
    @Override
    public int compareTo(Path_Query_Solution other)
    {
        // Two solutions are compared according to their cost
        // compareTo should return < 0 if this is supposed to be less than other, 
        // > 0 if this is supposed to be greater than other  
        // and 0 if they are supposed to be equal
    	if (this.cost < other.cost) return -1;
    	else if (this.cost > other.cost) return 1;
    	else return 0;
    }
}
