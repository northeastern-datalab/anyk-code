package algorithms.paths;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Stack;

import org.jheaps.dag.HollowHeap;
import org.jheaps.tree.FibonacciHeap;
import org.jheaps.tree.PairingHeap;

import algorithms.Configuration;
import data.BinaryRandomPattern;
import data.Database_Query_Generator;
import entities.Relation;
import entities.paths.DP_Decision;
import entities.paths.DP_DecisionSet;
import entities.paths.DP_Path_Equijoin_Instance;
import entities.paths.DP_Prefix_Solution;
import entities.paths.DP_Prefix_Solution_Follower;
import entities.paths.DP_Problem_Instance;
import entities.paths.DP_Solution;
import entities.paths.DP_State_Node;
import entities.paths.DP_Suffix_Solution;
import entities.paths.Path_Equijoin_Query;
import util.CompileTimeConfig;
/** 
 * Implementation of Anyk-PartMemoized for DP, a ranked enumeration algorithm that improves upon {@link algorithms.paths.DP_Part}
 * by adding certain memoization. 
 * In particular, we store the order that a prefix explores a subspace and then reuse that order for other prefixes too.
 * The data structures needed to for memoization are contained in {@link entities.paths.DP_SuffixMemoizer}. 
 * One such object is pointed to from each {@link entities.paths.DP_DecisionSet}.
 * @author Nikolaos Tziavelis
*/
public abstract class DP_PartPlus extends DP_Anyk_Iterator
{
	/** 
	 * Maintains all the solutions that are candidates for the next best solution.
	*/
	protected PriorityQueue<DP_Solution> global_pq_binary_heap;
	protected FibonacciHeap<DP_Solution,Object> global_pq_fibonacci_heap;
	protected PairingHeap<DP_Solution,Object> global_pq_pairing_heap;
	protected HollowHeap<DP_Solution,Object> global_pq_hollow_heap;
	/** 
	 * Keeps track of how many results have been returned.
	*/
	private long iteration_no;

	/** 
	 * @param inst The DP problem to run any-k on.
	 * @param conf A configuration of execution parameters.
	*/		
	public DP_PartPlus(DP_Problem_Instance inst, Configuration conf)
    {
    	super(inst, conf);
		
    	// Initialize the global PQ with an empty prefix (that contains only the starting node)
		if (CompileTimeConfig.heap_type == "binary_heap")
			this.global_pq_binary_heap = new PriorityQueue<DP_Solution>(new Comparator<DP_Solution>() 
			{
				public int compare(DP_Solution s1, DP_Solution s2) 
				{
					return Double.compare(s1.get_final_cost(), s2.get_final_cost());
				}
			});
		else if (CompileTimeConfig.heap_type == "fibonacci_heap")
			this.global_pq_fibonacci_heap = new FibonacciHeap<DP_Solution,Object>(new Comparator<DP_Solution>() 
			{
				public int compare(DP_Solution s1, DP_Solution s2) 
				{
					return Double.compare(s1.get_final_cost(), s2.get_final_cost());
				}
			});
		else if (CompileTimeConfig.heap_type == "pairing_heap")
			this.global_pq_pairing_heap = new PairingHeap<DP_Solution,Object>(new Comparator<DP_Solution>() 
			{
				public int compare(DP_Solution s1, DP_Solution s2) 
				{
					return Double.compare(s1.get_final_cost(), s2.get_final_cost());
				}
			});
		else if (CompileTimeConfig.heap_type == "hollow_heap")
			this.global_pq_hollow_heap = new HollowHeap<DP_Solution,Object>(new Comparator<DP_Solution>() 
			{
				public int compare(DP_Solution s1, DP_Solution s2) 
				{
					return Double.compare(s1.get_final_cost(), s2.get_final_cost());
				}
			});

    	// The prefix we start with contains only the best decision to go from starting_node to stage 1
    	// That way, we guarantee that for top-2 we start taking successor solutions from stage 1
    	if (instance.starting_node.get_opt_cost() != Double.POSITIVE_INFINITY)	// Corner case: if no path can reach the terminal node, leave the pq empty
    	{
	    	DP_Prefix_Solution starting_prefix = new DP_Prefix_Solution(instance.starting_node.get_best_decision());
	    	add_to_pq(starting_prefix);    		
		}

		// By default, initialize the data structures needed lazily
		// (i.e., the first time they are accesed)
		if ((conf != null) && !conf.initialization_laziness)
			initialize_data_structures();

		this.iteration_no = 0;
    }

	/** 
	 * For this algorithm, the expansion, successor-taking and following steps are combined.
	 * As a result, it is difficult to return a popped result as soon as possible.
	 * To emulate the behavior of other any-k algorithms, return the top-1 result immediately (known from bottom-up DP)
	 * and then call the regular get_next method from the 2nd iteration onward.
	*/	
	public DP_Solution get_next()
	{
		this.iteration_no += 1;
		if (iteration_no > 2) return get_next_helper();
		else if (iteration_no == 1)
		{
			if (instance.starting_node.get_opt_cost() != Double.POSITIVE_INFINITY)
			{
				DP_Prefix_Solution res = new DP_Prefix_Solution(instance.starting_node.get_best_decision());
				while (!res.latest_decision.target.is_terminal())
					res = new DP_Prefix_Solution(res, res.latest_decision.target.get_best_decision());
				return res;	
			}
			else return null;
		
		}
		else if (iteration_no == 2)
		{
			get_next_helper();
			return get_next_helper();
		}
		return null;
	}

    public DP_Solution get_next_helper()
    {
		DP_Prefix_Solution curr_prefix;
		DP_Suffix_Solution curr_suffix = null;
		DP_Solution res = null;

		// Pop the best solution from the global PQ
		DP_Solution popped_solution;
		// If the PQ is empty, then we have enumerated all solutions
		if (CompileTimeConfig.heap_type == "binary_heap")
		{
			if (global_pq_binary_heap.isEmpty()) return null;
			popped_solution = global_pq_binary_heap.poll();
		}
		else if (CompileTimeConfig.heap_type == "fibonacci_heap")
		{
			if (global_pq_fibonacci_heap.isEmpty()) return null;
			popped_solution = global_pq_fibonacci_heap.deleteMin().getKey();
		}
		else if (CompileTimeConfig.heap_type == "pairing_heap")
		{
			if (global_pq_pairing_heap.isEmpty()) return null;
			popped_solution = global_pq_pairing_heap.deleteMin().getKey();
		}
		else if (CompileTimeConfig.heap_type == "hollow_heap")
		{
			if (global_pq_hollow_heap.isEmpty()) return null;
			popped_solution = global_pq_hollow_heap.deleteMin().getKey();
		}

		if (popped_solution instanceof DP_Prefix_Solution) 
		{
			//System.out.println("Popped solution (not a follower): " + popped_solution);
			res = popped_solution;
			curr_prefix = ((DP_Prefix_Solution) popped_solution);

			// Always try to take successor(s) at the length of the popped prefix
			for (DP_Decision succ : get_successors(curr_prefix.get_latest_decision()))
				add_to_pq(curr_prefix.create_successor(succ));  

			// This loop expands the prefix until complete or until a top-1 suffix is found in the sorted lists
			while (!curr_prefix.latest_decision.target.is_terminal())
			{
				List<DP_Suffix_Solution> sorted_suffixes = curr_prefix.latest_decision.target.decisions.sorted_suffixes;
				if (sorted_suffixes != null)
				{
					// The sorted suffixes have been initialized with the top-1 suffix
					// Finish the optimal expansion by using that top-1 suffix
					DP_Prefix_Solution_Follower expanded_solution = new DP_Prefix_Solution_Follower(curr_prefix, sorted_suffixes.get(0), 0);
					// The non-leading prefix follows the leading prefix of its decision set
					follow_leading_prefix(expanded_solution);
					// Create the corresponding suffix by looking up the best stored suffix from that non-leading prefix
					// This way we create a shared structure between the suffixes
					curr_suffix = sorted_suffixes.get(0);

					res = expanded_solution;
					break;
				}
				else
				{
					// Expand optimally
					curr_prefix = new DP_Prefix_Solution(curr_prefix, curr_prefix.latest_decision.target.get_best_decision());
					res = curr_prefix;
					// Take successor(s)
					for (DP_Decision succ : get_successors(curr_prefix.get_latest_decision()))
						add_to_pq(curr_prefix.create_successor(succ));
				}
			}

			// Continue traversing the current solution backwards to record suffixes
			if (curr_suffix == null) curr_suffix = new DP_Suffix_Solution(curr_prefix.latest_decision);
			else curr_suffix = new DP_Suffix_Solution(curr_suffix, curr_prefix.latest_decision);
			curr_prefix = curr_prefix.get_shorter();
			while (curr_prefix != null)
			{		
				// If it is the first time we visit, then the current prefix is the leading one
				// This is where we initialize the memoization structure
				DP_DecisionSet curr_decisionset = curr_prefix.latest_decision.target.decisions;
				if (curr_decisionset.sorted_suffixes == null)
				{
					//System.out.println("Stage " + curr_prefix.length + " : Recording " + curr_prefix + " as leading prefix");
					curr_decisionset.sorted_suffixes = new ArrayList<DP_Suffix_Solution>();
					curr_decisionset.subscribers = new ArrayList<DP_Prefix_Solution_Follower>();
				}
				
				// Record suffix in the sorted order (no need to do that for full suffixes)
				// As we traverse the solution backwards, create a suffix of the decisions so that we can store it in the sorted order
				record_suffixes(curr_prefix, curr_suffix);

				// Shorten the current solution by cutting off one decision
				curr_suffix = new DP_Suffix_Solution(curr_suffix, curr_prefix.latest_decision);
				curr_prefix = curr_prefix.get_shorter();
			}
		}
		else if (popped_solution instanceof DP_Prefix_Solution_Follower) 
		{
			res = popped_solution;
			//System.out.println("Popped solution is a follower: " + popped_solution);
			DP_Prefix_Solution_Follower follower = (DP_Prefix_Solution_Follower) popped_solution;
			follow_leading_prefix(follower);

			// Record the suffixes of the popped solution in the sorted lists
			curr_suffix = new DP_Suffix_Solution(follower.suffix, follower.prefix.latest_decision);
			curr_prefix = follower.prefix.get_shorter();
			while (curr_prefix != null)
			{
				record_suffixes(curr_prefix, curr_suffix);
				// Shorten the current prefix by cutting off one decision
				curr_suffix = new DP_Suffix_Solution(curr_suffix, curr_prefix.latest_decision);
				curr_prefix = curr_prefix.get_shorter();
			}
		}
		// If null is returned, then we have enumerated all solutions
		else if (popped_solution != null)
		{
			System.err.println("Critical error: Unkown object type in PQ");
			System.exit(1);
		}

		return res;
	}

	/** 
	 * Makes a given prefix "follow" the leading prefix that ends on the same decision set.
	 * Specifically, we find the next best suffix from the sorted list of suffixes discovered by the leading prefix.
	 * If it hasn't been computed yet, we make the given prefix wait until it has (a subscriber).
	 * @param follower A prefix solution that is a follower.
	 */
    private void follow_leading_prefix(DP_Prefix_Solution_Follower follower)
    {
		DP_DecisionSet decs = follower.prefix.latest_decision.target.decisions;
		int next_rank = follower.rank_of_suffix + 1;
		if (decs.sorted_suffixes.size() > next_rank)
		{
			// Look up the next best suffix and create a new candidate with it
			//System.out.println("The next best suffix from " + follower.prefix + " is " + decs.sorted_suffixes.get(next_rank)));
			add_to_pq(new DP_Prefix_Solution_Follower(follower.prefix, decs.sorted_suffixes.get(next_rank), next_rank));
		}
		else
		{
			// Subscribe
			//System.out.println("Prefix " + follower.prefix + " is subscribing for suffix with rank " + next_rank);
			decs.subscribers.add(new DP_Prefix_Solution_Follower(follower.prefix, next_rank));
		}
    }

	/** 
	 * Records a suffix as the next best option in the sorted list of suffixes at the end of the given prefix.
	 * @param prefix The prefix that will store the suffix.
	 * @param suffix The suffix that will be stored.
	 */
    private void record_suffixes(DP_Prefix_Solution prefix, DP_Suffix_Solution suffix)
    {
		DP_DecisionSet decs = prefix.latest_decision.target.decisions;
		//System.out.println("Stage " + prefix.length + " : Adding " + suffix + " to sorted suffix list with rank " + decs.sorted_suffixes.size());
		assert !list_contains_reference(decs.sorted_suffixes, suffix) :
			"Duplicate suffix in sorted list";
		assert decs.sorted_suffixes.isEmpty() || 
			Double.compare(decs.sorted_suffixes.get(decs.sorted_suffixes.size() - 1).get_cost() - 0.0000001, suffix.get_cost()) <= 0 :
			"Wrong order in suffix list";
			decs.sorted_suffixes.add(suffix);
		// Publish this suffix to all subscribers
		for (DP_Prefix_Solution_Follower subscriber : decs.subscribers)
		{
			//System.out.println("Subscriber " + subscriber.prefix + " receives rank-" + subscriber.rank_of_suffix + " suffix: " + suffix);
			assert subscriber.rank_of_suffix == decs.sorted_suffixes.size() - 1 : "Subscriber was waiting for suffix of different rank";
			subscriber.set_suffix(suffix);
			add_to_pq(subscriber);
		}
		// Clear the subscribers list
		decs.subscribers.clear();
    }

	/** 
	 * Expands the prefix solution into a full solution by following an optimal sequence of decisions.
	 * The full solution reaches a terminal node.
	 * @param pref A prefix solution to be expanded optimally.
	 * @return DP_Prefix_Solution A new object that is a full solution.
	 */
    protected DP_Prefix_Solution expand(DP_Prefix_Solution pref)
    {
    	DP_Prefix_Solution current;
    	DP_Decision next_best_decision;
    	current = pref;
    	while (!current.latest_decision.target.is_terminal())
    	{
    		next_best_decision = current.latest_decision.target.get_best_decision();
    		current = new DP_Prefix_Solution(current, next_best_decision);
		}
		// Precision may be a problem here, hence the precision cutoff
		//System.out.println(String.format("%.4f", (pref.get_cost() + pref.latest_decision.target.get_opt_cost())));
		//System.out.println(String.format("%.4f", current.get_cost()));
    	//assert String.format("%.4f", (pref.get_cost() + pref.latest_decision.target.get_opt_cost())).equals(String.format("%.4f", current.get_cost())); 
    	return current;
    }

	private void add_to_pq(DP_Solution sol)
	{
		if (CompileTimeConfig.heap_type == "binary_heap")
			this.global_pq_binary_heap.add(sol);
		else if (CompileTimeConfig.heap_type == "fibonacci_heap")
			this.global_pq_fibonacci_heap.insert(sol, null);
		else if (CompileTimeConfig.heap_type == "pairing_heap")
			this.global_pq_pairing_heap.insert(sol, null);
		else if (CompileTimeConfig.heap_type == "hollow_heap")
			this.global_pq_hollow_heap.insert(sol, null);
	}

	/** 
	 * Initialization of the data structures needed at each DP_DesicionSet
	 * in order to compute a partial order among decisions.
	 * @param decisions The set of decisions whose data structures will be initialized.
	 */
    public abstract void initialize_partial_order(DP_DecisionSet decisions);

	/** 
	 * Find the successors of a given decision among the other decisions in the {@link entities.paths.DP_DecisionSet}.
	 * The way that successors are computed depends on the variant (subclass).
	 * @param dec The decision whose successors will be computed.
	 * @return DP_Decision The successor decisions according to the (partial) order.
	 */
    public abstract List<DP_Decision> get_successors(DP_Decision dec);

	/** 
	 * Initializes the data structures needed for all nodes.
	 * Visits all the nodes in a DFS traversal.
	 */
    public void initialize_data_structures()
	{
		Stack<DP_State_Node> dfs_stack = new Stack<DP_State_Node>();
		dfs_stack.push(instance.starting_node);

		DP_State_Node curr_node;
		while (!dfs_stack.isEmpty())
		{
			curr_node = dfs_stack.pop();
			initialize_partial_order(curr_node.decisions);
			for (DP_Decision dec : curr_node.get_decisions())
			{
				if (!dec.target.decisions.partial_order_computed)
					dfs_stack.push(dec.target);
			}
		}
	}

	/*
	private boolean prefix_is_candidate(DP_Prefix_Solution pref)
	{
		if (pref == null) return true;
		DP_Prefix_Solution curr_p = null;
		for (DP_Solution p : global_pq)
		{
			if (p instanceof DP_Prefix_Solution)
				curr_p = (DP_Prefix_Solution) p;
			else if (p instanceof DP_Prefix_Solution_Follower)
				curr_p = ((DP_Prefix_Solution_Follower) p).prefix;
			while (curr_p != null)
			{
				if (curr_p == pref) return true;
				curr_p = curr_p.get_shorter();
			}
		}
		return false;
	}
	*/

	/*
	private boolean suffix_is_last(DP_Suffix_Solution suff)
	{
		DP_Suffix_Solution curr_s = suff;
		while (curr_s != null)
		{
			if (!get_successors(curr_s.first_decision).isEmpty()) return false;
			curr_s = curr_s.get_shorter();
		}
		return true;
	}
	*/

	private boolean list_contains_reference(List<DP_Suffix_Solution> suf_list, DP_Suffix_Solution ref)
	{
		for (DP_Suffix_Solution suf : suf_list)
		{
			if (suf == ref) return true;
		}
		return false;
	}

	private static void timing_experiment_few()
	{
		int times_to_repeat = 75;
		int n = 75000;
		int l = 4;
		int dom = 7500;

		List<Double> times = new ArrayList<Double>();
		for (int i = 0; i < times_to_repeat; i++)
		{
			Database_Query_Generator gen = new BinaryRandomPattern(n, l, dom, "path");
			gen.create();
			List<Relation> db = gen.get_database();
			Path_Equijoin_Query q = new Path_Equijoin_Query(db);
			q.set_join_conditions(new int[]{1}, new int[]{0});
			DP_Problem_Instance inst = new DP_Path_Equijoin_Instance(q);
			inst.bottom_up();

			// Initialize time
			long startTime = System.nanoTime();

			DP_Anyk_Iterator iter = new DP_QuickPlus(inst, null);
			DP_Solution solution;
			for (int j = 0; j < n; j++)
			{
				solution = iter.get_next();
				if (solution == null) break;
			}  

			double elapsedTime = (double) (System.nanoTime() - startTime) / 1_000_000_000.0;
			times.add(elapsedTime);
		}
		
		double average_time = times.stream()
								.mapToDouble(a -> a)
								.average()
								.getAsDouble();

		Collections.sort(times);
		double median_time = times.get(times.size() / 2);

		System.out.println("Median = " + median_time + " sec, Average = " + average_time + " sec");
	}

	private static void timing_experiment_all()
	{
		int times_to_repeat = 50;
		int n = 1000;
		int l = 4;
		int dom = 100;

		List<Double> times = new ArrayList<Double>();
		for (int i = 0; i < times_to_repeat; i++)
		{
			Database_Query_Generator gen = new BinaryRandomPattern(n, l, dom, "path");
			gen.create();
			List<Relation> db = gen.get_database();
			Path_Equijoin_Query q = new Path_Equijoin_Query(db);
			q.set_join_conditions(new int[]{1}, new int[]{0});
			DP_Problem_Instance inst = new DP_Path_Equijoin_Instance(q);
			inst.bottom_up();

			// Initialize time
			long startTime = System.nanoTime();

			DP_Anyk_Iterator iter = new DP_QuickPlus(inst, null);
			DP_Solution solution;
			while (true)
			{
				solution = iter.get_next();
				if (solution == null) break;
			}  

			double elapsedTime = (double) (System.nanoTime() - startTime) / 1_000_000_000.0;
			times.add(elapsedTime);
		}
		
		double average_time = times.stream()
								.mapToDouble(a -> a)
								.average()
								.getAsDouble();

		Collections.sort(times);
		double median_time = times.get(times.size() / 2);

		System.out.println("Median = " + median_time + " sec, Average = " + average_time + " sec");
	}

	public static void main(String args[]) 
    {
        // Run the example
        Path_Equijoin_Query example_query = new Path_Equijoin_Query(2);
        DP_Path_Equijoin_Instance instance = new DP_Path_Equijoin_Instance(example_query);
        instance.bottom_up();
        // Print the cost of the optimal solution
        //System.out.println("Optimal cost = " + instance.starting_node.get_opt_cost());


		DP_Anyk_Iterator iter = new DP_QuickPlus(instance, null);
		DP_Solution solution;
		int k;
		for (k = 1; k <= 10000; k++)
		{
			solution = iter.get_next();
			if (solution == null) break;
			System.out.println(solution);
		}  
		System.out.println("Number of results = " + (k - 1));

		// timing_experiment_few();
		// timing_experiment_all();
	}
}