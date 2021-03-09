package entities.trees;

import java.util.ArrayList;
import java.util.List;

/** 
 * A state-node in the state-space graph of T-DP constructed by {@link entities.paths.DP_Problem_Instance#bottom_up}.
 * Each state contains local information specific to the problem.
 * For example, in join problems states represent database tuples.  
 * In T-DP, the stage of a node could have multiple children stages (i.e., branches).
 * For each branch, we get a separate decision set and a separate optimal achievable cost.
 * The overall minimum achievable cost is the sum of them.
 * Important: the branches are indexed sequentially from 0 - those indexes are different than stage indexes.
 * @author Nikolaos Tziavelis
*/
public class TDP_State_Node
{
    /** 
     * The stage of T-DP the node belongs to.
    */
    public int stage;
    /** 
     * The number of stages below this node.
    */
    public int num_branches;
    /** 
     * A list of minimum achievable costs, one for each branch.
    */
    public List<Double> branches_opt_cost;
    /** 
     * The optimal achievable cost starting from this node and traversing its subtree.
    */
    private Double subtree_opt_cost;    
    /** 
     * All the decisions (edges) that can be made from this state, one set for each branch.
    */
    public List<TDP_DecisionSet> decisions;  
    /** 
     * Holds local information about the state, depends on the T-DP problem.
    */
    public Object state_info;
    /** 
     * For Lawler in the space of subtree solutions (used by TDP_Recursive).
    */
    public List<Double> lawler_future_costs;

    /** 
     * Creates a new T-DP state.
     * Initially, we don't know the possible decisions and there is no way to reach the final state(s).
     * Thus, the cost of all branches is infinite.
     * @param stage The T-DP stage that the state belongs to.
     * @param num_branches The number of stages below this state.
     * @param info Local state information.
    */
    public TDP_State_Node(int stage, int num_branches, Object info)
    {
        this.stage = stage;
        this.num_branches = num_branches;
        this.branches_opt_cost = new ArrayList<Double>();
        for (int i = 0; i < num_branches; i++) branches_opt_cost.add(Double.POSITIVE_INFINITY);
        this.subtree_opt_cost = null;   // Stays null until requested with get_opt_cost()
        this.decisions = new ArrayList<TDP_DecisionSet>();
        for (int i = 0; i < num_branches; i++) this.decisions.add(new TDP_DecisionSet());
        this.state_info = info;
        this.lawler_future_costs = null;
    }

    
    /** 
     * Adds a decision (edge) to the pool of possible ones in the specified branch.
     * This decision incurs some immediate cost given as parameter.
     * If we follow the new decision from this state, we end up in resulting_state.
     * If the minimum achievable cost with this decision is better than the current one, 
     * updates the best decision and the optimal cost in this branch.
     * @param branch The branch that the decision belongs to.
     * @param resulting_state The state we end up if we follow the new edge-decision.
     * @param immediate_cost The cost we have to pay to follow the new edge-decision.
     */
    public void add_decision(int branch, TDP_State_Node resulting_state, double immediate_cost)
    {
        TDP_DecisionSet decision_set = this.decisions.get(branch);
        TDP_Decision new_decision = new TDP_Decision(resulting_state, immediate_cost, decision_set);
        int is_now_best = decision_set.add(new_decision);

        if (is_now_best == 1) this.branches_opt_cost.set(branch, immediate_cost + new_decision.target.get_subtree_opt_cost());
    }

    /** 
     * Makes this state share the same decisions with another state in the specified branch.
     * Important: the decisions of each state-node can be different in each branch
     * (e.g. for join queries, join attribute values are different across branches).
     * Hence, we only share a specific branch.
     * Use only after the other state has finished adding decisions.
     * @param other_state The state whose decisions will be copied to this one.
     * @param branch The branch to be shared.
     */
    public void share_decisions(TDP_State_Node other_state, int branch)
    {
        this.decisions.set(branch, other_state.decisions.get(branch));
        this.branches_opt_cost.set(branch, other_state.branches_opt_cost.get(branch));
    }

    /** 
     * Returns the best child (state-node) in a specific branch we can hop to from the current state-node.
     * @param branch A branch of the current node.
     * @return TDP_State_Node The child that incurs the minimum achievable cost for the specified branch.
     */
    public TDP_State_Node get_best_child(int branch)
    {
        return this.decisions.get(branch).best_decision.target;
    }

    /** 
     * Returns the best children (state-nodes) from all branches we can hop to from the current state-node.
     * @return List<TDP_State_Node> A list of the children that incur the minimum achievable cost 
     * (one for each branch) or an empty list if this node belongs to a leaf stage.
     */
    public List<TDP_State_Node> get_best_children()
    {
        // if (this.num_branches == 0) return null;
        List<TDP_State_Node> res = new ArrayList<TDP_State_Node>();
        for (TDP_DecisionSet decs_one_branch : this.decisions)
            res.add(decs_one_branch.best_decision.target);
        return res;
    }

    /** 
     * Returns the best available decision from this state for a particular branch.
     * @param branch A branch of the current node.
     * @return TDP_Decision The best decision in that branch.
     */
    public TDP_Decision get_best_decision(int branch)
    {
        return this.decisions.get(branch).best_decision;
    }
    
    /** 
     * Returns the decisions that can be taken from this state for a particular branch 
     * (that can lead to leaf stage nodes).
     * @param branch A branch of the current node.
     * @return ArrayList<TDP_Decision> The admissible decisions for the branch.
     */
    public ArrayList<TDP_Decision> get_decisions(int branch)
    {
        return this.decisions.get(branch).list_of_decisions;
    }

    /** 
     * Returns the minimum achievable cost starting from this state.
     * It is computed on-demand and then stored
     * to avoid summing up multiple times during the bottom-up phase.
     * @return double The minimum achievable cost starting from this state
     */
    public double get_subtree_opt_cost()
    {
        if (this.subtree_opt_cost != null) return this.subtree_opt_cost;
        else
        {
            double res = 0.0;
            for (Double branch_cost : branches_opt_cost) res +=  branch_cost;
            this.subtree_opt_cost = res;
            return res;
        }
    }

    /** 
     * Sets the minimum achievable cost from this node to 0.
     * Use to set the cost of leaf nodes.
     */
    public void set_to_terminal()
    {
        this.subtree_opt_cost = 0.0;
    }
    
    /** 
     * CAUTION: expensive because it allocates a new list.
     * @param branch A branch of the current node.
     * @return ArrayList<TDP_State_Node> The children in the specified branch that are capable of reaching the terminal state.
     */
    public ArrayList<TDP_State_Node> get_children_of_one_branch(int branch)
    {
        ArrayList<TDP_State_Node> children = new ArrayList<TDP_State_Node>();
        for (TDP_Decision edge : decisions.get(branch).list_of_decisions) children.add(edge.target);
        return children;
    }

    /** 
     * CAUTION: expensive operation, use only for debugging.
     * @param child One of the children of this node.
     * @param branch The branch of the child.
     * @return double The cost of a decision that leads to the specified child.<br>
     * If there is no such feasible decision, returns infinity.
     */
    public double get_decision_cost(TDP_State_Node child, int branch)
    {
        for (TDP_Decision edge : decisions.get(branch).list_of_decisions) 
            if (edge.target.equals(child)) 
                return edge.cost;
        return Double.POSITIVE_INFINITY;
    }

    /** 
     * @return String
     */
    @Override
    public String toString() 
    {
        if (state_info != null) return state_info.toString();
        else return "start"; 
    } 
}
