package algorithms.trees;

import java.util.Collections;

import algorithms.Configuration;
import entities.trees.Star_Equijoin_Query;
import entities.trees.TDP_Problem_Instance;
import entities.trees.TDP_Solution;
import entities.trees.TDP_Star_Equijoin_Instance;

/** 
 * This algorithm produces all the solutions to a T-DP problem specified 
 * as a {@link entities.trees.TDP_Problem_Instance} object and then sorts them
 * so that it can return them in ranked order.
 * The class has been customized for join problems only since it produces
 * {@link entities.trees.Tree_Query_Solution} as results.
 * It is implemented as an iterator that is first initialized and then the method {@link #get_next} 
 * returns the next best solution.
 * The batch-computation of all the solutions is done via the methods of {@link algorithms.trees.Tree_Batch}
 * that are inherited.
 * <br><br>
 * IMPORTANT: Before using this class, the nodes and the edges of the DP state-space graph
 * must have already been initialized either by {@link entities.paths.DP_Problem_Instance#bottom_up}
 * or some other method.
 * @author Nikolaos Tziavelis
*/
public class Tree_BatchSorting extends Tree_Batch
{
	public Tree_BatchSorting(TDP_Problem_Instance inst, Configuration conf)
    {
        super(inst, conf);
        // The superclass Tree_Batch stores all solutions in all_solutions list but does not sort them
        Collections.sort(all_solutions);
    }

    public static void main(String args[]) 
    {
        // Run the example
        Star_Equijoin_Query example_query = new Star_Equijoin_Query();
        TDP_Star_Equijoin_Instance instance = new TDP_Star_Equijoin_Instance(example_query);
        instance.bottom_up();
        TDP_Anyk_Iterator iter = new Tree_BatchSorting(instance, null);
        
        System.out.println("All solutions:");
        TDP_Solution sol;
        while ((sol = iter.get_next()) != null)
            System.out.println(sol.solutionToString() + "  Cost = " + sol.get_cost());
    }

}