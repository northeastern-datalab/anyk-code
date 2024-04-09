package data;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import entities.Relation;

/** 
 * Abstract class for a synthetic data generator.
 * Produces a list of database relations.
 * The generated database can be accessed by the getter method {@link #get_database}
 * or written to an output stream by {@link #print_database}.
 * For specific queries and patterns, 
 * subclasses need to implement {@link #populate_database} appropriately.
 * @author Nikolaos Tziavelis
*/
public abstract class Database_Query_Generator 
{
	/**
	 * The name of the output file(s) where the database can be written to.
	 * If multiple files will be written, then "_1", "_2", etc. will be appended.
	 */	
	protected String out = null;
	/**
	 * If true, the relations will be printed in different files and without headers.
	 */
	protected boolean multipleFilesOutputFlag = false;
	/**
	 * The sizes of the relations of the database.
	 */	
	protected List<Integer> n_list;
	/**
	 * The number of relations in the database.
	 */	
	protected int l;
	/**
	 * The generated database as a list of relations.
	 */	
	protected List<Relation> database;
	/**
	 * An object responsible for assigning weights to the generated input tuples.
	 */	
	protected WeightAssigner weight_assigner;

	/**
	 * Constructor for the case where all the relations have the same size n and the weights are assigned in a default way.
	 */	
	public Database_Query_Generator(int n, int l)
	{
		this.l = l;
		this.n_list = new ArrayList<Integer>();
		for (int i = 0; i < l; i ++) this.n_list.add(n);
		this.weight_assigner = new WeightAssigner();
	}

	/**
	 * Constructor for the case where we have a different size per relation and the weights are assigned in a default way.
	 */	
	public Database_Query_Generator(List<Integer> n_list, int l)
	{
		this.l = l;
		this.n_list = n_list;
		this.weight_assigner = new WeightAssigner();
	}

	/**
	 * Constructor for the case where all the relations have the same size n and the weights are assigned by a given parameter.
	 */	
	public Database_Query_Generator(int n, int l, WeightAssigner weight_assigner)
	{
		this.l = l;
		this.n_list = new ArrayList<Integer>();
		for (int i = 0; i < l; i ++) this.n_list.add(n);
		this.weight_assigner = weight_assigner;
	}

	/**
	 * Constructor for the case where we have a different size per relation and the weights are assigned in a default way.
	 */	
	public Database_Query_Generator(List<Integer> n_list, int l, WeightAssigner weight_assigner)
	{
		this.l = l;
		this.n_list = n_list;
		this.weight_assigner = weight_assigner;
	}

	/**
	 * Set the weight assigner to a given object.
	 */
	public void setWeightAssigner(WeightAssigner weight_assigner)
	{
		this.weight_assigner = weight_assigner;
	}
	
	/** 
	 * Populates the relations of the database according to a subclass logic.
	 */
	protected abstract void populate_database();
	
	/**
	 * Creates the database.
	 */
	public void create()
	{
		database = new ArrayList<Relation>();
		populate_database();
	}

	/**
	 * Prints the database to the specified output file(s).
	 */
	public void print_database()
	{
		if (!multipleFilesOutputFlag)
		{
			PrintStream outStream = System.out;
			if (out != null)
			{
				try 
				{
					outStream = new PrintStream(out);
				} 
				catch (FileNotFoundException e) 
				{
					System.err.println("Error opening file " + out);
					e.printStackTrace();
				}
			}
			for (int i = 0; i < l; i++) outStream.print(database.get(i).toString());
			if (outStream != System.out) outStream.close();	
		}
		else
		{
			if (out == null)
			{
				System.err.println("If multipleFilesOutput is set, then outputFile has to be set as well.");
				System.exit(1);
			}

			for (int i = 0; i < l; i++)
			{
				PrintStream outStream = null;
				String outFileExtension = out.split("\\.")[1];
				String outFileName = out.split("\\.")[0];
				String outFile = outFileName + "_" + (i + 1) + "." + outFileExtension;
				try 
				{
					outStream = new PrintStream(outFile);
				} 
				catch (FileNotFoundException e) 
				{
					System.err.println("Error opening file " + outFile);
					e.printStackTrace();
				}
				outStream.print(database.get(i).toString_NoHeader());
				if (outStream != System.out) outStream.close();	
			}
		}
	}

	/**
	 * Returns the database as a list of relations.
	 */
	public List<Relation> get_database()
	{
		return this.database;
	}

	/**
	 * Parses a string from the command line that specifies a weight distribution
	 * @param arg A astring from the command line.
	 * @return An object that assigns weights to tuples.
	 */
	protected static WeightAssigner parse_weight_distribution_from_arg(String arg, Database_Query_Generator gen)
	{
		WeightAssigner res = new WeightAssigner();
		String[] args = arg.split("\\|");
		if (args[0].equals("uniform"))
		{
			if (args.length == 1) res.set_uniform_distribution();
			else if (args.length == 2) res.set_uniform_distribution(Double.parseDouble(args[1]));
			else
			{
				System.err.println("Incorrect format for weight distribution");
				System.exit(1);
			}
		}
		else if (args[0].equals("lex"))
		{
			res.set_lexicographic_distribution(Collections.max(gen.n_list), gen.l);
		}
		else if (args[0].equals("revlex"))
		{
			res.set_reverse_lexicographic_distribution(Collections.max(gen.n_list), gen.l);
		}
		else if (args[0].equals("gauss"))
		{
			if (args.length == 1) res.set_gaussian();
			else if (args.length == 3) res.set_gaussian(Double.parseDouble(args[1]), Double.parseDouble(args[2]));
			else
			{
				System.err.println("Incorrect format for weight distribution");
				System.exit(1);
			}
		}
		else
		{
			System.err.println("Unknown weight distribution");
			System.exit(1);
		}
		return res;
	}

	/**
	 * Parses command-line arguments that are common in all subclasses of different data generators
	 * @param cmd The command-line arguments.
	 */
	protected void parse_common_args(CommandLine cmd)
	{
		if (cmd.hasOption("weights"))
		{
			WeightAssigner weight_assigner = parse_weight_distribution_from_arg(cmd.getOptionValue("weights"), this);
			this.setWeightAssigner(weight_assigner);
		} 
        if (cmd.hasOption("outputFile")) out = cmd.getOptionValue("outputFile");
		if (cmd.hasOption("multipleFilesOutput")) this.multipleFilesOutputFlag = true;
		if (cmd.hasOption("multipleFilesOutput") && !cmd.hasOption("outputFile"))
		{
			System.err.println("If multipleFilesOutput is set, then outputFile has to be set as well.");
			System.exit(1);
		}
	}

	/**
	 * Returns a list of options that are common for all data generators.
	 */
	protected static List<Option> common_command_line_options()
	{
		List<Option> res = new ArrayList<Option>();

		Option n_option = new Option("n", "relationSize", true, "number of tuples per relation");
        n_option.setRequired(false);
        res.add(n_option);

        Option l_option = new Option("l", "relationNo", true, "number of relations");
        l_option.setRequired(true);
        res.add(l_option);

        Option w_option = new Option("w", "weights", true, "Can be uniform|uniform-max_weight|lex|revlex|gauss|gauss-mean-stddev");
        w_option.setRequired(false);
        res.add(w_option);

		Option out_option = new Option("o", "outputFile", true, "directory of output file(s) (default: stdout)");
        out_option.setRequired(false);
        res.add(out_option);

		Option many_files_option = new Option("mf", "multipleFilesOutput", false, "print relations in different files and without headers");
        many_files_option.setRequired(false);
        res.add(many_files_option);

		return res;
	}

    /**
	 * Utility method for help messages.
     * @return String
	 */
    protected static String getName()
    {
        String className = Thread.currentThread().getStackTrace()[2].getClassName(); 
        return className;
    }
}