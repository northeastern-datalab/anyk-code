package algorithms.paths;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import algorithms.Configuration;
import entities.paths.DP_Decision;
import entities.paths.DP_DecisionSet;
import entities.paths.DP_Problem_Instance;
import entities.paths.DP_Solution;
import entities.paths.DP_State_Node;
import entities.paths.DP_Suffix_Solution;

/** 
 * Implementation of Anyk-Rec for DP, a ranked enumeration algorithm that relies 
 * on recursion: a node finds the next best solution by 
 * recursively finding the next best solutions of its children.<br>
 * Brief description of the algorithm: 
 * Each node maintains a priority queue {@link entities.paths.DP_DecisionSet#pq_rec} 
 * with the current candidates for the next best suffix solution 
 * (from the current node to the terminal).
 * The method {@link #get_next} works by popping from that PQ and replacing that candidate
 * with a suffix solution that goes to the same child 
 * but then takes the next best path from the child 
 * (determined recursively with {@link #find_next_recursive}).
 * The ranked order of the suffix solutions from a particular node is also stored
 * with {@link entities.paths.DP_Suffix_Solution#next} pointers 
 * and reused among different {@link #find_next_recursive} calls.
 * This provides a speedup as the computation keeps going.<br>
 * Since multiple nodes could share the same decisions, we maintain the PQs at
 * {@link entities.paths.DP_DecisionSet} objects instead of {@link entities.paths.DP_State_Node}.
 * @see <a href="https://doi.org/10.1007/3-540-48318-7_4">Ranked Enumeration Algorithm</a> 
 * @author Nikolaos Tziavelis
*/
public class DP_Recursive extends DP_Anyk_Iterator
{
    private DP_Suffix_Solution latest_solution;

    public DP_Recursive(DP_Problem_Instance inst, Configuration conf)
    {
        super(inst, conf);
        this.latest_solution = null;
    }

    /** 
     * Initializes the data strucures that the algorithm needs.
     * We initialize a PQ that has each decision concatenated with the best path from the child.
     * Not needed for the decisions that lead to the terminal node - no PQ is needed there.
     * @param curr_decisionSet The current set of decisions to be initialized.
     */
    private void initialize_pq(DP_DecisionSet curr_decisionSet)
    {   
        DP_Suffix_Solution solution;

        // The PQ has to be initialized with each decision concatenated with
        // the best suffix solution that begins from its target (determined recursively)
        List<DP_Decision> edges = curr_decisionSet.list_of_decisions;
        ArrayList<DP_Suffix_Solution> candidates = new ArrayList<DP_Suffix_Solution>();
        // An optimization to avoid many reallocations of the underlying array
        candidates.ensureCapacity(edges.size());
        for (DP_Decision edge : edges)
        {
            DP_State_Node child = edge.target;
            if (child.is_terminal())
            {
                // If the decision leads to the last stage, then the corresponding solution consists of only this edge
                solution = new DP_Suffix_Solution(edge);
            }
            else
            {
                // If not, compute the best solution from the child recursively
                DP_Suffix_Solution best_suffix_from_child = get_best_suffix_solution(child);
                solution = new DP_Suffix_Solution(best_suffix_from_child, edge);
            }
            candidates.add(solution);
        }
        // curr_decisionSet.pq_rec = new Priority_Queue<DP_Suffix_Solution>("Lib_BHeap", candidates);
        curr_decisionSet.pq_rec = new PriorityQueue<DP_Suffix_Solution>(candidates);
        return;

    }

    /** 
     * Computes the best suffix solution that starts from a node
     * @param curr_node The starting node.
     * @return DP_Suffix_Solution The best suffix solution.
     */
    private DP_Suffix_Solution get_best_suffix_solution(DP_State_Node curr_node)
    {
        // Base Case: we are at the last stage 
        if (curr_node.is_terminal()) return null;

        DP_DecisionSet curr_decisionSet = curr_node.decisions;
        // If we have come at this node before, the best suffix solution has been constructed
        // In that case, just return it
        if (curr_decisionSet.rec_best_suffix != null) return curr_decisionSet.rec_best_suffix;

        // Else compute it now
        DP_Suffix_Solution best;

        // For all stages other than the last one, the best suffix is computed by concatenating the best decision with
        // the best suffix solution that begins from its target (determined recursively)   
        DP_Decision best_decision = curr_node.get_best_decision();
        DP_State_Node child = best_decision.target;
        if (child.is_terminal())
        {
            // If the decision leads to the last stage, then the best solution consists of only this decision
            best = new DP_Suffix_Solution(best_decision);
        }
        else
        {
            // If not, compute the best solution from the child recursively
            DP_Suffix_Solution best_suffix_from_child = get_best_suffix_solution(child);
            best = new DP_Suffix_Solution(best_suffix_from_child, best_decision);
        }

        curr_decisionSet.rec_best_suffix = best;
        return best;
    }

    public DP_Solution get_next()
    {
    	// If no latest_solution has been stored, then this is the first call to this method
        if (latest_solution == null)
        {
            // Corner case: If no solution exists from the starting node to the terminal node return null
            if (instance.starting_node.get_best_decision() == null) return null;
            // Initializes the Pi_1 in each node
            // Thus, also finds the Pi_1 from the starting node
            latest_solution = get_best_suffix_solution(instance.starting_node);
        }
        else
        {
            // In all other cases, we first need to generate new candidates recursively     
            latest_solution = find_next_recursive(latest_solution);
        }
        return latest_solution;
    }

    /** 
     * Determines the next best suffix solution that starts from a node recursively.
     * @param curr The suffix solution whose next will be computed.
     * @return DP_Suffix_Solution
     */
    private DP_Suffix_Solution find_next_recursive(DP_Suffix_Solution curr)
    {
        if (curr.next == null)
        {
            // Split the solution in its parts
            DP_Decision first_decision = curr.get_first_decision();
            DP_Suffix_Solution remainder = curr.get_shorter();

            // Find the PQ that the first decision belongs to
            DP_DecisionSet curr_decisionSet = first_decision.belongs_to();
            // Initialize the pq if we haven't done so yet
            if (curr_decisionSet.pq_rec == null) initialize_pq(curr_decisionSet);
            PriorityQueue<DP_Suffix_Solution> pq = curr_decisionSet.pq_rec;
            // If it is empty then there is no next, return null
            if (pq.isEmpty()) return null;
            
            // If it is not empty then the top element is the current one, pop it
            // pq.pop();
            pq.poll();

            // The recursion ends just before the last stage
            if (remainder != null)
            {
                // The new candidate we have to insert consists of the same decision concatenated with the next best remainder
                DP_Suffix_Solution next_best_remainder = find_next_recursive(remainder);
                if (next_best_remainder != null)
                    // pq.push(new DP_Suffix_Solution(next_best_remainder, first_decision));
                    pq.add(new DP_Suffix_Solution(next_best_remainder, first_decision));
            }

            // The result is now sitting at the top of the PQ
            curr.next = pq.peek();
        }
        return curr.next;
    }
}