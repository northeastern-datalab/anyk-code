package algorithms.trees;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import algorithms.Configuration;
import entities.trees.TDP_Decision;
import entities.trees.TDP_DecisionSet;
import entities.trees.TDP_Problem_Instance;

/** 
 * Lazy is a variant of Anyk-Part that implements {@link #get_successors} 
 * by popping them from a priority queue.
 * Each time a decision is popped, we store it as the (single) successor of the previous one
 * so that future calls return in constant time.
 * The idea behind this approach is also described in Chang et al. VLDB'15 
 * <a href="https://doi.org/10.14778/2735479.2735486">https://doi.org/10.14778/2735479.2735486</a>.
 * @author Nikolaos Tziavelis
*/
public class TDP_Lazy extends TDP_Part
{
	public TDP_Lazy(TDP_Problem_Instance inst, Configuration conf)
    {
    	super(inst, conf);
    }
    
    public void initialize_partial_order(TDP_DecisionSet decisions)
    {
        // Initialize the successor lists
        for (TDP_Decision dec : decisions.list_of_decisions)
            dec.successors = new ArrayList<TDP_Decision>();

        // Initialize a PQ for this decision set and insert all the decisions except the best one
        // To do this efficiently, build the entire heap and pop once
        decisions.pq_lazysort = new PriorityQueue<TDP_Decision>(decisions.list_of_decisions);
        
        // Remove the best element from the heap
        decisions.pq_lazysort.poll();
    }

    public List<TDP_Decision> get_successors(TDP_Decision dec)
    {
        TDP_DecisionSet decision_set = dec.belongs_to();
        if (!decision_set.partial_order_computed)
        {
            initialize_partial_order(decision_set);
            decision_set.partial_order_computed = true;
        }
        // assert dec.belongs_to().partial_order_computed;
        // Whenever we want to find the successor, first check if we have already computed it
        if (!dec.successors.isEmpty()) return dec.successors;
        // Otherwise compute it by popping from the PQ
        // Corner case: if it is the last decision in the sorted order, no successor exists and the pq is empty
        PriorityQueue<TDP_Decision> pq = dec.belongs_to().pq_lazysort;
        // assert pq != null;
        if (!pq.isEmpty())
        {
            TDP_Decision succ = pq.poll();
            dec.successors.add(succ);
        }
        // assert dec.successors.size() == 1 || dec.successors.size() == 0;
        return dec.successors;
    }
}