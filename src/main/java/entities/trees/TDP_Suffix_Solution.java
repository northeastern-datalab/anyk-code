package entities.trees;

import java.util.ArrayList;
import java.util.List;

import entities.Tuple;

/** 
 * A suffix representation of TDP solutions that allows for sharing common suffixes. 
 * Each suffix solution consists of one decision and a pointer to a suffix solution of shorter length.
 * The suffix may contain nodes from different subtrees and follows the tree order.
 * @author Nikolaos Tziavelis
*/
public class TDP_Suffix_Solution extends TDP_Solution implements Comparable<TDP_Suffix_Solution>
{
    /** 
     * The first decision contained in the solution.
    */
	public TDP_Decision first_decision;
    /** 
     * A shorter solution that consists of the decisions after {@link #first_decision}.
    */
    public TDP_Suffix_Solution shorter_suffix;

    /** 
     * Constructor for initializing a suffix solution of length 1.
     * @param dec The only decision that this solution will contain.
    */
    public TDP_Suffix_Solution(TDP_Decision dec)
    {
    	this.first_decision = dec;
    	this.shorter_suffix = null;
    	// this.length = 1;
        this.cost = dec.cost;
    }

    /** 
     * Instantiates a suffix solution of length greater than 1 
     * from another suffix solution concatenated with one more decision.
    */
    public TDP_Suffix_Solution(TDP_Suffix_Solution suffix, TDP_Decision new_decision)
    {
    	this.shorter_suffix = suffix;
    	this.first_decision = new_decision;
        // this.length = suffix.length + 1;
        this.cost = suffix.cost + new_decision.cost;
    }

    /** 
     * @return TDP_Suffix_Solution The suffix shortened by one decision.
     */
    public TDP_Suffix_Solution get_shorter()
    {
    	return this.shorter_suffix;
    }
    
    /** 
     * @return TDP_Decision The first decision of this suffix solution.
     */
    public TDP_Decision get_first_decision()
    {
    	return this.first_decision;
    }

    public double get_final_cost()
    {
        return this.cost;
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
        TDP_Suffix_Solution current = this;
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
        TDP_Suffix_Solution current = this;  
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
    public int compareTo(TDP_Suffix_Solution other)
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
        if (!(obj instanceof TDP_Solution))
            return false;
        TDP_Solution other = (TDP_Solution) obj;
        if ((obj instanceof TDP_Suffix_Solution)) 
            return this.solutionToTuples().equals(other.solutionToTuples());
        else
            return this.solutionToTuples_strict_order().equals(other.solutionToTuples_strict_order());
    }
}
