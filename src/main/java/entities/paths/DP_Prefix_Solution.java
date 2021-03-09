package entities.paths;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import entities.Tuple;

/** 
 * A prefix representation of DP solutions that allows for sharing common prefixes. 
 * Each prefix solution consists of one decision and a pointer to a prefix solution of shorter length.
 * @see algorithms.paths.DP_Part
 * @author Nikolaos Tziavelis
*/
public class DP_Prefix_Solution extends DP_Solution implements Comparable<DP_Prefix_Solution>
{
    /** 
     * The last decision contained in the solution.
    */
	public DP_Decision latest_decision;
    /** 
     * A shorter solution that consists of the decisions before {@link #latest_decision}.
    */
    public DP_Prefix_Solution shorter_prefix;
    /** 
     * The length of the prefix solution is equal to the number of decisions it represents.
     * A full solution will have length stages_no (one decision for each stage).
    */
    public int length;
    /** 
     * The cost of the prefix if it is expanded optimally to a full solution.
    */
    private double future_cost;

    /** 
     * Constructor for initializing a prefix solution of length 1.
     * @param first_decision The only decision that this solution contains.
    */
    public DP_Prefix_Solution(DP_Decision first_decision)
    {
    	this.latest_decision = first_decision;
    	this.shorter_prefix = null;
        this.length = 1;
        // Cost is the current cost of the decisions in the prefix
        this.cost = first_decision.cost;
        // Future cost is the cost we will get if we expand the prefix optimally
        this.future_cost = this.cost + this.latest_decision.target.get_opt_cost();
    }
              
    /** 
     * Constructor that instantiates a prefix solution of length greater than 1 
     * from another prefix solution concatenated with one more decision.
    */
    public DP_Prefix_Solution(DP_Prefix_Solution prefix, DP_Decision new_decision)
    {
    	this.shorter_prefix = prefix;
    	this.latest_decision = new_decision;
        this.length = prefix.length + 1;
        this.cost = prefix.cost + new_decision.cost;
        this.future_cost = this.cost + this.latest_decision.target.get_opt_cost();
    }

    /** 
     * @return DP_Prefix_Solution The prefix shortened by one decision.
     */
    public DP_Prefix_Solution get_shorter()
    {
    	return this.shorter_prefix;
    }
    
    /** 
     * @return DP_Decision The last decision of this prefix solution.
     */
    public DP_Decision get_latest_decision()
    {
    	return this.latest_decision;
    }

    /** 
     * A successor solution has the same length but replaces the last decision with an alternative
     * @param alt_decision The alternative decision
     * @return DP_Prefix_Solution A new prefix solution (a new object is created)
     */
    public DP_Prefix_Solution create_successor(DP_Decision alt_decision)
    {
        // We have to check if this prefix has length 1 to avoid null pointers
        if (this.length == 1) return new DP_Prefix_Solution(alt_decision);
        // If the length is > 1, then use the 2nd constructor to replace the last decision
        else return new DP_Prefix_Solution(this.shorter_prefix, alt_decision);
    }
    
    /** 
     * @return String A string representation of the solution 
     */
    public String solutionToString()
    {
        // Follow pointers to shorter prefixes
        // and accumulate the decisions that this prefix solution represents
    	String s;
    	DP_Prefix_Solution current;
    	// Use a stack to gather strings as we go backwards to avoid inserting in the front of the string
    	// When all strings are gathered, start appending to the end of the string
    	Deque<String> stack = new ArrayDeque<String>();
    	current = this;
    	while (current != null)
    	{
    		s = current.latest_decision.target.toString();
    		stack.addFirst(s);	// push string to the front of the stack
    		current = current.shorter_prefix;
    	}
    	StringBuilder builder = new StringBuilder();
    	while (!stack.isEmpty()) 
    	{
			s = stack.removeFirst();	// pop from the front of the stack
			builder.append(s);
    	}
    	return builder.toString();
    }
    
    /** 
     * @return List<Tuple> The list of tuples that the solution represents. 
     * The tuples are returned in reverse order (as they are encountered).
     */
    public List<Tuple> solutionToTuples()
    {
        // Following pointers to shorter prefixes
        // and accumulate the decisions that this prefix solution represents
        List<Tuple> res = new ArrayList<Tuple>();
        Tuple tuple;
        DP_Prefix_Solution current = this;
        while (current != null)
        {
            if (current.latest_decision.target.state_info instanceof Tuple)
            {
                tuple = (Tuple) current.latest_decision.target.state_info;
                res.add(tuple);
            }
            current = current.shorter_prefix;
        }
        return res;
    }
    
    public List<Tuple> solutionToTuples_strict_order()
    {
        // Uses a stack to reverse the order and return them in the correct order
        List<Tuple> res = new ArrayList<Tuple>();
        // Use a stack to gather tuples as we go backwards to avoid inserting in the front of the string
        // When all tuples are gathered, start appending to the end of the list
        Deque<Tuple> stack = new ArrayDeque<Tuple>();
        Tuple tuple;
        DP_Prefix_Solution current = this;
        while (current != null)
        {
            if (current.latest_decision.target.state_info instanceof Tuple)
            {
                tuple = (Tuple) current.latest_decision.target.state_info;
                stack.addFirst(tuple);  // push tuple to the front of the stack
            }
            current = current.shorter_prefix;
        }
        while (!stack.isEmpty()) 
        {
            tuple = stack.removeFirst();    // pop from the front of the stack
            res.add(tuple);
        }
        return res;
    }

    /** 
     * Two prefix solutions are compared according to the cost that they will have when fully expanded.
     * This method is needed for inserting the prefix solutions into a PQ.
     * @param other
     * @return int
     */
    @Override
    public int compareTo(DP_Prefix_Solution other)
    {
        // compareTo should return < 0 if this is supposed to be less than other, 
        // > 0 if this is supposed to be greater than other  
        // and 0 if they are supposed to be equal
    	if (this.future_cost < other.future_cost) return -1;
    	else if (this.future_cost > other.future_cost) return 1;
    	else return 0;
    }

}
