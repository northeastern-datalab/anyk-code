package data;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import entities.Relation;
import entities.Tuple;

/** 
 * A subclass of the data generator {@link data.Database_Query_Generator}.
 * The relations are generated with two attributes.
 * The join pattern between the relations is skewed: 
 * The values are drawn from a Gaussian distribution with mean 0 and a provided standard deviation (default=n/10),
 * and are then rounded to the closest integer.
 * If the standard deviation is too low compared to the size of the relations n, 
 * the generator might take a very long time to produce the relation (because of set semantics).
 * @author Nikolaos Tziavelis
*/
public class BinaryGaussPattern extends Database_Query_Generator
{	
	/** 
	 * The standard deviation of the Gaussian distribution.
	*/
	private double stddev;
	/** 
	 * The type of the query (path, star, etc.).
	 * Only affects the names of the attributes.
	*/
	private String query;

	public BinaryGaussPattern(int n, int l, double stddev, String query, WeightAssigner weight_assigner)
	{
		super(n, l, weight_assigner);
		this.stddev = stddev;
		this.query = query;
	}

	public BinaryGaussPattern(int n, int l, double stddev, String query)
	{
		super(n, l);
		this.stddev = stddev;
		this.query = query;
	}

	public BinaryGaussPattern(int n, int l, String query)
	{
		super(n, l);
        this.stddev = n / 10.0;
		this.query = query;
	}
	
	@Override
	protected void populate_database()
	{
		Relation r;
		double tup_vals[];
		double tup_weight;
		int attribute_no = 1;
		Set<Tuple> non_duplicate_tuples;
		Tuple new_tuple;
		int n;

		// In iteration relation_no, we populate relation relation_no with random values
		for (int relation_no = 1; relation_no <= l; relation_no++)
		{
			n = n_list.get(relation_no - 1);
			
			// Instantiate relation object
			if (query.equals("path"))
			{
				// In a path, the attribute of the left relation is the same as the attribute of the right relation
				r = new Relation("R" + relation_no, new String[]{"A" + attribute_no, "A" + (attribute_no + 1)});
				attribute_no += 1;
			}
			else if (query.equals("star"))
			{
				// In a star, all the relations join on A1, the first attribute of R1
				r = new Relation("R" + relation_no, new String[]{"A1", "A" + (attribute_no + 1)});
				attribute_no += 1;
			}
			else if (query.equals("cycle"))
			{
				// In a cycle, do the same as the path except for the last relation that must join back to the first one
				if (relation_no != l)
				{
					r = new Relation("R" + relation_no, new String[]{"A" + attribute_no, "A" + (attribute_no + 1)});
					attribute_no += 1;
				}
				else
				{
					r = new Relation("R" + relation_no, new String[]{"A" + attribute_no, "A1"});
				}
			}
			else
			{
				// If none of the above is specified, just make each relation have separate attributes
				r = new Relation("R" + relation_no, new String[]{"A" + attribute_no, "A" + (attribute_no + 1)});
				attribute_no += 1;				
			}

			// Add the random tuples to a set in order to avoid duplicates
			non_duplicate_tuples = new HashSet<Tuple>();
			while (non_duplicate_tuples.size() < n)
			{
				// Instantiate a random tuple
				tup_vals = new double[2];
				tup_vals[0] = (double) Math.round(ThreadLocalRandom.current().nextGaussian() * this.stddev);
				tup_vals[1] = (double) Math.round(ThreadLocalRandom.current().nextGaussian() * this.stddev);
				tup_weight = this.weight_assigner.get_tuple_weight(non_duplicate_tuples.size(), relation_no);
				new_tuple = new Tuple(tup_vals, tup_weight, r);

				non_duplicate_tuples.add(new_tuple);
			}

			// Go through the set and add the tuples to the relation
			for (Tuple t : non_duplicate_tuples) r.insert(t);
			database.add(r);
		}
	}
	
	public static void main(String[] args) 
	{
        // Parse the command line
        Options options = new Options();

		// First parse the options that are common to all generators
		for (Option option : common_command_line_options()) options.addOption(option);

		// Generator-specific options below
		Option q_option = new Option("q", "queryType", true, "the type of the query (path, star, cycle)");
        q_option.setRequired(true);
		options.addOption(q_option);

        Option stddev_option = new Option("std", "standardDeviation", true, "standard deviation of Gaussian distribution");
        stddev_option.setRequired(false);
        options.addOption(stddev_option);


        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;
        try
        {
            cmd = parser.parse(options, args);
        } 
        catch (ParseException e) 
        {
            System.err.println(e.getMessage());
            formatter.printHelp(getName(), options);
            System.exit(1);
        }

		String query = cmd.getOptionValue("queryType");
		int n = -1;
		if (cmd.hasOption("relationSize")) n = Integer.parseInt(cmd.getOptionValue("relationSize"));
		else
		{
			System.err.println("-n has to be set!");
            System.exit(1);
		}
		int l = Integer.parseInt(cmd.getOptionValue("relationNo"));
		double stddev;
        if (cmd.hasOption("standardDeviation")) stddev = Double.parseDouble(cmd.getOptionValue("standardDeviation"));
        else stddev = n / 10.0;
        Database_Query_Generator gen = new BinaryGaussPattern(n, l, stddev, query);
		gen.parse_common_args(cmd);
        gen.create();
        gen.print_database();
	}
}
