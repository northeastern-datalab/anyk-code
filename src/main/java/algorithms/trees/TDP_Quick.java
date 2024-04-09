package algorithms.trees;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ThreadLocalRandom;

import algorithms.Configuration;
import entities.trees.TDP_Decision;
import entities.trees.TDP_DecisionSet;
import entities.trees.TDP_Problem_Instance;
import util.Common;

/** 
 * Quick is a variant of Anyk-Part that implements {@link #get_successors} 
 * by incrementally sorting the decision set with Incremental QuickSort (IQS).
 * Each call returns the single true successor like 
 * {@link algorithms.paths.DP_Eager} and {@link algorithms.paths.DP_Lazy}.
 * IQS is proposed in Paredes and Navarro 2006
 * <a href="https://doi/10.1137/1.9781611972863.16">https://doi/10.1137/1.9781611972863.16</a>.
 * It works by tweaking the usual quicksort method so that it returns the sorted result incrementally
 * with a very small overhead.
 * @author Nikolaos Tziavelis
*/
public class TDP_Quick extends TDP_Part
{
	public TDP_Quick(TDP_Problem_Instance inst, Configuration conf)
    {
    	super(inst, conf);
    }
    
    public void initialize_partial_order(TDP_DecisionSet decisions)
    {
        // Initialize the successor lists
        for (TDP_Decision dec : decisions.list_of_decisions)
            dec.successors = new ArrayList<TDP_Decision>(1);

        // Initialize the data structures needed for IQS
        decisions.pivot_stack = new Stack<Integer>();
        decisions.pivot_stack.push(decisions.list_of_decisions.size());

        // Call get_next once to remove the best element
        decisions.next_idx = 0;
        get_next_iqs(decisions);

        decisions.partial_order_computed = true;
    }

    public List<TDP_Decision> get_successors(TDP_Decision dec)
    {
        TDP_DecisionSet decision_set = dec.belongs_to();
        if (!decision_set.partial_order_computed)
            initialize_partial_order(decision_set);
        // Whenever we want to find the successor, first check if we have already computed it
        if (!dec.successors.isEmpty()) return dec.successors;

        if (decision_set.next_idx == decision_set.list_of_decisions.size()) return dec.successors;

        TDP_Decision res = get_next_iqs(decision_set);
        dec.successors.add(res);
        return dec.successors;
    }

    /** 
     * Implementation of the core method of IQS for incremental sorting.
    */
    private TDP_Decision get_next_iqs(TDP_DecisionSet decision_set)
    {
        while (decision_set.next_idx != decision_set.pivot_stack.peek())
        {
            // Pick a random pivot 
            // in-between the current index and the previous pivot position (stored in the stack)
            // Note that the 2nd argument is not inclusive (so we do +1)
            int pivot_idx = ThreadLocalRandom.current().nextInt(decision_set.next_idx, decision_set.pivot_stack.peek());
            // Partition according to the pivot, bringing it to the correct position
            int new_pivot_idx = Common.partition(decision_set.list_of_decisions, pivot_idx, decision_set.next_idx, decision_set.pivot_stack.peek() - 1);
            decision_set.pivot_stack.push(new_pivot_idx);
        }

        // When we break from the loop, the element at next_idx has been used as a pivot
        // That means it has been placed at the correct (sorted) position
        decision_set.pivot_stack.pop();
        TDP_Decision res = decision_set.list_of_decisions.get(decision_set.next_idx);
        // Increase the index for the next call
        decision_set.next_idx++;

        return res;
    }
}