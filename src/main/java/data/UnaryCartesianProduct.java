package data;

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

// A subclass of the data generator.
// The relations are using bag semantics and are 
// generated with one attribute and the same value 0 (plus the weight).
// The join pattern is a cartesian product (all tuples join with value = 0).
public class UnaryCartesianProduct extends Database_Query_Generator
{	
    public UnaryCartesianProduct(int n, int l)
	{
        super(n, l);
    }

    public UnaryCartesianProduct(List<Integer> n_list, int l)
	{
        super(n_list, l);
    }

	public UnaryCartesianProduct(int n, int l, WeightAssigner weight_assigner)
	{
        super(n, l, weight_assigner);
    }
    
    public UnaryCartesianProduct(List<Integer> n_list, int l, WeightAssigner weight_assigner)
	{
        super(n_list, l, weight_assigner);
    }

	@Override
	protected void populate_database()
	{
		Relation r;
		double tup_vals[];
		double tup_weight;
        Tuple new_tuple;
        int n;
        
        // In each iteration create one relation
		for (int relation_no = 1; relation_no <= l; relation_no++)
		{
            n = n_list.get(relation_no - 1);
            r = new Relation("R" + relation_no, new String[]{"A"});

            for (int i = 0; i < n; i++)
            {
                // Instantiate a tuple
				tup_vals = new double[1];
				tup_vals[0] = 0;
				tup_weight = this.weight_assigner.get_tuple_weight(i, relation_no - 1);	
                new_tuple = new Tuple(tup_vals, tup_weight, r);
                r.insert(new_tuple);
            }

            database.add(r);
		}
    }

	
    /** 
     * @param args
     */
    public static void main(String[] args) 
	{
        // Parse the command line
        // l is required
        // Either n or r have to be set
        // Higher priority is given to n if both are set
        Options options = new Options();

        // First parse the options that are common to all generators
        for (Option option : common_command_line_options()) options.addOption(option);

        // Generator-specific options below
        Option r_option = new Option("r", "outputSize", true, "desired output size");
        r_option.setRequired(false);
        options.addOption(r_option);

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

        Database_Query_Generator gen = null;
        Integer n = null;
        int l = Integer.parseInt(cmd.getOptionValue("relationNo"));
        if (cmd.hasOption("relationSize"))
        {
            n = Integer.parseInt(cmd.getOptionValue("relationSize"));
            gen = new UnaryCartesianProduct(n, l);
        }
        else
        {
            System.err.println("Either -n or -r has to be set!");
            System.exit(1);
        }
        gen.parse_common_args(cmd);
        gen.create();
        gen.print_database();
	}

}
