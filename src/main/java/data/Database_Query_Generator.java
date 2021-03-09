package data;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

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
	 * An output stream where the database can be written to.
	 */	
	protected PrintStream out;
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
	 * Constructor for when all the relations have the same size n.
	 */	
	public Database_Query_Generator(int n, int l)
	{
		this.l = l;
		this.n_list = new ArrayList<Integer>();
		for (int i = 0; i < l; i ++) this.n_list.add(n);
		this.out = System.out;
	}

	/**
	 * Constructor that allows for a different size per relation.
	 */	
	public Database_Query_Generator(List<Integer> n_list, int l)
	{
		this.l = l;
		this.n_list = n_list;
		this.out = System.out;
	}

	/**
	 * Set the output file that will be created. If no file is specified, the default output will be used
	 * (stdout).
	 * @param fileName The path of the output file.
	 */
	public void setOutputFile(String fileName)
	{
		try 
		{
			this.out = new PrintStream(fileName);
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
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
	 * Prints the database to the specified output stream
	 */
	public void print_database()
	{
		for (int i = 0; i < l; i++)
			printRelation(database.get(i));
		if (out != System.out) this.out.close();		
	}

	/**
	 * Prints a relation to the specified output stream
	 */
	private void printRelation(Relation r)
	{
		this.out.print(r.toString());
	}

	/**
	 * Returns the database as a list of relations
	 */
	public List<Relation> get_database()
	{
		return this.database;
	}

	/**
	 * Returns a random number in a specific range (uniform distribution)
	 * Used whenever tuple costs are generated uniformly at random
	 * Hardcoded maximum cost = 10,000
	 */
	protected double get_uniform_tuple_weight()
	{
		return ThreadLocalRandom.current().nextDouble(0.0, 10000.0);
	}

	/**
	 * Returns the weight of the tuple according to the weight distribution specified
	 * @param weight_distr Specifies the distribution of input weights.
	 * @param tup_no The index of the tuple (starting from 1)
	 * @param relation_no The index of the relation (starting from 1)
	 */
	protected double get_tuple_weight(String weight_distr, int tup_no, int relation_no)
	{
		Double res = null;
		if (weight_distr.equals("uniform"))
		{
			/**
			 * Returns a random number in a specific range (uniform distribution)
			 * Used whenever tuple costs are generated uniformly at random
			 * Hardcoded maximum cost = 10,000
			*/
			res = ThreadLocalRandom.current().nextDouble(0.0, 10000.0);
		}
		else if (weight_distr.equals("lex"))
		{
			int max_n = Collections.max(n_list);
			res = tup_no * Math.pow(max_n, 2 * (l - relation_no));
		}
		else if (weight_distr.equals("revlex"))
		{
			int max_n = Collections.max(n_list);
			res = tup_no * Math.pow(max_n, 2 * (relation_no - 1));
		}
		else
		{
			System.err.println("Unknown weight distribution");
			System.exit(1);
		}
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