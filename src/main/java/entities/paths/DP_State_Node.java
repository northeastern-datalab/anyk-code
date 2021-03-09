package entities.paths;

import java.util.ArrayList;

import entities.Tuple;

/** 
 * A state-node in the multi-stage DP graph constructed by {@link entities.paths.DP_Problem_Instance#bottom_up}.
 * Each state contains local information specific to the problem.
 * For example, in join problems states represent database tuples.  
 * @author Nikolaos Tziavelis
*/
public class DP_State_Node
{
    /** 
     * A flag that indicates whether this node is part of the last stage of DP.
    */
    public boolean terminal; 
    /** 
     * All the decisions that can be made at this state.
    */
    public DP_DecisionSet decisions;
    /** 
     * The minimum achievable cost starting from this node.
    */    
    private double opt_cost;
    /** 
     * Holds local information about the state, depends on the DP problem.
    */
    public Object state_info;

    /** 
     * Creates a new DP state.
     * Initially, we don't know the possible decisions and there is no way to reach the final state.
     * Thus, the optimal cost is infinite.
     * @param info Local state information.
    */
    public DP_State_Node(Object info)
    {
        // Initially, we don't know the possible decisions and there is no way to reach the final state
        this.decisions = new DP_DecisionSet();
        this.opt_cost = Double.POSITIVE_INFINITY;
        this.state_info = info;
        this.terminal = false;
    }

    // Use only for the terminal node
    // The terminal node may have some cost associated with it
    // REMOVED for simplicity
    /*
    public DP_State_Node(int stage, double cost)
    {
        this.stage = stage;
        this.decisions = new DP_DecisionSet();
        this.opt_cost = cost;
    }
    */

    /** 
     * Adds a decision to the pool of possible ones from this node.
     * This decision incurs some immediate cost given as parameter.
     * If we follow the new decision from this state, we end up in resulting_state.
     * @param resulting_state The state we end up if we follow the new edge-decision.
     * @param immediate_cost The cost we have to pay to follow the new edge-decision.
    */
    public void add_decision(DP_State_Node resulting_state, double immediate_cost)
    {
        DP_Decision new_decision = new DP_Decision(resulting_state, immediate_cost, this.decisions);
        this.decisions.add(new_decision);
    }
    
    /** 
     * Makes this state share the same decisions with another state.
     * Useful e.g. for the equi-join when tuples share the same join values.
     * Use only after the other state has finished adding decisions.
     * @param other_state The state whose decisions will be copied to this one.
     */
    public void share_decisions(DP_State_Node other_state)
    {
        this.decisions = other_state.decisions;
    }

    /** 
     * Returns the best child (state-node) we can hop to from the current state-node.
     * @return DP_State_Node The child that incurs the minimum achievable cost.
     */
    public DP_State_Node get_best_child()
    {
        return this.decisions.best_decision.target;
    }

    /** 
     * @return DP_Decision The best available decision from this state.
     */
    public DP_Decision get_best_decision()
    {
        return this.decisions.best_decision;
    }

    /** 
     * @return ArrayList<DP_Decision> The decisions that can be made from this state and can lead to the terminal state.
     */
    public ArrayList<DP_Decision> get_decisions()
    {
        return this.decisions.list_of_decisions;
    }

    /** 
     * @return double The minimum achievable cost starting from this state.
     */
    public double get_opt_cost()
    {
        return this.opt_cost;
    }

    /** 
     * @param opt_cost The minimum achievable cost starting from this state.
     */
    public void set_opt_cost(double opt_cost)
    {
        this.opt_cost = opt_cost;
    }

    /** 
     * Sets the minimum achievable cost from this node to 0.
     */
    public void set_to_terminal()
    {
        this.terminal = true;
    }

    /** 
     * @return boolean True if this node is part of the last stage of DP, False otherwise.
     */
    public boolean is_terminal()
    {
        return this.terminal;
    }
    
    /** 
     * @return int The number of nodes that this node reaches in 1-hop.
     */
    public int get_number_of_children()
    {
        return decisions.list_of_decisions.size();
    }

    /** 
     * CAUTION: expensive because it allocates a new list.
     * @return ArrayList<DP_State_Node> The children that are capable of reaching the terminal state.
     */
    public ArrayList<DP_State_Node> get_children()
    {
        ArrayList<DP_State_Node> children = new ArrayList<DP_State_Node>();
        for (DP_Decision edge : decisions.list_of_decisions) children.add(edge.target);
        return children;
    }

    /** 
     * Returns the tuple that is contained inside the node.
     * Only works for state-nodes that correspond to database tuples, will fail otherwise.
     * @return Tuple The corresponding database tuple.
     */
    public Tuple toTuple()
    {
        return (Tuple) this.state_info;
    }
    
    /** 
     * CAUTION: expensive operation, use only for debugging.
     * @param child One of the children of this node.
     * @return double The cost of a decision that leads to the specified child.<br>
     * If there is no such feasible decision, returns infinity.
     */
    public double get_decision_cost(DP_State_Node child)
    {
        for (DP_Decision edge : decisions.list_of_decisions) 
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
        else return ""; 
    } 
}
