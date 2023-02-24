package algorithms.trees;

import java.util.ArrayList;
import java.util.List;

import algorithms.Configuration;
import entities.trees.Star_Equijoin_Query;
import entities.trees.TDP_Decision;
import entities.trees.TDP_DecisionSet;
import entities.trees.TDP_Problem_Instance;
import entities.trees.TDP_Solution;
import entities.trees.TDP_Star_Equijoin_Instance;

/** 
 * All is a variant of Anyk-Part that implements {@link #get_successors} 
 * by treating all the non-optimal decisions as successors of the best (optimal) decision.
 * This requires virtually no time for initialization (except setting the successor pointers)
 * but returns many successors on a single {@link #get_successors} call which overloads 
 * {@link algorithms.trees.TDP_Part#global_pq}. 
 * The idea behind this approach is also described in Yang et al. WWW'18 
 * <a href="https://doi.org/10.1145/3178876.3186115">https://doi.org/10.1145/3178876.3186115</a>.
 * @author Nikolaos Tziavelis
*/
public class TDP_All extends TDP_Part
{
	public TDP_All(TDP_Problem_Instance inst, Configuration conf)
    {
    	super(inst, conf);
    }

    // Called before the enumeration begins on each node of the DP graph
    public void initialize_partial_order(TDP_DecisionSet decisions)
    {
        // Initialize the successor lists
        for (TDP_Decision dec : decisions.list_of_decisions)
            dec.successors = new ArrayList<TDP_Decision>();

        TDP_Decision best_dec = decisions.best_decision;
        for (TDP_Decision dec : decisions.list_of_decisions)
            if (dec != best_dec)
                best_dec.successors.add(dec);            
    }

    public List<TDP_Decision> get_successors(TDP_Decision dec)
    {
        TDP_DecisionSet decision_set = dec.belongs_to();
        if (!decision_set.partial_order_computed)
        {
            initialize_partial_order(decision_set);
            decision_set.partial_order_computed = true;
        }
        return dec.successors;
    }

    public static void main(String args[]) 
    {
        // Run the example
        Star_Equijoin_Query example_query = new Star_Equijoin_Query();
        TDP_Star_Equijoin_Instance instance = new TDP_Star_Equijoin_Instance(example_query);
        instance.bottom_up();
        TDP_Anyk_Iterator iter = new TDP_All(instance, null);
        
        System.out.println("All solutions:");
        TDP_Solution sol;
        while ((sol = iter.get_next()) != null)
            System.out.println(sol.solutionToString() + "  Cost = " + sol.get_cost());
    }
}