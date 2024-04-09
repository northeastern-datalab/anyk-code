package entities.trees;

import java.util.ArrayList;
import java.util.List;

import entities.Tuple;

/** 
 * A T-DP solution for problems that are equi-join acyclic (tree) queries.
 * The solution thus consists of database tuples.
 * Implements the abstract methods of {@link entities.trees.TDP_Solution}.
 * Used by {@link algorithms.trees.Tree_Batch}.
 * @author Nikolaos Tziavelis
*/
public class Tree_Query_Solution extends TDP_Solution implements Comparable<Tree_Query_Solution>
{
    public List<Tuple> tuple_list;

    public Tree_Query_Solution(List<TDP_State_Node> node_list)
    {
        Tuple t;
        this.cost = 0.0;
        // Shallow copy the list of tuples
        this.tuple_list = new ArrayList<Tuple>();
        for (TDP_State_Node node : node_list)
        {
            t = (Tuple) node.state_info;
            this.tuple_list.add(t);
            this.cost += t.cost;
        }
    }

    public double get_final_cost()
    {
        return this.cost;
    }

    public String solutionToString()
    {
        return tuple_list.toString();
    }

    public List<Tuple> solutionToTuples_strict_order()
    {
        return tuple_list;
    }

    public List<Tuple> solutionToTuples()
    {
        return tuple_list;
    }
    
    /** 
     * @param other
     * @return int
     */
    @Override
    public int compareTo(Tree_Query_Solution other)
    {
        // compareTo should return < 0 if this is supposed to be less than other, 
        // > 0 if this is supposed to be greater than other  
        // and 0 if they are supposed to be equal
    	if (this.cost < other.cost) return -1;
    	else if (this.cost > other.cost) return 1;
    	else return 0;
    }
}
