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
 * The join pattern between the relations is random in the sense that 
 * we randomly sample the attribute values from a fixed domain, given as a parameter.
 * A small domain size will result in a very dense join pattern.
 * A large domain size will result in many tuples not being part of the join result.
 * @author Nikolaos Tziavelis
*/
public class BinaryRandomPattern extends Database_Query_Generator
{	
	/** 
	 * The size of the domain of the attributes.
	*/
	private int domain;
	/** 
	 * The type of the query (path, star, etc.).
	 * Only affects the names of the attributes.
	*/
	private String query;

	public BinaryRandomPattern(int n, int l, int domain, String query)
	{
		super(n, l);

		// CAUTION: if the domain is too small, then this method would enter an endless loop
		// The maximum n we can generate with domain is by choosing all the domain values in the first col
		// followed by n values in the second col
		long allowed_pairs = (long) domain * (long) domain;
		if (allowed_pairs < n)
		{
			System.err.println("Domain too small for the specified n!");
			System.exit(1);
		}

		this.domain = domain;
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
				tup_vals[0] = ThreadLocalRandom.current().nextInt(domain);
				tup_vals[1] = ThreadLocalRandom.current().nextInt(domain);
				tup_weight = get_uniform_tuple_weight();
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

		Option q_option = new Option("q", "queryType", true, "the type of the query (path, star, cycle)");
        q_option.setRequired(true);
		options.addOption(q_option);
		
        Option n_option = new Option("n", "relationSize", true, "number of tuples per relation");
        n_option.setRequired(true);
        options.addOption(n_option);

        Option l_option = new Option("l", "databaseSize", true, "number of relations");
        l_option.setRequired(true);
        options.addOption(l_option);

        Option dom_option = new Option("dom", "domain", true, "size of domain to sample attribute values from");
        dom_option.setRequired(false);
        options.addOption(dom_option);

        Option out_option = new Option("o", "outputFile", true, "directory of output file (default: stdout)");
        out_option.setRequired(false);
        options.addOption(out_option);

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
		int n = Integer.parseInt(cmd.getOptionValue("relationSize"));
		int l = Integer.parseInt(cmd.getOptionValue("databaseSize"));
		int domain;
        if (cmd.hasOption("domain")) domain = Integer.parseInt(cmd.getOptionValue("domain"));
        else domain = n;
        Database_Query_Generator gen = new BinaryRandomPattern(n, l, domain, query);
        if (cmd.hasOption("outputFile")) gen.setOutputFile(cmd.getOptionValue("outputFile"));
        gen.create();
        gen.print_database();
	}
}
