package entities;

public abstract class State_Node 
{
    /** 
     * A flag that indicates whether this node is part of the last stage of DP.
    */
    public boolean terminal; 
    /** 
     * The minimum achievable cost starting from this node.
    */    
    private double opt_cost;
    /** 
     * Holds local information about the state, depends on the T-DP problem.
    */
    public Object state_info;

    public State_Node(Object info)
    {
        this.opt_cost = Double.POSITIVE_INFINITY;
        this.state_info = info;
        this.terminal = false;
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
     * Marks this node as terminal.
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
     * Returns the tuple that is contained inside the node.
     * Only works for state-nodes that correspond to database tuples, will fail otherwise.
     * @return Tuple The corresponding database tuple.
     */
    public Tuple toTuple()
    {
        return (Tuple) this.state_info;
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
