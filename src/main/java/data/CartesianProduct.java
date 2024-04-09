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

// A subclass of the data generator.
// The relations are generated with three attributes.
// The join pattern is a cartesian product (all tuples join with value = 0).
// The midddle attrribute differentiates the tuples to keep set semantics.
public class CartesianProduct extends Database_Query_Generator
{	
    public CartesianProduct(int n, int l)
	{
        super(n, l);
    }

    public CartesianProduct(List<Integer> n_list, int l)
	{
        super(n_list, l);
    }

	public CartesianProduct(int n, int l, WeightAssigner weight_assigner)
	{
        super(n, l, weight_assigner);
    }
    
    public CartesianProduct(List<Integer> n_list, int l, WeightAssigner weight_assigner)
	{
        super(n_list, l, weight_assigner);
    }

	@Override
	protected void populate_database()
	{
		Relation r;
		double tup_vals[];
		double tup_weight;
		int attribute_no = 1;
        Tuple new_tuple;
        int n;
        
        // In each iteration create one relation
		for (int relation_no = 1; relation_no <= l; relation_no++)
		{
            n = n_list.get(relation_no - 1);

            // In a path, the attribute of the left relation is the same as the attribute of the right relation
            r = new Relation("R" + relation_no, new String[]{"A" + attribute_no, "A" + (attribute_no + 1),  "A" + (attribute_no + 2)});
            attribute_no += 2;

            for (int i = 0; i < n; i++)
            {
                // Instantiate a tuple
				tup_vals = new double[3];
				tup_vals[0] = 0;
                tup_vals[1] = i;
                tup_vals[2] = 0;
				tup_weight = this.weight_assigner.get_tuple_weight(i, relation_no - 1);	
                new_tuple = new Tuple(tup_vals, tup_weight, r);
                r.insert(new_tuple);
            }

            database.add(r);
		}
    }
    
    
    /** 
     * @param num
     * @param no_factors
     * @return List<Integer>
     */
    // Decomposes a number num into a fixed number of (integer) factors
    // such that their product is as close as possible to num
    public static List<Integer> decompose_number(int num, int no_factors)
    {
        // First calculate the no_factors'th root
        double root = Math.pow(num, 1.0 / no_factors);
        int int_root = (int) Math.floor(root);

        // Initialize the list of factors with the (rounded down) root
        List<Integer> n_list = new ArrayList<>();
        for (int i = 0; i < no_factors; i++) n_list.add(int_root);

        // The product could now be less than num
        int current_product = (int) Math.pow(int_root, no_factors);
        
        // Iteratively try to increase the product by replacing some of the factors with factor+1
        int no_of_factors_to_change = 0;
        int previous_product;
        while (true)
        {
            previous_product = current_product;
            current_product = current_product / int_root;
            current_product = current_product * (int_root + 1);

            // Stop the iteration when we exceed the number
            if (current_product > num)
            {
                // At this point, check if the current or the previous product are closer
                if (current_product - num < num - previous_product) no_of_factors_to_change++;
                break;
            }

            no_of_factors_to_change++;
        }

        // Apply the changes
        for (int i = 0; i < no_of_factors_to_change; i++) n_list.set(i, int_root + 1);

        /*
        System.out.println(n_list);
        int print = 1;
        for (int i = 0; i < no_factors; i++) print = print * n_list.get(i);
        System.out.println(print);
        */

        return n_list;
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
        Integer n = null, r = null;
        int l = Integer.parseInt(cmd.getOptionValue("relationNo"));
        if (cmd.hasOption("relationSize"))
        {
            n = Integer.parseInt(cmd.getOptionValue("relationSize"));
            gen = new CartesianProduct(n, l);
        }
        else if (cmd.hasOption("outputSize"))
        {
            r = Integer.parseInt(cmd.getOptionValue("outputSize"));
            gen = new CartesianProduct(decompose_number(r, l), l);
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
