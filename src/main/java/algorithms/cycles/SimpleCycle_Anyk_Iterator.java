package algorithms.cycles;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.PriorityQueue;

import org.javatuples.Pair;

import algorithms.paths.DP_All;
import algorithms.paths.DP_Anyk_Iterator;
import algorithms.paths.DP_Eager;
import algorithms.paths.DP_Lazy;
import algorithms.paths.DP_Quick;
import algorithms.paths.DP_QuickPlus;
import algorithms.paths.DP_Recursive;
import algorithms.paths.DP_Take2;
import algorithms.paths.Path_Batch;
import algorithms.paths.Path_BatchSorting;
import data.Cycle_HeavyLightPattern;
import data.Database_Query_Generator;
import entities.Relation;
import entities.Tuple;
import entities.cycles.SimpleCycle_Equijoin_Query;
import entities.paths.DP_Path_Equijoin_Instance;
import entities.paths.DP_Problem_Instance;
import entities.paths.DP_Solution;
import entities.paths.Path_Equijoin_Query;
import entities.paths.Path_Query_Solution;

/** 
 * A ranked enumeration algorithm for a conjunctive query that is a simple cycle of binary relations 
 * specified by a {@link entities.cycles.SimpleCycle_Equijoin_Query} object.
 * It is implemented as an iterator that is first initialized and then the method {@link #get_next} 
 * returns the next best output Tuple in ranked order.
 * <br><br>
 * This class uses a decomposition for the cycle that is based on the submodular width.
 * Instead of the cyclic query, it solves a union of (larger) acylic queries
 * that produces the same output.
 * The input tuples are routed to the appropriate acyclic queries based on a 
 * threshold value that determines if they are "heavy" or "light".
 * <br><br>
 * CAUTION: Currently only works for even-length cycles!!
 * @author Nikolaos Tziavelis
*/
public class SimpleCycle_Anyk_Iterator
{
    /** 
     * The relations that form the cycle.
    */
    public List<Relation> input_relations;
    /** 
     * The length of the cycle.
    */
    public int cyc_length;
    /** 
     * The algorithm to be used for any-k enumeration on the acyclic (path) queries.
    */
    public String anyk_alg;
    /** 
     * Type of heap to be used by the Anyk-Part variants.
    */
    public String heap_type;
    /** 
     * A top-level priority queue that compares the solutions between the different acyclic queries of the decomposition.
    */
    private PriorityQueue<Pair<DP_Solution, DP_Anyk_Iterator>> pq;
    /** 
     * Keeps track of which iterator was used in the last call of {@link #get_next} 
     * so that we know how to refill the empty spot.
    */
    private DP_Anyk_Iterator last_iterator;
    /** 
     * The relation that output tuples use as a reference.
     * @see entities.Relation
    */
    private Relation output;
    /** 
     * The numerical value used by the decomposition to differentiate between "heavy" and "light".
    */
    private double threshold;

    /** 
     * Inserts a relation to the path (on the right).
     * @param query A simple-cycle conjunctive query.
     * @param algorithm An any-k algorithm for the acyclic cases.
     * @param heap_type A string that specifies the type of heap used by the Any-k Part variants.
     */
	public SimpleCycle_Anyk_Iterator(SimpleCycle_Equijoin_Query query, String algorithm, String heap_type)
    {
    	this.input_relations = query.relations;
    	this.cyc_length = query.length;
        this.anyk_alg = algorithm;
        this.heap_type = heap_type;
        // For now, we allow output tuples to have different schemas depending on the decomposition
        // they were pulled from. Ideally we would want to have one strategy per decomposition that
        // keeps each attribute value only once
        this.output = new Relation("Rout", null);

        List<Path_Equijoin_Query> union_acyclic = decompose_heavy_light();

        /*
        Path_Equijoin_Query qu;
        for (int i = 0; i < union_acyclic.size(); i++)
        {
            qu = union_acyclic.get(i);
            System.out.println("Decomposition " + i + " :");
            for (Relation r1 : qu.relations)
                System.out.println(r1);
        }
        */

        DP_Path_Equijoin_Instance instance;
        DP_Anyk_Iterator iter;
        DP_Solution first_solution;

        // Initialize the priority queue that will compare solutions of the different acyclic problems
        this.pq = new PriorityQueue<Pair<DP_Solution, DP_Anyk_Iterator>>(new Comparator<Pair<DP_Solution, DP_Anyk_Iterator>>()
        {
            public int compare(Pair<DP_Solution, DP_Anyk_Iterator> p1, Pair<DP_Solution, DP_Anyk_Iterator> p2) 
            {
                return p1.getValue0().compareAgainst(p2.getValue0());
            }
        });

        //Integer name = 1;

        for (Path_Equijoin_Query subquery : union_acyclic)
        {
            instance = new DP_Path_Equijoin_Instance(subquery);
            instance.bottom_up();
            iter = initialize_iterator(instance);
            //iter.set_name(name.toString());
            first_solution = iter.get_next();
            if (first_solution != null) pq.add(new Pair<DP_Solution, DP_Anyk_Iterator>(first_solution, iter));

            //if (first_solution != null) System.out.println("First solution of Iterator " + iter.get_name() + " is " + first_solution.solutionToString() + " with cost " + first_solution.get_cost());
            //name += 1;
        }
        this.last_iterator = null;
    }

    /** 
     * Computes the next output Tuple of {@link entities.cycles.SimpleCycle_Equijoin_Query} in ranked order. 
     * Ties are broken arbitrarily.
     * @return Tuple The next best tuple or null if there are no other tuple.
     */
    public Tuple get_next()
    {
        // First refill the spot left open from the previous call
        if (last_iterator != null)
        {
            // This is not the first call to this method, we need to refill a spot in the priority queue
            DP_Solution next_sol = last_iterator.get_next();
            if (next_sol != null) pq.add(new Pair<DP_Solution, DP_Anyk_Iterator>(next_sol, last_iterator));            
        }
        // Now pick the best one from the pq and return it
        if (!pq.isEmpty())
        {
            Pair<DP_Solution, DP_Anyk_Iterator> best_pair = pq.poll();
            List<Tuple> tuple_list = best_pair.getValue0().solutionToTuples();
            Tuple tup = new Tuple(tuple_list, output);

            // System.out.println("Result about to be pulled from " + best_pair.getValue1().get_name());

            // Store the iterator so that the spot is refilled in the next call
            last_iterator = best_pair.getValue1();

            // System.out.println("Pulling solution from iterator " + last_iterator.get_name());

            return tup;
        }
        else return null;
    }

    /** 
     * Decomposes the cyclic query into a union of acyclic queries.
     * The decomposition is done based on a threshold value such that the produced queries have 
     * (input) size determined by the submodular width of the query. 
     * For simple cycles, the produced queries are always paths.
     * @return List<Path_Equijoin_Query> A list of acyclic queries as the result of the decomposition.
     */
    private List<Path_Equijoin_Query> decompose_heavy_light()
    {
        double key;
        Relation heavy_r, light_r;
        Hashtable<Double, List<Tuple>> hash;
        List<Tuple> tup_list_same_key;

        // First determine what constitutes a "heavy value"
        // Here we assume that all relations have the same size
        double max_n = 0.0;
        for (Relation r : input_relations)
            if (r.tuples.size() > max_n)
                max_n = r.tuples.size();
        this.threshold = Math.pow(max_n, 1.0 / (cyc_length / 2));

        // For each relation, create two new ones: a heavy and a light
        List<Relation> heavy_relations = new ArrayList<Relation>();
        List<Relation> light_relations = new ArrayList<Relation>();
        for (Relation r : input_relations)
        {
            heavy_r = new Relation(r.relation_id + "_H", r.schema);
            light_r = new Relation(r.relation_id + "_L", r.schema);

            // Hash the tuples according to the first attribute value
            hash = new Hashtable<Double, List<Tuple>>();
            for (Tuple t : r.tuples)
            {
                key = t.values[0];
                if ((tup_list_same_key = hash.get(key)) != null)
                {
                    // Key already exist, append tuple to list of tuples that share the same key
                    tup_list_same_key.add(t);
                }
                else
                {
                    tup_list_same_key = new ArrayList<Tuple>();
                    tup_list_same_key.add(t);
                    hash.put(key, tup_list_same_key);
                }
            }
            // Go through the hash buckets and if the tuples in a bucket are heavy (their number is above the threshold)
            // put them in the heavy relation
            // else put them in the light relation
            for (List<Tuple> tuples_with_same_key : hash.values())
            {
                if (tuples_with_same_key.size() >= threshold)
                    heavy_r.insertAll(tuples_with_same_key);
                else 
                    light_r.insertAll(tuples_with_same_key);
            }
            heavy_relations.add(heavy_r);
            light_relations.add(light_r);

        }
        
        // System.out.println("=============== HEAVY RELATIONS ============");
        // System.out.println(heavy_relations);
        // System.out.println("=============== LIGHT RELATIONS ============");
        // System.out.println(light_relations);

        // Now create cyc_length + 1 join plans that are a partition of the initial one
        List<Path_Equijoin_Query> union_acyclic = new ArrayList<Path_Equijoin_Query>();
        List<Relation> plan;
        Path_Equijoin_Query acyc_q;
        // Create the heavy plans
        for (int i = 0; i < cyc_length; i++)
        {
            plan = new ArrayList<Relation>();
            plan.add(heavy_relations.get(i));
            for (int j = i + 1; j < cyc_length; j++)
                plan.add(input_relations.get(j));
            for (int j = 0; j < i; j++)
                plan.add(light_relations.get(j));

            // System.out.println("---------- Plan " + i + " ============");
            // System.out.println(plan);

            acyc_q = carry_through_join(plan);
            union_acyclic.add(acyc_q);
        }
        // Create the light plan
        plan = new ArrayList<Relation>();
        for (int j = 0; j < cyc_length; j++)
            plan.add(light_relations.get(j));

        // System.out.println("---------- Plan " + cyc_length + " ============");
        // System.out.println(plan);

        acyc_q = split_join(plan);
        union_acyclic.add(acyc_q);

        return union_acyclic;
    }

    /** 
     * Instantiates a particular tree decomposition of the cycle as a new query.
     * This tree decomposition "breaks" the cycle at some attribute, unfolding it into a path.
     * The "breakpoint" attribute is then carried through to all relations.
     * The method assumes that the first attribute of the first relation is the breakpoint.
     * Used by the submodular-width decompositions for the "heavy attributes" 
     * (which will be the breakpoint).
     * @param relations A list of relations that will be decomposed by this method.
     * @return Path_Equijoin_Query A tree decomposition as a new query.
     */
    private Path_Equijoin_Query carry_through_join(List<Relation> relations)
    {
        int num_relations = relations.size();
        // The first attribute of the first relation is the breakpoint
        // First, gather all the breakpoint values
        String breakpoint_attribute = relations.get(0).schema[0];
        HashSet<Double> breakpoint_vals = new HashSet<Double>();
        for (Tuple t : relations.get(0).tuples)
            breakpoint_vals.add(t.values[0]);

        /*      Materialize first bag       */
        Relation r = new Relation("Rstart", new String[]{breakpoint_attribute, relations.get(1).schema[0], relations.get(1).schema[1]});
        // First hash R0 on both attributes so that we can look up if a tuple exists in there
        // Also keep the cost of the tuples you hash so that we can use it when we look them up
        //!!!!! In order to support bags, the hashtable gives us a list of costs!
        Hashtable<List<Double>, List<Double>> hash = new Hashtable<List<Double>, List<Double>>();
        List<Double> key_vals;
        List<Double> cost_list_same_key;
        for (Tuple t : relations.get(0).tuples)
        {
            key_vals = new ArrayList<Double>();
            key_vals.add(t.values[0]);
            key_vals.add(t.values[1]);
            if ((cost_list_same_key = hash.get(key_vals)) != null)
            {
                // Key already exist, append cost to list of costs that share the same key
                cost_list_same_key.add(t.cost);
            }
            else
            {
                cost_list_same_key = new ArrayList<Double>();
                cost_list_same_key.add(t.cost);
                hash.put(key_vals, cost_list_same_key);
            }
        }

        Tuple new_tuple;
        for (Tuple t : relations.get(1).tuples)
        {
            for (double breakpoint_val : breakpoint_vals)
            {
                key_vals = new ArrayList<Double>();
                key_vals.add(breakpoint_val);
                key_vals.add(t.values[0]);
                if ((cost_list_same_key = hash.get(key_vals)) != null)
                {
                    for (Double cost_from_first : cost_list_same_key)
                    {
                        // Materialize tuple
                        new_tuple = new Tuple(new double[]{breakpoint_val, t.values[0], t.values[1]}, cost_from_first + t.cost, r);
                        r.insert(new_tuple);                        
                    }
                }
            }
        }
        // Initialize the acyclic plan with our relation
        Path_Equijoin_Query acyclic_query = new Path_Equijoin_Query(r);


        /*      Materialize the bags in the middle of the path      */
        // They are a cartesian product between the corresponding relations and the breakpoint values
        for (int i = 2; i <= num_relations - 3; i++)
        {
            r = new Relation("R" + (i - 1), new String[]{breakpoint_attribute, relations.get(i).schema[0], relations.get(i).schema[1]});
            for (Tuple t : relations.get(i).tuples)
            {
                for (double breakpoint_val : breakpoint_vals)    
                {
                    // Materialize tuple
                    new_tuple = new Tuple(new double[]{breakpoint_val, t.values[0], t.values[1]}, t.cost, r);
                    r.insert(new_tuple);                
                }    
            }
            acyclic_query.insert(r);
        }

        /*      Materialize last bag      */
        r = new Relation("Rend", new String[]{breakpoint_attribute, relations.get(num_relations - 2).schema[0], relations.get(num_relations - 2).schema[1]});
        // First hash R(l-1) on both attributes so that we can look up if a tuple exists in there
        // Also keep the cost of the tuples you hash so that we can use it when we look them up
        hash = new Hashtable<List<Double>, List<Double>>();
        for (Tuple t : relations.get(num_relations - 1).tuples)
        {
            key_vals = new ArrayList<Double>();
            key_vals.add(t.values[0]);
            key_vals.add(t.values[1]);
            if ((cost_list_same_key = hash.get(key_vals)) != null)
            {
                // Key already exist, append cost to list of costs that share the same key
                cost_list_same_key.add(t.cost);
            }
            else
            {
                cost_list_same_key = new ArrayList<Double>();
                cost_list_same_key.add(t.cost);
                hash.put(key_vals, cost_list_same_key);
            }
        }

        for (Tuple t : relations.get(num_relations - 2).tuples)
        {
            for (double breakpoint_val : breakpoint_vals)
            {
                key_vals = new ArrayList<Double>();
                key_vals.add(t.values[1]);
                key_vals.add(breakpoint_val);
                if ((cost_list_same_key = hash.get(key_vals)) != null)
                {
                    for (Double cost_from_last : cost_list_same_key)
                    {
                        // Materialize tuple
                        new_tuple = new Tuple(new double[]{breakpoint_val, t.values[0], t.values[1]}, cost_from_last + t.cost, r);
                        r.insert(new_tuple);
                    }
                }
            }
        }
        acyclic_query.insert(r);

        acyclic_query.set_join_conditions(new int[]{0, 2}, new int[]{0, 1});
        return acyclic_query;
    }

    
    /** 
     * Instantiates a particular tree decomposition of the cycle as a new query.
     * This tree decomposition "splits" the cycle into two and creates two bags.
     * Used by the submodular-width decompositions when all the attributes are "light".
     * @param relations A list of relations that will be decomposed by this method.
     * @return Path_Equijoin_Query A tree decomposition as a new query.
     */
    private Path_Equijoin_Query split_join(List<Relation> relations)
    {
        int length = relations.size();

        List<Relation> first_half = relations.subList(0, length / 2);
        Relation first_bag = simple_join(first_half, "FirstHalf");

        List<Relation> second_half = relations.subList(length / 2, length);
        Relation second_bag = simple_join(second_half, "SecondHalf");    

        Path_Equijoin_Query acyclic_query = new Path_Equijoin_Query(first_bag); 
        acyclic_query.insert(second_bag);
        acyclic_query.set_join_conditions(new int[]{0, length / 2}, new int[]{length / 2, 0});
        return acyclic_query;
    }

    /** 
     * Performs the join of binary relations in a path.
     * Called by {@link #split_join}.
     * @param relations The input relations that will be joined.
     * @param outName The name of the output relation.
     * @return Relation The output relation of the join.
     */
    private Relation simple_join(List<Relation> relations, String outName)
    {
        int num_relations = relations.size();
        // Initialize the resulting relation
        String[] res_schema = new String[num_relations + 1];
        res_schema[0] = relations.get(0).schema[0];
        // res_schema[0] = "AllLight";
        for (int i = 0; i < num_relations; i++) res_schema[i + 1] = relations.get(i).schema[1];
        Relation res = new Relation(outName, res_schema);

        // Use the DP bottom-up phase for the join, then the Batch algorithm
        // and transform the DP solutions to Tuples
        Path_Equijoin_Query query = new Path_Equijoin_Query(relations);
        query.set_join_conditions(new int[]{1}, new int[]{0});
        DP_Path_Equijoin_Instance dp_instance = new DP_Path_Equijoin_Instance(query);

        dp_instance.bottom_up();
        DP_Anyk_Iterator batch = new Path_Batch(dp_instance, null);

        Path_Query_Solution solution;
        Tuple new_tuple;
        List<Tuple> tup_list;
        double tup_vals[];
        while ((solution = (Path_Query_Solution) batch.get_next()) != null)
        {
            // Transform the DP solution to a single tuple that contains the attribute values once
            // and in the correct order
            tup_vals = new double[num_relations + 1];
            tup_list = solution.solutionToTuples();
            for (int i = 0; i < num_relations; i++)
            {
                tup_vals[i] = tup_list.get(i).values[0];
            }
            tup_vals[num_relations] = tup_list.get(num_relations - 1).values[1];
            new_tuple = new Tuple(tup_vals, solution.get_cost(), res);
            res.insert(new_tuple);
        }
        return res;
    }    

    
    /** 
     * @return DP_Anyk_Iterator
     */
    // Given an array, creates a sublist that contains only the elements located at the specified indices
    /*
    private ArrayList<Integer> createSublist(int[] oldArray, int[] indices)
    {
        ArrayList<Integer> newList = new ArrayList<Integer>();
        for(int i = 0; i < indices.length; i++)
             newList.add(oldArray[indices[i]]); //Adds specified indices to new list
        return newList;
    }
    */

    private DP_Anyk_Iterator initialize_iterator(DP_Problem_Instance instance)
    {
        DP_Anyk_Iterator iter = null;
        if (anyk_alg.equals("Eager")) iter = new DP_Eager(instance, null);
        else if (anyk_alg.equals("All")) iter = new DP_All(instance, null);
        else if (anyk_alg.equals("Take2")) iter = new DP_Take2(instance, null);
        else if (anyk_alg.equals("Lazy")) iter = new DP_Lazy(instance, null);
        else if (anyk_alg.equals("Quick")) iter = new DP_Quick(instance, null);
        else if (anyk_alg.equals("Recursive")) iter = new DP_Recursive(instance, null);
        else if (anyk_alg.equals("QuickMemoized")) iter = new DP_QuickPlus(instance, null);
        else if (anyk_alg.equals("BatchSorting")) iter = new Path_BatchSorting(instance, null);
        else
        {
            System.err.println("Any-k algorithm not recognized.");
            System.exit(1);
        }
        return iter;
    }

    public String get_alg()
    {
        return this.anyk_alg;
    }

    // Only for debugging puproses
    /*
    private boolean isLight(Relation r)
    {
        int val, cnt;
        for (Tuple t : r.tuples)
        {
            val = t.values[0];
            cnt = 0;
            for (Tuple t1 : r.tuples)
            {
                if (t1.values[0] == val)
                    cnt++;
            }
            if (cnt >= this.threshold) return false;
        }
        return true;
    }
    */

    public static void main(String args[]) 
    {
        SimpleCycle_Equijoin_Query query = new SimpleCycle_Equijoin_Query();
        SimpleCycle_Anyk_Iterator iter = new SimpleCycle_Anyk_Iterator(query, "REA", "Lib_BHeap_Bulk");

        Tuple res;
        while ((res = iter.get_next()) != null)
            System.out.println("Cost= " + res.cost + "  " + res);

        System.out.println("==============================");

        Database_Query_Generator gen = new Cycle_HeavyLightPattern(100, 4);
        gen.create();
        gen.print_database();
        List<Relation> database = gen.get_database();
        query = new SimpleCycle_Equijoin_Query(database);
        
        iter = new SimpleCycle_Anyk_Iterator(query, "REA", "Lib_BHeap_Bulk");

        while ((res = iter.get_next()) != null)
            System.out.println("Cost= " + res.cost + "  " + res);

    }
}