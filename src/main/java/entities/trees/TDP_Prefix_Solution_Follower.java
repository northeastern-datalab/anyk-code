package entities.trees;

import java.util.List;

import entities.Tuple;

/** 
 * A follower solution follows the order that some other "leading" prefix solution discovers.
 * Thus, when it is popped from the PQ of {@link algorithms.trees.TDP_PartPlus}, 
 * it does not generate (more than 1) new succesor solutions as candidates.
 * Instead, it looks up the next best suffix from the leading prefix and replaces its suffix with that one.
 * @see algorithms.trees.TDP_PartPlus
 * @author Nikolaos Tziavelis
*/
public class TDP_Prefix_Solution_Follower extends TDP_Solution
{
    /** 
	 * The prefix of the solution.
	*/
    public TDP_Prefix_Solution prefix;
    /** 
	 * The suffix of the solution (note that common suffixes among different solutions are not shared).
	*/
    public TDP_Suffix_Solution suffix;
    /** 
	 * The position of the current suffix in the sorted list of suffixes given the prefix.
	*/
    public int rank_of_suffix;

    public TDP_Prefix_Solution_Follower(TDP_Prefix_Solution pref, int rank)
    {
        this.prefix = pref;
        this.rank_of_suffix = rank;
    }

    public TDP_Prefix_Solution_Follower(TDP_Prefix_Solution pref, TDP_Suffix_Solution suff, int rank)
    {
        this.prefix = pref;
        this.suffix = suff;
        this.rank_of_suffix = rank;
        this.cost = pref.cost + suff.cost;
    }
    
    /** 
     * Setter for the suffix field
     */
    public void set_suffix(TDP_Suffix_Solution suff)
    {
    	this.suffix = suff;
        this.cost = this.prefix.cost + suff.cost;
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
        TDP_Suffix_Solution current = this.suffix;  
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
        TDP_Suffix_Solution current = this.suffix;  
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
        if (!(obj instanceof TDP_Solution))
            return false;
        TDP_Solution other = (TDP_Solution) obj;
        return this.solutionToTuples_strict_order().equals(other.solutionToTuples_strict_order());
    }

}
