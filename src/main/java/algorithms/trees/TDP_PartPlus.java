package algorithms.trees;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Stack;

import algorithms.Configuration;
import algorithms.paths.DP_Anyk_Iterator;
import algorithms.paths.DP_QuickPlus;
import data.BinaryRandomPattern;
import data.Database_Query_Generator;
import entities.Join_Predicate;
import entities.Relation;
import entities.paths.DP_Path_Equijoin_Instance;
import entities.paths.DP_Problem_Instance;
import entities.paths.DP_Solution;
import entities.paths.Path_Equijoin_Query;
import entities.trees.TDP_Decision;
import entities.trees.TDP_DecisionSet;
import entities.trees.TDP_Prefix_Solution;
import entities.trees.TDP_Prefix_Solution_Follower;
import entities.trees.TDP_Problem_Instance;
import entities.trees.TDP_Solution;
import entities.trees.TDP_State_Node;
import entities.trees.TDP_Suffix_Solution;
import entities.trees.TDP_Thetajoin_Instance;
import entities.trees.Tree_ThetaJoin_Query;

/** 
 * Implementation of Anyk-PartMemoized for DP, a ranked enumeration algorithm that improves upon {@link algorithms.paths.DP_Part}
 * by adding certain memoization. 
 * In particular, we store the order that a prefix explores a subspace and then reuse that order for other prefixes too.
 * The data structures needed to for memoization are contained in {@link entities.paths.DP_SuffixMemoizer}. 
 * One such object is pointed to from each {@link entities.paths.DP_DecisionSet}.
 * @author Nikolaos Tziavelis
*/
public abstract class TDP_PartPlus extends TDP_Anyk_Iterator
{
	/** 
	 * Maintains all the solutions that are candidates for the next best solution.
	*/
	protected PriorityQueue<TDP_Solution> global_pq;
	/** 
	 * Keeps track of how many results have been returned.
	*/
	private long iteration_no;
	/** 
	 * Stores for every stage whether it is an independence point for the tree. This means that given a node of that stage,
	 * the prefixes and suffixes are independent. With a BFS ordering, this corresponds to the last stage of every level.
	*/
	List<Boolean> independence_points;

	/** 
	 * @param inst The DP problem to run any-k on.
	 * @param conf A configuration of execution parameters.
	*/		
	public TDP_PartPlus(TDP_Problem_Instance inst, Configuration conf)
    {
    	super(inst, conf);
		
		// Enforce a BFS ordering of the stages
		inst.enforce_bfs_ordering();

		// Find which stages are independence points for the tree
		independence_points = new ArrayList<Boolean>(this.stages_no);
		// The root stage is always an independence point
		independence_points.add(true);
		for (int sg = 1; sg < this.stages_no - 1; sg += 1)
        {
			if (instance.get_parent_stage(sg) != instance.get_parent_stage(sg + 1)) independence_points.add(true);
			else independence_points.add(false);
		}
		// The last stage is always an independence point
		independence_points.add(true);

    	// Initialize the global PQ with an empty prefix (that contains only the starting node)
		this.global_pq = new PriorityQueue<TDP_Solution>(new Comparator<TDP_Solution>() 
		{
			public int compare(TDP_Solution s1, TDP_Solution s2) 
			{
				return Double.compare(s1.get_final_cost(), s2.get_final_cost());
			}
		});

    	// The prefix we start with contains only the best decision to go from starting_node to stage 1
    	// That way, we guarantee that for top-2 we start taking successor solutions from stage 1
    	if (instance.starting_node.get_opt_cost() != Double.POSITIVE_INFINITY)	// Corner case: if no path can reach the terminal node, leave the pq empty
    	{
	    	TDP_Prefix_Solution starting_prefix = new TDP_Prefix_Solution(instance.starting_node.get_best_decision(0));
	    	this.global_pq.add(starting_prefix);    		
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
	public TDP_Solution get_next()
	{
		this.iteration_no += 1;
		if (iteration_no > 2) return get_next_helper();
		else if (iteration_no == 1)
		{
			if (instance.starting_node.get_opt_cost() != Double.POSITIVE_INFINITY)
			{
				TDP_Prefix_Solution res = new TDP_Prefix_Solution(instance.starting_node.get_best_decision(0));
				return expand(res);
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

    public TDP_Solution get_next_helper()
    {
		TDP_Prefix_Solution curr_prefix;
		TDP_Suffix_Solution curr_suffix = null;
		TDP_Solution res = null;

		// Pop the best solution from the global PQ
		TDP_Solution popped_solution = global_pq.poll();
		if (popped_solution instanceof TDP_Prefix_Solution) 
		{
			//System.out.println("Popped solution (not a follower): " + popped_solution);
			res = popped_solution;
			curr_prefix = ((TDP_Prefix_Solution) popped_solution);

			// Always try to take successor(s) at the length of the popped prefix
			for (TDP_Decision succ : get_successors(curr_prefix.get_latest_decision()))
				global_pq.add(curr_prefix.create_successor(succ));

			// Convert the prefix to a list so that we can look up the parent nodes in each step
			List<TDP_State_Node> node_list = curr_prefix.solutionToNodes_strict_order();
			
			// This loop expands the prefix until complete or until a top-1 suffix is found in the sorted lists
			while (curr_prefix.length < instance.stages_no - 1)
			{
				// Find the parent node and the current branch
				int next_stage = curr_prefix.length + 1;
				int parent_stage = instance.get_parent_stage(next_stage);
				// The starting state is not included so all stages are shifted by one
				TDP_State_Node parent_node = node_list.get(parent_stage - 1);
				int branch_idx = instance.get_branch_index(next_stage);
				TDP_DecisionSet cur_decisionset = parent_node.decisions.get(branch_idx);

				List<TDP_Suffix_Solution> sorted_suffixes = cur_decisionset.sorted_suffixes;
				if (sorted_suffixes != null)
				{
					// The sorted suffixes have been initialized with the top-1 suffix
					// Finish the optimal expansion by using that top-1 suffix
					TDP_Prefix_Solution_Follower expanded_solution = new TDP_Prefix_Solution_Follower(curr_prefix, sorted_suffixes.get(0), 0);
					// The non-leading prefix follows the leading prefix of its decision set
					follow_leading_prefix(expanded_solution, cur_decisionset);
					// Create the corresponding suffix by looking up the best stored suffix from that non-leading prefix
					// This way we create a shared structure between the suffixes
					curr_suffix = sorted_suffixes.get(0);

					res = expanded_solution;
					break;
				}
				else
				{
					// Expand optimally
					curr_prefix = new TDP_Prefix_Solution(curr_prefix, cur_decisionset.best_decision);
					node_list.add(curr_prefix.latest_decision.target);
					res = curr_prefix;
					// Take successor(s)
					for (TDP_Decision succ : get_successors(curr_prefix.get_latest_decision()))
						global_pq.add(curr_prefix.create_successor(succ));
				}
			}

			// Continue traversing the current solution backwards to record suffixes
			if (curr_suffix == null) curr_suffix = new TDP_Suffix_Solution(curr_prefix.latest_decision);
			else curr_suffix = new TDP_Suffix_Solution(curr_suffix, curr_prefix.latest_decision);
			TDP_DecisionSet curr_decisionset = curr_prefix.latest_decision.belongs_to();
			curr_prefix = curr_prefix.get_shorter();
			while (curr_prefix != null)
			{	
				// Only record suffixes for stages that are independence points
				if (this.independence_points.get(curr_prefix.length + 1))
				{
					// If it is the first time we visit, then the current prefix is the leading one
					// This is where we initialize the memoization structure
					if (curr_decisionset.sorted_suffixes == null)
					{
						//System.out.println("Stage " + curr_prefix.length + " : Recording " + curr_prefix + " as leading prefix");
						curr_decisionset.sorted_suffixes = new ArrayList<TDP_Suffix_Solution>();
						curr_decisionset.subscribers = new ArrayList<TDP_Prefix_Solution_Follower>();
					}
					
					// Record suffix in the sorted order (no need to do that for full suffixes)
					// As we traverse the solution backwards, create a suffix of the decisions so that we can store it in the sorted order
					record_suffixes(curr_prefix, curr_suffix, curr_decisionset);
				}	

				// Shorten the current solution by cutting off one decision
				curr_decisionset = curr_prefix.latest_decision.belongs_to();
				curr_suffix = new TDP_Suffix_Solution(curr_suffix, curr_prefix.latest_decision);
				curr_prefix = curr_prefix.get_shorter();
			}
		}
		else if (popped_solution instanceof TDP_Prefix_Solution_Follower) 
		{
			res = popped_solution;
			//System.out.println("Popped solution is a follower: " + popped_solution);
			TDP_Prefix_Solution_Follower follower = (TDP_Prefix_Solution_Follower) popped_solution;

			TDP_DecisionSet curr_decisionset = follower.suffix.first_decision.belongs_to();

			follow_leading_prefix(follower, curr_decisionset);

			// Record the suffixes of the popped solution in the sorted lists
			curr_suffix = new TDP_Suffix_Solution(follower.suffix, follower.prefix.latest_decision);
			curr_decisionset = follower.prefix.latest_decision.belongs_to();
			curr_prefix = follower.prefix.get_shorter();
			while (curr_prefix != null)
			{
				// Only record suffixes for stages that are independence points
				if (this.independence_points.get(curr_prefix.length + 1))
				{
					record_suffixes(curr_prefix, curr_suffix, curr_decisionset);
				}

				// Shorten the current prefix by cutting off one decision
				curr_decisionset = curr_prefix.latest_decision.belongs_to();
				curr_suffix = new TDP_Suffix_Solution(curr_suffix, curr_prefix.latest_decision);
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
	 * @param decs The decision set where the data structures related to the prefix are stored.
	 */
    private void follow_leading_prefix(TDP_Prefix_Solution_Follower follower, TDP_DecisionSet decs)
    {
		int next_rank = follower.rank_of_suffix + 1;
		if (decs.sorted_suffixes.size() > next_rank)
		{
			// Look up the next best suffix and create a new candidate with it
			//System.out.println("The next best suffix from " + follower.prefix + " is " + decs.sorted_suffixes.get(next_rank)));
			global_pq.add(new TDP_Prefix_Solution_Follower(follower.prefix, decs.sorted_suffixes.get(next_rank), next_rank));
		}
		else
		{
			// Subscribe
			//System.out.println("Prefix " + follower.prefix + " is subscribing for suffix with rank " + next_rank);
			decs.subscribers.add(new TDP_Prefix_Solution_Follower(follower.prefix, next_rank));
		}
    }

	/** 
	 * Records a suffix as the next best option in the sorted list of suffixes at the end of the given prefix.
	 * @param prefix The prefix that will store the suffix.
	 * @param suffix The suffix that will be stored.
	 * @param decs The decision set where the data structures related to the prefix are stored.
	 */
    private void record_suffixes(TDP_Prefix_Solution prefix, TDP_Suffix_Solution suffix, TDP_DecisionSet decs)
    {
		//System.out.println("Stage " + prefix.length + " : Adding " + suffix + " to sorted suffix list with rank " + decs.sorted_suffixes.size());
		assert !list_contains_reference(decs.sorted_suffixes, suffix) :
			"Duplicate suffix in sorted list";
		assert decs.sorted_suffixes.isEmpty() || 
			Double.compare(decs.sorted_suffixes.get(decs.sorted_suffixes.size() - 1).get_cost() - 0.0000001, suffix.get_cost()) <= 0 :
			"Wrong order in suffix list";
			decs.sorted_suffixes.add(suffix);
		// Publish this suffix to all subscribers
		for (TDP_Prefix_Solution_Follower subscriber : decs.subscribers)
		{
			//System.out.println("Subscriber " + subscriber.prefix + " receives rank-" + subscriber.rank_of_suffix + " suffix: " + suffix);
			assert subscriber.rank_of_suffix == decs.sorted_suffixes.size() - 1 : "Subscriber was waiting for suffix of different rank";
			subscriber.set_suffix(suffix);
			global_pq.add(subscriber);
		}
		// Clear the subscribers list
		decs.subscribers.clear();
    }

	/** 
	 * Expands the prefix solution into a full solution by following an optimal sequence of decisions.
	 * The full solution reaches a terminal node for each leaf stage.
	 * 
	 * @param pref A prefix solution to be expanded optimally.
	 * @return TDP_Prefix_Solution A new object that is a full solution.
	 */
    protected TDP_Prefix_Solution expand(TDP_Prefix_Solution pref)
    {
		int next_stage, parent_stage, branch_idx;
    	TDP_Prefix_Solution current;
		TDP_Decision next_best_decision;
		TDP_State_Node parent_node;
		current = pref;
		List<TDP_State_Node> node_list = pref.solutionToNodes_strict_order();
		// Recall that stage 0 contains the starting node which is not encoded in the solutions
    	while (current.length < instance.stages_no - 1)
    	{
			next_stage = current.length + 1;
			// Find the parent state for the next decision
			parent_stage = instance.get_parent_stage(next_stage);
			// The starting state is not included so all stages are shifted by one
			parent_node = node_list.get(parent_stage - 1);
			branch_idx = instance.get_branch_index(next_stage);
			// Follow the best decision from the parent node
    		next_best_decision = parent_node.get_best_decision(branch_idx);
			current = new TDP_Prefix_Solution(current, next_best_decision);
			// Add the latest node to the list
			node_list.add(next_best_decision.target);
    	}
    	// assert String.format("%.4f", current.future_cost).equals(String.format("%.4f", current.get_cost())); // Precision may be a problem here, hence the precision cutoff
		return current;
    }

	/** 
	 * Initialization of the data structures needed at each TDP_DesicionSet
	 * in order to compute a partial order among decisions.
	 * @param decisions The set of decisions whose data structures will be initialized.
	 */
    public abstract void initialize_partial_order(TDP_DecisionSet decisions);

	/** 
	 * Find the successors of a given decision among the other decisions in the {@link entities.trees.TDP_DecisionSet}.
	 * The way that successors are computed depends on the variant (subclass).
	 * @param dec The decision whose successors will be computed.
	 * @return TDP_Decision The successor decisions according to the (partial) order.
	 */
    public abstract List<TDP_Decision> get_successors(TDP_Decision dec);

	/** 
	 * Initializes the data structures needed for all nodes.
	 * Visits all the nodes in a DFS traversal.
	 */
    public void initialize_data_structures()
	{
		Stack<TDP_State_Node> dfs_stack = new Stack<TDP_State_Node>();
		dfs_stack.push(instance.starting_node);

		TDP_State_Node curr_node;
		while (!dfs_stack.isEmpty())
		{
			curr_node = dfs_stack.pop();
			for (int b = 0; b < curr_node.decisions.size(); b++)
			{
				initialize_partial_order(curr_node.decisions.get(b));
				for (TDP_Decision dec : curr_node.get_decisions(b))
				{
					if (!dec.target.decisions.get(b).partial_order_computed)
						dfs_stack.push(dec.target);
				}
			}
		}
	}

	private boolean list_contains_reference(List<TDP_Suffix_Solution> suf_list, TDP_Suffix_Solution ref)
	{
		for (TDP_Suffix_Solution suf : suf_list)
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
		int times_to_repeat = 10;
		int n = 50;
		int l = 6;
		int dom = 8;

		List<Double> times = new ArrayList<Double>();
		for (int i = 0; i < times_to_repeat; i++)
		{
			Database_Query_Generator gen = new BinaryRandomPattern(n, l, dom, "onebranch");
			gen.create();
			List<Relation> db = gen.get_database();

			Tree_ThetaJoin_Query q = new Tree_ThetaJoin_Query();

			// OneBranch Query
			// q.add_to_tree_wConjunction(db.get(0), 0, -1, null);
			// for (int j = 1; j < l - 1; j++) q.add_to_tree_wConjunction(db.get(j), j, j - 1, Arrays.asList(new Join_Predicate("E", 1, 0, null))); 
			// q.add_to_tree_wConjunction(db.get(l - 1), l - 1, l - 3, Arrays.asList(new Join_Predicate("E", 0, 0, null))); 

			// Path Query
			q.add_to_tree_wConjunction(db.get(0), 0, -1, null);
			for (int j = 1; j < l; j++) q.add_to_tree_wConjunction(db.get(j), j, j - 1, Arrays.asList(new Join_Predicate("E", 1, 0, null))); 

			TDP_Problem_Instance inst = new TDP_Thetajoin_Instance(q, null);
			inst.bottom_up();

			// Initialize time
			long startTime = System.nanoTime();

			TDP_Anyk_Iterator iter = new TDP_Recursive(inst, null);
			TDP_Solution solution;
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
        // Tree_ThetaJoin_Query example_query = new Tree_ThetaJoin_Query(2);
        // TDP_Thetajoin_Instance instance = new TDP_Thetajoin_Instance(example_query, null);
        // instance.bottom_up();
        // // Print the cost of the optimal solution
        // //System.out.println("Optimal cost = " + instance.starting_node.get_opt_cost());

		// TDP_Anyk_Iterator iter = new TDP_QuickPlus(instance, null);
		// TDP_Solution solution;
		// int k;
		// for (k = 1; k <= 10000; k++)
		// {
		// 	solution = iter.get_next();
		// 	if (solution == null) break;
		// 	System.out.println(solution + " " + solution.get_cost());
		// }  
		// System.out.println("Number of results = " + (k - 1));

		// Check the BFS ordering
		// Database_Query_Generator gen = new BinaryRandomPattern(4, 4, 3, "onebranch");
		// gen.create();
		// List<Relation> db = gen.get_database();
		// Tree_ThetaJoin_Query q = new Tree_ThetaJoin_Query();
		// q.add_to_tree_wConjunction(db.get(0), 0, -1, null);
		// for (int j = 1; j < 3; j++) q.add_to_tree_wConjunction(db.get(j), j, j - 1, Arrays.asList(new Join_Predicate("E", 1, 0, null))); 
		// q.add_to_tree_wConjunction(db.get(3), 3, 0, Arrays.asList(new Join_Predicate("E", 1, 0, null))); 
		
		// TDP_Thetajoin_Instance instance = new TDP_Thetajoin_Instance(q, null);
        // instance.bottom_up();
		// TDP_Anyk_Iterator iter = new TDP_QuickPlus(instance, null);
		// TDP_Solution solution;
		// int k;
		// for (k = 1; k <= 10000; k++)
		// {
		// 	solution = iter.get_next();
		// 	if (solution == null) break;
		// 	System.out.println(solution + " " + solution.get_cost());
		// }  


		// timing_experiment_few();
		timing_experiment_all();
	}
}