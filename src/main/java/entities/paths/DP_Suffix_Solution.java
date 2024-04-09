package entities.paths;

import java.util.ArrayList;
import java.util.List;

import entities.Tuple;

/** 
 * A suffix representation of DP solutions that allows for sharing common suffixes. 
 * Each suffix solution consists of one decision and a pointer to a suffix solution of shorter length.
 * @see algorithms.paths.DP_Recursive
 * @author Nikolaos Tziavelis
*/
public class DP_Suffix_Solution extends DP_Solution implements Comparable<DP_Suffix_Solution>
{
    /** 
     * The first decision contained in the solution.
    */
	public DP_Decision first_decision;
    /** 
     * A shorter solution that consists of the decisions after {@link #first_decision}.
    */
    public DP_Suffix_Solution shorter_suffix;

    // DEPRECATED: we don't really use the length anywhere
    // The length of the suffix solution is equal to the number of decisions it represents
    // A full solution will have length stages_no
    // public int length;

    /** 
     * A next pointer points to the next best suffix that has the same origin (used by {@link algorithms.paths.DP_Recursive}).
    */
    public DP_Suffix_Solution next;

    /** 
     * Constructor for initializing a suffix solution of length 1.
     * @param dec The only decision that this solution will contain.
    */
    public DP_Suffix_Solution(DP_Decision dec)
    {
    	this.first_decision = dec;
    	this.shorter_suffix = null;
    	// this.length = 1;
        this.cost = dec.cost;
        this.next = null;
    }

    /** 
     * Instantiates a suffix solution of length greater than 1 
     * from another suffix solution concatenated with one more decision.
    */
    public DP_Suffix_Solution(DP_Suffix_Solution suffix, DP_Decision new_decision)
    {
    	this.shorter_suffix = suffix;
    	this.first_decision = new_decision;
        // this.length = suffix.length + 1;
        this.cost = suffix.cost + new_decision.cost;
        this.next = null;
    }

    /** 
     * @return DP_Suffix_Solution The suffix shortened by one decision.
     */
    public DP_Suffix_Solution get_shorter()
    {
    	return this.shorter_suffix;
    }
    
    /** 
     * @return DP_Decision The first decision of this suffix solution.
     */
    public DP_Decision get_first_decision()
    {
    	return this.first_decision;
    }

    public double get_final_cost()
    {
        return this.cost;
    }

    public double get_length()
    {
        int res = 0;
        DP_Suffix_Solution curr_suffix = this;
        while (curr_suffix != null)
        {
            res += 1;
            curr_suffix = curr_suffix.get_shorter();
        }
        return res;
    }
    
    /** 
     * @return String A string representation of the solution 
     */
    public String solutionToString()
    {
        // Follow pointers to shorter suffixes
        // and accumulate the decisions that this suffix solution represents
        String s;
    	StringBuilder builder = new StringBuilder();
        DP_Suffix_Solution current = this;
        // Traverse the pointers to shorter suffixes and append the encountered tuples to the end of the string
    	while (current != null)
    	{
    		s = current.first_decision.target.toString();
    		builder.append(s);
            current = current.shorter_suffix;
    	}
    	return builder.toString();
    }

    /** 
     * @return List<Tuple> The list of tuples that the solution represents.
     */
    // Returns the list of tuples that the solution represents by 
    public List<Tuple> solutionToTuples()
    {
        // Follow pointers to shorter suffixes
        // and accumulate the decisions that this suffix solution represents
        List<Tuple> res = new ArrayList<Tuple>();
        Tuple tuple;
        DP_Suffix_Solution current = this;  
        while (current != null)
        {
            if (current.first_decision.target.state_info instanceof Tuple)
            {
                tuple = (Tuple) current.first_decision.target.state_info;
                res.add(tuple);
            }
            current = current.shorter_suffix;
        }
        return res;
    }
    
    public List<Tuple> solutionToTuples_strict_order()
    {
        // Suffix solutions encounter the tuples in the correct order as they are traversed anyway
        return this.solutionToTuples();
    }
    
    /** 
     * @param other
     * @return int
     */
    @Override
    public int compareTo(DP_Suffix_Solution other)
    {
        // compareTo should return < 0 if this is supposed to be less than other, 
        // > 0 if this is supposed to be greater than other  
        // and 0 if they are supposed to be equal
    	if (this.cost < other.cost) return -1;
    	else if (this.cost > other.cost) return 1;
        else
        {
            // !!! We want consistent tie-breaking that agrees with the bottom_up phase
            // and the best_decisions that have been computed
            if (this.first_decision.hashCode() < other.first_decision.hashCode()) return -1;
            else return 1;
        }
    }

    @Override
    public boolean equals(Object obj) 
    {
        if (!(obj instanceof DP_Solution))
            return false;
        DP_Solution other = (DP_Solution) obj;
        if ((obj instanceof DP_Suffix_Solution)) 
            return this.solutionToTuples().equals(other.solutionToTuples());
        else
            return this.solutionToTuples_strict_order().equals(other.solutionToTuples_strict_order());
    }
}
