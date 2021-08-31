package algorithms.paths;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import algorithms.Configuration;
import entities.paths.DP_Decision;
import entities.paths.DP_DecisionSet;
import entities.paths.DP_Problem_Instance;

/** 
 * Lazy is a variant of Anyk-Part that implements {@link #get_successors} 
 * by popping them from a priority queue.
 * Each time a decision is popped, we store it as the (single) successor of the previous one
 * so that future calls return in constant time.
 * The idea behind this approach is also described in Chang et al. VLDB'15 
 * <a href="https://doi.org/10.14778/2735479.2735486">https://doi.org/10.14778/2735479.2735486</a>.
 * @author Nikolaos Tziavelis
*/
public class DP_Lazy extends DP_Part
{
	public DP_Lazy(DP_Problem_Instance inst, Configuration conf)
    {
    	super(inst, conf);

        // Deprecated - initialization happens on the fly
        // initialize_partial_order_DFS(instance.starting_node);
    }
    
    public void initialize_partial_order(DP_DecisionSet decisions)
    {
        // Initialize the successor lists
        for (DP_Decision dec : decisions.list_of_decisions)
            dec.successors = new ArrayList<DP_Decision>();

        // Initialize a PQ for this decision set and insert all the decisions except the best one
        // To do this efficiently, build the entire heap and pop once
        decisions.pq_lazysort = new PriorityQueue<DP_Decision>(decisions.list_of_decisions);
        
        //List<DP_Decision> decision_list = decisions.list_of_decisions;
        //DP_Decision[] decision_arr = decision_list.toArray(new DP_Decision[decision_list.size()]);
        //BinaryArrayHeap<DP_Decision> decision_heap = BinaryArrayHeap.heapify(decision_arr);
        //decisions.pq_lazysort = decision_heap;
        
        //System.out.println("The Lazy PQ is ");
        //System.out.println(decisions.pq_lazysort);

        // Remove the best element from the heap
        //DP_Decision best = decisions.pq_lazysort.poll();
        decisions.pq_lazysort.poll();

        //System.out.println("----- The best element of the Lazy PQ was " + best);
        //System.out.println("----- Its cost is " + best.cost + " and from its target we can do " + best.target.get_opt_cost());

        //System.out.println("The best decision from here is " + decisions.best_decision);
        //System.out.println("----- Its cost is " + decisions.best_decision.cost + " and from its target we can do " + decisions.best_decision.target.get_opt_cost());

        // assert best == decisions.best_decision;
        // assert decisions.pq_lazysort != null;
        decisions.partial_order_computed = true;
    }

    public List<DP_Decision> get_successors(DP_Decision dec)
    {
        DP_DecisionSet decision_set = dec.belongs_to();
        if (!decision_set.partial_order_computed)
            initialize_partial_order(decision_set);
        // assert dec.belongs_to().partial_order_computed;
        // Whenever we want to find the successor, first check if we have already computed it
        if (!dec.successors.isEmpty()) return dec.successors;
        // Otherwise compute it by popping from the PQ
        // Corner case: if it is the last decision in the sorted order, no successor exists and the pq is empty
        PriorityQueue<DP_Decision> pq = decision_set.pq_lazysort;
        // assert pq != null;
        if (!pq.isEmpty())
        {
            DP_Decision succ = pq.poll();
            dec.successors.add(succ);
        }
        // assert dec.successors.size() == 1 || dec.successors.size() == 0;
        return dec.successors;
    }
}