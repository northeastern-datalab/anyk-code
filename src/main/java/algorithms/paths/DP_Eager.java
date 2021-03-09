package algorithms.paths;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import entities.paths.DP_Decision;
import entities.paths.DP_DecisionSet;
import entities.paths.DP_Problem_Instance;

/** 
 * Eager is a variant of Anyk-Part that implements {@link #get_successors} 
 * by sorting the decision set eagerly 
 * (upon the first visit with {@link #initialize_partial_order}).
 * After sorting is complete, each decision points to its exact successor.
 * @author Nikolaos Tziavelis
*/
public class DP_Eager extends DP_Part
{
	public DP_Eager(DP_Problem_Instance inst, String heap_type)
    {
    	super(inst, heap_type);

        // Deprecated - initialization happens on the fly
        // Sort all the decision sets
        // To find them, traverse the DP graph in a DFS manner
        // initialize_partial_order_DFS(instance.starting_node);
    }
    
    public void initialize_partial_order(DP_DecisionSet decisions)
    {
        // Initialize the successor lists
        for (DP_Decision dec : decisions.list_of_decisions)
            dec.successors = new ArrayList<DP_Decision>();

        // For each node, we sort all decisions and then store a pointer for each decision according to the sorted order
        ArrayList<DP_Decision> list_of_decisions = decisions.list_of_decisions;
        Collections.sort(list_of_decisions);
        // Store a pointer to the next decision in the sorted list
        // The list of successors will contain only one element
        for (int i = 0; i < (list_of_decisions.size() - 1); i++)
            list_of_decisions.get(i).successors.add(list_of_decisions.get(i + 1));            
        // The successor list of the last decision in the sorted order will be empty
    }

    public List<DP_Decision> get_successors(DP_Decision dec)
    {
        DP_DecisionSet decision_set = dec.belongs_to();
        if (!decision_set.partial_order_computed)
        {
            initialize_partial_order(decision_set);
            decision_set.partial_order_computed = true;
        }
        return dec.successors;
    }
}