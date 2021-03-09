package algorithms.trees;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import entities.trees.TDP_Decision;
import entities.trees.TDP_DecisionSet;
import entities.trees.TDP_Problem_Instance;

/** 
 * Eager is a variant of Anyk-Part that implements {@link #get_successors} 
 * by sorting the decision set eagerly 
 * (upon the first visit with {@link #initialize_partial_order}).
 * After sorting is complete, each decision points to its exact successor.
 * @author Nikolaos Tziavelis
*/
public class TDP_Eager extends TDP_Part
{
	public TDP_Eager(TDP_Problem_Instance inst, String heap_type)
    {
    	super(inst, heap_type);
    }
    
    // Called before the enumeration begins on each node of the Dp graph
    public void initialize_partial_order(TDP_DecisionSet decisions)
    {
        // Initialize the successor lists
        for (TDP_Decision dec : decisions.list_of_decisions)
            dec.successors = new ArrayList<TDP_Decision>();

        // For each node, we sort all decisions and then store a pointer for each decision according to the sorted order
        ArrayList<TDP_Decision> list_of_decisions = decisions.list_of_decisions;
        Collections.sort(list_of_decisions);
        // Store a pointer to the next decision in the sorted list
        // The list of successors will contain only one element
        for (int i = 0; i < (list_of_decisions.size() - 1); i++)
            list_of_decisions.get(i).successors.add(list_of_decisions.get(i + 1));            
        // The successor list of the last decision in the sorted order will be empty
    }

    // Returns a list of successors according to the partial order
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
}