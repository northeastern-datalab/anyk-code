package data;

import java.util.ArrayList;
import java.util.List;

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
 * A subclass of the data generator @link data.Database_Query_Generator} for simple cycle queries.
 * The relations are generated with two attributes.
 * The join pattern creates a lot of results and is also given in Ngo et al., JACM'98 
 * <a href="https://doi/10.1145/3180143">https://doi/10.1145/3180143</a>. 
 * One half of the left relation creates a cartesian product with one half of the right relation.
 * The other halfs are connected with a single edge per tuple.
 * The pattern is alternated between consecutive stages.
 * The pattern is perfectly symmetric when the cycles are of even length.
 * @author Nikolaos Tziavelis
*/
public class Cycle_HeavyLightPattern extends Database_Query_Generator
{	
	public Cycle_HeavyLightPattern(int n, int l)
	{
		super(n, l);
	}
	
	public Cycle_HeavyLightPattern(int n, int l, WeightAssigner weight_assigner)
	{
		super(n, l, weight_assigner);
	}

	@Override
	protected void populate_database()
	{
		double[] tup_vals;
		Relation r;
		Tuple tuple;
		int attribute_no = 1;
		List<Double> val_list1, val_list2;
		int n;

		// In iteration relation_no, we initialize relation relation_no
		for (int relation_no = 1; relation_no <= l; relation_no++)
		{
			n = n_list.get(relation_no - 1);

			// Create two lists of values that we will use to populate the relations
			val_list1 = new ArrayList<Double>();
			for (double i = 1.0; i <= n; i++)
			{
				if (i <= n / 2) val_list1.add(i);
				else val_list1.add(0.0);
			}
			val_list2 = new ArrayList<Double>();
			for (double i = 1.0; i <= n; i++)
			{
				if (i <= n / 2) val_list2.add(0.0);
				else val_list2.add(i);
			}

			// Instantiate relation object
			if (relation_no != l)
				r = new Relation("R" + relation_no, new String[]{"A" + attribute_no, "A" + (attribute_no + 1)});
			else
			r = new Relation("R" + relation_no, new String[]{"A" + attribute_no, "A1"});
			attribute_no += 1;

			for (int j = 0; j < n ; j++)
			{
				tup_vals = new double[2];
				tuple = new Tuple(tup_vals, this.weight_assigner.get_tuple_weight(j, relation_no), r);
				r.insert(tuple);				
			}

			//Collections.shuffle(val_list);
			if (relation_no % 2 == 0)
			{
				for (int j = 0; j < n ; j++)
				{
					tup_vals = r.tuples.get(j).values;
					tup_vals[0] = val_list1.get(j);		
					tup_vals[1] = val_list2.get(j);	
				}
			}
			else
			{
				//Collections.shuffle(val_list);
				for (int j = 0; j < n ; j++)
				{
					tup_vals = r.tuples.get(j).values;
					tup_vals[0] = val_list2.get(j);	
					tup_vals[1] = val_list1.get(j);		
				}
			}

			database.add(r);
		}
	}

	public static void main(String[] args) 
	{
        // Parse the command line
        Options options = new Options();

		// First parse the options that are common to all generators
		for (Option option : common_command_line_options()) options.addOption(option);

		// No generator-specific options

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

		int n = Integer.parseInt(cmd.getOptionValue("relationSize"));
		int l = Integer.parseInt(cmd.getOptionValue("relationNo"));
        Database_Query_Generator gen = new Cycle_HeavyLightPattern(n, l);
		gen.parse_common_args(cmd);
        gen.create();
        gen.print_database();
	}

}
