package algorithms.trees;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import algorithms.Configuration;
import entities.trees.TDP_Decision;
import entities.trees.TDP_DecisionSet;
import entities.trees.TDP_Problem_Instance;
import entities.trees.TDP_Solution;
import entities.trees.TDP_State_Node;
import entities.trees.TDP_Subtree_Collection;
import entities.trees.TDP_Subtree_Solution;


/** 
 * Implementation of Anyk-Rec for T-DP, a ranked enumeration algorithm that relies 
 * on recursion: a node finds the next best solution by 
 * recursively finding the next best solutions of its children.<br>
 * Brief description of the algorithm: 
 * Each node maintains a priority queue {@link entities.trees.TDP_DecisionSet#pq_rec} 
 * for each branch of the tree
 * with the current candidates for the next best subtree solution.
 * The method {@link #get_next} works by popping from that PQ and replacing that candidate
 * with a subtree solution that goes to the same child 
 * but then takes the next best subtree from the child 
 * (determined recursively with {@link #find_next_recursive}).
 * We also need to combine to appropriately combine and rank the subtree solutions from each branch.
 * To do that, we use a Lawler procedure at each PQ
 * that performs ranked enumeration in the space of subtree solutions of the children.
 * The ranked order of the subtree solutions from a particular node is also stored
 * with {@link entities.trees.TDP_Subtree_Solution#next} pointers 
 * and reused among different {@link #find_next_recursive} calls.
 * This provides a speedup as the computation keeps going.<br>
 * Since multiple nodes could share the same decisions, we maintain the PQs at
 * {@link entities.trees.TDP_DecisionSet} objects instead of {@link entities.trees.TDP_State_Node}.
 * @see <a href="https://doi.org/10.1007/3-540-48318-7_4">Ranked Enumeration Algorithm</a> 
 * @author Nikolaos Tziavelis
*/
public class TDP_Recursive extends TDP_Anyk_Iterator
{
    private TDP_Subtree_Solution latest_solution;

    public TDP_Recursive(TDP_Problem_Instance inst, Configuration conf)
    {
        super(inst, conf);
        this.latest_solution = null;
    }

    /** 
     * Initializes the data strucures that the algorithm needs.
     * We initialize a PQ that has each decision concatenated with the best subtrees from the children.
     * Not needed for the decisions that lead to leaf node - no PQ is needed there.
     * @param curr_decisionSet The current set of decisions to be initialized.
     */
    private void initialize_pq(TDP_DecisionSet curr_decisionSet)
    {   
        TDP_Subtree_Solution cand;
        List<TDP_Decision> edges = curr_decisionSet.list_of_decisions;
        // The PQ has to be initialized with each decision concatenated with
        // the best subtree solution that begins from its target
        List<TDP_Subtree_Solution> candidates = new ArrayList<TDP_Subtree_Solution>(edges.size());
        for (TDP_Decision edge : edges)
        {
            TDP_State_Node child = edge.target;

            if (child.is_terminal()) 
            {
                // After edge, we arrive at a leaf stage
                cand = new TDP_Subtree_Solution(edge);
            }
            else
            {
                // Concatenate the edge with the best subtree solution from the child
                TDP_Subtree_Collection best_subtrees_from_child = get_best_subtree_solutions(child);
                cand = new TDP_Subtree_Solution(edge, best_subtrees_from_child);
            }
            candidates.add(cand);
        }
        //System.out.print("The initial candidates are: ");
        //for (TDP_Subtree_Solution c : candidates) System.out.print(c.solutionToString() + "\t");
        //System.out.println();
        curr_decisionSet.pq_rec = new PriorityQueue<TDP_Subtree_Solution>(candidates);
        return;
    }

    /** 
     * Computes a collection of the best subtree solutions starting from a node,
     * one for each of its branches.
     * Because of the prefix structure of the collections, we initialize them only with
     * the best solution for the first branch (index 0).
     * @param curr_node The starting node.
     * @return TDP_Subtree_Collection The best subtree solutions.
     */
    private TDP_Subtree_Collection get_best_subtree_solutions(TDP_State_Node curr_node)
    {
        // This is the point where we initialize the future cost of each prefix 
        // for the Lawler procedure so that we don't have to do it eagerly at the start of the algorithm
        if (curr_node.lawler_future_costs == null)
        {
            double[] future_costs = new double[curr_node.decisions.size() + 1];
            double c = curr_node.get_opt_cost();
            int idx = 0;
            for (TDP_DecisionSet decisions : curr_node.decisions)
            {
                double branch_opt_cost = decisions.best_decision.opt_achievable_cost();
                // The future cost of the remaining branches in the iteration is the total minimum achievable cost
                // minus the minimum achievable costs of the branches we have already seen
                future_costs[idx] = c;
                c -= branch_opt_cost;
                idx += 1;
            }
            // Add one more zero entry to avoid index out of bounds
            // This means that the future cost of a complete prefix is 0.0
            future_costs[idx] = 0.0;
            curr_node.lawler_future_costs = future_costs;
        }

        TDP_Subtree_Collection res;

        // Initialize the collection only with the Pi_1 from the first branch            
        TDP_Subtree_Solution best_solution_first_branch = get_best_subtree_solution(curr_node, 0);
        res = new TDP_Subtree_Collection(best_solution_first_branch);

        return res;
    }

    /** 
     * Computes the best subtree solution starting from a node for one particular branch.
     * Because many nodes may share the same decisions (and thus subtree solutions),
     * we do that for every {@link entities.trees.TDP_DecisionSet}.
     * We also store the result so that subsequent calls return immediately.
     * @param curr_node The starting node.
     * @param branch One of its branches.
     * @return TDP_Subtree_Collection The best subtree solution in that branch.
     */
    private TDP_Subtree_Solution get_best_subtree_solution(TDP_State_Node curr_node, int branch)
    {
        TDP_Subtree_Solution res;
        TDP_DecisionSet curr_decision_set = curr_node.decisions.get(branch);

        // If we have come at this node before, then the best subtree solution has already been constructed
        // In that case, just return it
        if (curr_decision_set.rec_best_subtree != null) return curr_decision_set.rec_best_subtree;

        // Compute the best subtree solution recursively
        // First we follow the best decision in that branch
        TDP_Decision best_decision = curr_decision_set.best_decision;
        if (best_decision.target.is_terminal()) 
        {
            // After best_decision, we arrive at a leaf stage
            res = new TDP_Subtree_Solution(best_decision);
        }
        else
        {
            // We (recursively) compute the best subtree solutions from there on 
            TDP_Subtree_Collection best_subtrees = get_best_subtree_solutions(best_decision.target);
            // The Pi_1 is the best decision concatenated with those best subtrees
            res = new TDP_Subtree_Solution(best_decision, best_subtrees);
            // Expand the solution so that the first iteration of the algorithm returns a complete solution
            expand(res);
        }

        // Remember this result for fast future lookups
        curr_decision_set.rec_best_subtree = res;

        //System.out.print("The Pi_1 from " + curr_node + " (branch " + branch + " is : ");
        //System.out.println(res.solutionToString());
        return res;
    }

    public TDP_Solution get_next()
    {
    	// If no latest_solution has been stored, then this is the first call to this method
        if (latest_solution == null)
        {
            // Corner case: if no solution exists at all, return null
            if (instance.starting_node.get_best_decision(0) == null) return null;

            // Initialize the Pi_1 in each node
            // Thus, also finds the Pi_1 from the starting node
            // Notice that the starting node always has a single branch, 
            // therefore the overall solution is the first element of the list returned
            latest_solution = get_best_subtree_solution(instance.starting_node, 0);
        }
        else
        {
            // In all other cases, we need to generate new candidates recursively     
            latest_solution = find_next_recursive(latest_solution);
        }
        return latest_solution;
    }

    /** 
     * Determines the next best subtree solution that starts from a node recursively.
     * @param curr The subtree solution whose next will be computed.
     * @return TDP_Subtree_Solution
     */
    private TDP_Subtree_Solution find_next_recursive(TDP_Subtree_Solution curr)
    {
        TDP_Subtree_Solution next_from_child;

        // If we have already computed the next of this solution, just return it
        if (curr.next == null)
        {
            // Split the solution in its parts
            TDP_Decision parent_decision = curr.get_parent_decision();
            TDP_Subtree_Collection curr_subtrees = curr.get_shorter_subtree_collection();

            // Find the PQ that the parent decision belongs to
            TDP_DecisionSet curr_decisionSet = parent_decision.belongs_to();
            // Initialize the pq if we haven't done so yet
            if (curr_decisionSet.pq_rec == null) initialize_pq(curr_decisionSet);
            PriorityQueue<TDP_Subtree_Solution> pq = curr_decisionSet.pq_rec;
            // If it is empty then there is no next, return null
            if (pq.isEmpty()) return null;

            // If it is not empty then the top element is the current one, pop it
            // pq.pop();
            pq.poll();

            // Generate new candidates according to the Lawler procedure
            for (int i = parent_decision.target.decisions.size() - 1; i >= curr.last_sidetrack; i--)
            {
                // Take the successor for branch i
                // Recursive call
                next_from_child = find_next_recursive(curr_subtrees.last_solution);
                if (next_from_child != null)
                {
                    // Lawler successor
                    pq.add(new TDP_Subtree_Solution(parent_decision, curr_subtrees.create_successor(next_from_child)));
                }       
                curr_subtrees = curr_subtrees.prefix;
            }

            // The result is now sitting at the top of the PQ
            curr.next = pq.peek();
            // Expand the solution before returning it
            expand(curr.next);
        }
        //if (curr.next != null) System.out.println("The next of " + curr.solutionToString() + " is " + curr.next.solutionToString());
        //else System.out.println("The next of " + curr.solutionToString() + " is null");
        return curr.next;
    }

    /** 
     * Expands optimally a subtree solution from a particular node
     * that contains only some of the branches
     * (by adding to them the best subtrees for the branches that are missing).
     * @param sol The subtree solution to expand.
     */
    private void expand(TDP_Subtree_Solution sol)
    {
        if (sol == null) return;

        TDP_State_Node parent = sol.get_parent_decision().target;
        TDP_Subtree_Collection curr_subtrees = sol.get_shorter_subtree_collection();

        while (curr_subtrees.size < parent.decisions.size())
        {
            // Expand once by appending the Pi_1 from the next child
            curr_subtrees = new TDP_Subtree_Collection(curr_subtrees, get_best_subtree_solution(parent, curr_subtrees.size));       
        }
        // Assign the expanded prefix to the current solution
        sol.set_shorter_subtree_collection(curr_subtrees);
    }
}