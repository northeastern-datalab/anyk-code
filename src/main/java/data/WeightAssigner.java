package data;

import java.util.concurrent.ThreadLocalRandom;

/** 
 * This class is responsible for assigning weights to input tuples.
 * The supported distributions are:
 * <ul>
 * <li>Uniform. The weights are integers drawn from [0, {@link #max_n}).</li>
 * <li>Lexicographic. The weights are set in a way that prioritizes the first relation, then the second, and so on.
 * The tuples within a relation are weighted in ascending order (according to their index).</li>
 * <li>Reverse Lexicographic. Like Lexicographic, but here the order (of the important relations) is reversed (the last one is the most important).</li>
 * <li>Gaussian. </li>
 * </ul>
 * To add a new weight distribution:
 * <ul>
 * <li>In this class, define the appropriate data structures needed and the appropriate setters.</li>
 * <li>Specify the way the weights are assigned in {@link #get_tuple_weight}.<\li>
 * <li>Specify a way to parse it from the command line in {@link data.Database_Query_Generator#parse_weight_distribution_from_arg}.</li>
 * <li>Add it to the helper message of {@link data.Database_Query_Generator#common_command_line_options}.</li>
 * </ul>
 * @author Nikolaos Tziavelis
*/
public class WeightAssigner 
{
    private final static double DEFAULT_MAX_WEIGHT = 10000.0;
    private String weight_distribution;
    private double max_weight;
    private int max_n;
    private int num_of_relations;
    private double mean;
    private double stddev;

    /** 
     * The constructor initializes it to a uniform distribution.
    */
    public WeightAssigner()
    {
        this.weight_distribution = "uniform";
        this.max_weight = DEFAULT_MAX_WEIGHT;
    }

    /** 
     * Sets the distribution to Uniform with a default max weight of 10000.
    */
    public void set_uniform_distribution()
    {
        this.weight_distribution = "uniform";
        this.max_weight = DEFAULT_MAX_WEIGHT;
    }

    /** 
     * Sets the distribution to Uniform with a given max weight.
     * @param max_weight An upper bound (exclusive) for the assigned weights.
    */
    public void set_uniform_distribution(double max_weight)
    {
        this.weight_distribution = "uniform";
        this.max_weight = max_weight;
    }    

    /** 
     * Sets the distribution to Lexicographic.
     * @param max_n The maximum number of tuples in a relation.
     * @param num_of_relations The number of relations.
    */
    public void set_lexicographic_distribution(int max_n, int num_of_relations)
    {
        this.weight_distribution = "lex";
        this.max_n = max_n;
        this.num_of_relations = num_of_relations;
    }
    
    /** 
     * Sets the distribution to Reverse Lexicographic.
     * @param max_n The maximum number of tuples in a relation.
     * @param num_of_relations The number of relations.
    */
    public void set_reverse_lexicographic_distribution(int max_n, int num_of_relations)
    {
        this.weight_distribution = "revlex";
        this.max_n = max_n;
        this.num_of_relations = num_of_relations;
    }

    /** 
     * Sets the distribution to a (0, 1) Gaussian.
    */
    public void set_gaussian()
    {
        this.weight_distribution = "gauss";
        this.mean = 0.0;
        this.stddev = 1.0;
    }

    /** 
     * Sets the distribution to a Gaussian with the given mean and standard deviation.
    */
    public void set_gaussian(double mean, double standard_deviation)
    {
        this.weight_distribution = "gauss";
        this.mean = mean;
        this.stddev = standard_deviation;
    }

	/**
	 * Returns the weight of the tuple according to the weight distribution specified.
	 * @param tup_no The index of the tuple (starting from 0).
	 * @param relation_no The index of the relation (starting from 0).
	 */
	protected double get_tuple_weight(int tup_no, int relation_no)
	{
		Double res = null;
		if (this.weight_distribution.equals("uniform"))
		{
			res = ThreadLocalRandom.current().nextDouble(0.0, max_weight);
		}
		else if (this.weight_distribution.equals("lex"))
		{
			res = (tup_no + 1) * Math.pow(this.max_n, 2 * (this.num_of_relations - (relation_no - 1)));
		}
		else if (this.weight_distribution.equals("revlex"))
		{
			res = (tup_no + 1) * Math.pow(this.max_n, 2 * relation_no);
		}
		else if (this.weight_distribution.equals("gauss"))
		{
			res = ThreadLocalRandom.current().nextGaussian() * this.stddev + this.mean;
		}
		else
		{
			System.err.println("Unknown weight distribution");
			System.exit(1);
		}
		return res;
	}
}
