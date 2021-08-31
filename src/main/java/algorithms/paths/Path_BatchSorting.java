package algorithms.paths;

import algorithms.Configuration;
import entities.paths.DP_Problem_Instance;
import entities.paths.Path_Query_Solution;

/** 
 * This algorithm produces all the solutions to a DP problem specified 
 * as a {@link entities.paths.DP_Problem_Instance} object and then sorts them
 * so that it can return them in ranked order.
 * The class has been customized for join problems only since it produces
 * {@link entities.paths.Path_Query_Solution} as results.
 * It is implemented as an iterator that is first initialized and then the method {@link #get_next} 
 * returns the next best solution.
 * The batch-computation of all the solutions is done via the methods of {@link algorithms.paths.Path_Batch}
 * that are inherited.
 * <br><br>
 * IMPORTANT: Before using this class, the nodes and the edges of the DP state-space graph
 * must have already been initialized either by {@link entities.paths.DP_Problem_Instance#bottom_up}
 * or some other method.
 * @author Nikolaos Tziavelis
*/
public class Path_BatchSorting extends Path_Batch
{
	public Path_BatchSorting(DP_Problem_Instance inst, Configuration conf)
    {
        super(inst, conf);
        // The superclass Path_Batch stores all solutions in all_solutions list but does not sort them
        sort_results();
    }

	public Path_BatchSorting(Path_Batch unsorted)
    {
        super(unsorted);
        // The superclass Path_Batch stores all solutions in all_solutions list but does not sort them
        sort_results();
    }

    /** 
     * Computes the next DP solution of {@link #instance} in ranked order. 
     * Ties are broken arbitrarily.
     * @return Path_Query_Solution The next best DP solution or null if there are no other solutions.
     */
    @Override
    public Path_Query_Solution get_next()
    {
        this.current_index++;
        if (current_index == this.all_solutions.size()) return null;
        return all_solutions.get(current_index);
    }
}