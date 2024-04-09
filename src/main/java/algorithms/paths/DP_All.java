package algorithms.paths;

import java.util.ArrayList;
import java.util.List;

import algorithms.Configuration;
import entities.paths.DP_Decision;
import entities.paths.DP_DecisionSet;
import entities.paths.DP_Problem_Instance;

/** 
 * All is a variant of Anyk-Part that implements {@link #get_successors} 
 * by treating all the non-optimal decisions as successors of the best (optimal) decision.
 * This requires virtually no time for initialization (except setting the successor pointers)
 * but returns many successors on a single {@link #get_successors} call which overloads 
 * {@link algorithms.paths.DP_Part#global_pq}. 
 * The idea behind this approach is also described in Yang et al. WWW'18 
 * <a href="https://doi.org/10.1145/3178876.3186115">https://doi.org/10.1145/3178876.3186115</a>.
 * @author Nikolaos Tziavelis
*/
public class DP_All extends DP_Part
{
	public DP_All(DP_Problem_Instance inst, Configuration conf)
    {
    	super(inst, conf);
    }

    // Called before the enumeration begins on each node of the DP graph
    public void initialize_partial_order(DP_DecisionSet decisions)
    {
        // Initialize the successor lists
        for (DP_Decision dec : decisions.list_of_decisions)
            dec.successors = new ArrayList<DP_Decision>();

        DP_Decision best_dec = decisions.best_decision;
        for (DP_Decision dec : decisions.list_of_decisions)
            if (dec != best_dec)
                best_dec.successors.add(dec);      
                
        decisions.partial_order_computed = true;
    }

    public List<DP_Decision> get_successors(DP_Decision dec)
    {
        DP_DecisionSet decision_set = dec.belongs_to();
        if (!decision_set.partial_order_computed)
            initialize_partial_order(decision_set);
        return dec.successors;
    }
}