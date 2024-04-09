package entities.paths;

import java.util.List;

import entities.Tuple;

/** 
 * A follower solution follows the order that some other "leading" prefix solution discovers.
 * Thus, when it is popped from the PQ of {@link algorithms.paths.DP_PartPlus}, 
 * it does not generate (more than 1) new succesor solutions as candidates.
 * Instead, it looks up the next best suffix from the leading prefix and replaces its suffix with that one.
 * @see algorithms.paths.DP_PartPlus
 * @author Nikolaos Tziavelis
*/
public class DP_Prefix_Solution_Follower extends DP_Solution
{
    /** 
	 * The prefix of the solution.
	*/
    public DP_Prefix_Solution prefix;
    /** 
	 * The suffix of the solution (note that common suffixes among different solutions are not shared).
	*/
    public DP_Suffix_Solution suffix;
    /** 
	 * The position of the current suffix in the sorted list of suffixes given the prefix.
	*/
    public int rank_of_suffix;

    public DP_Prefix_Solution_Follower(DP_Prefix_Solution pref, int rank)
    {
        this.prefix = pref;
        this.rank_of_suffix = rank;
    }

    public DP_Prefix_Solution_Follower(DP_Prefix_Solution pref, DP_Suffix_Solution suff, int rank)
    {
        this.prefix = pref;
        this.suffix = suff;
        this.rank_of_suffix = rank;
        this.cost = pref.cost + suff.cost;
    }
    
    /** 
     * Setter for the suffix field
     */
    public void set_suffix(DP_Suffix_Solution suff)
    {
    	this.suffix = suff;
        this.cost = this.prefix.cost + suff.cost;
    }

    public double get_final_cost()
    {
        return this.cost;
    }

    public double get_length()
    {
        return this.prefix.length + this.suffix.get_length();
    }

    /** 
     * @return String A string representation of the solution 
     */
    public String solutionToString()
    {
    	StringBuilder builder = new StringBuilder();
        builder.append(this.prefix.solutionToString());
        builder.append(this.suffix.solutionToString());
    	return builder.toString();
    }
    
    /** 
     * @return List<Tuple> The list of tuples that the solution represents. 
     * The tuples may be returned in arbitrary order.
     */
    public List<Tuple> solutionToTuples()
    {
        List<Tuple> res = this.prefix.solutionToTuples();
        DP_Suffix_Solution current = this.suffix;  
        while (current != null)
        {
            if (current.first_decision.target.state_info instanceof Tuple)
            {
                res.add((Tuple) current.first_decision.target.state_info);
            }
            current = current.shorter_suffix;
        }
        return res;
    }
    
    public List<Tuple> solutionToTuples_strict_order()
    {
        List<Tuple> res = this.prefix.solutionToTuples_strict_order();
        DP_Suffix_Solution current = this.suffix;  
        while (current != null)
        {
            if (current.first_decision.target.state_info instanceof Tuple)
            {
                res.add((Tuple) current.first_decision.target.state_info);
            }
            current = current.shorter_suffix;
        }
        return res;
    }

    @Override
    public boolean equals(Object obj) 
    {
        if (!(obj instanceof DP_Solution))
            return false;
        DP_Solution other = (DP_Solution) obj;
        return this.solutionToTuples_strict_order().equals(other.solutionToTuples_strict_order());
    }

}
