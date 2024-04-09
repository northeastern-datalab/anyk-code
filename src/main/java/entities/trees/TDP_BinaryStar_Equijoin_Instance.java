package entities.trees;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;

import algorithms.trees.TDP_Anyk_Iterator;
import algorithms.trees.TDP_Eager;
import algorithms.trees.Tree_BatchSorting;
import data.BinaryRandomPattern;
import data.Database_Query_Generator;
import entities.Relation;
import entities.Tuple;

/** 
 * A class for Τ-DP problems that are equi-join star queries with *binary relations*:
 * A star query of size l involves one parent relation R1 and l-1 other relations hanging off of it.
 * All the children relation of R1 join with A1 which is the first attribute of R1.
 * Particularly useful for identifying star patterns in graphs.
 * The goal of T-DP is to find the minimum total weight of a query result.
 * Inside this class it is safe to downcast {@link entities.trees.TDP_State_Node#state_info} to {@link entities.Tuple}
 * (except the starting_state).
 * @author Nikolaos Tziavelis
*/
public class TDP_BinaryStar_Equijoin_Instance extends TDP_Problem_Instance
{
    /** 
     * The query that creates the T-DP problem.
    */
	Star_Equijoin_Query star_query;

    /** 
    * The bottom-up phase of T-DP creates a T-DP state/node for each tuple. 
    * Every relation maps to a stage of T-DP.
    * The starting node is artificial and the terminal nodes are implicit
    * (To avoid the unnecessary overhead, we omit the terminal nodes).
    * The cost of transitioning to a tuple is the cost of that tuple.
    */
    public TDP_BinaryStar_Equijoin_Instance(Star_Equijoin_Query query)
    {
        super();
        this.star_query = query;

        TDP_State_Node new_node, node_same_key;
        Tuple child_tuple;
        List<TDP_State_Node> new_stage, prev_stage, joining_nodes;
        Relation relation;
        int relation_index, branch;
        double cost, join_value_parent;
        HashMap<Double, List<TDP_State_Node>> child_hash;

        List<List<TDP_State_Node>> stages = new ArrayList<List<TDP_State_Node>>();

        // The number of stages is equal to the size of the star query
        // plus one starting stage (indexed by 0)
        int l = star_query.size;

        // To do the bottom-up traversal, just go from Rl to R1
        // We assume that the order of the indexes agrees with the tree order!!
        // Finally construct a starting stage R0 which contains only the starting node 

        // For star queries, we just need to go though R2 to Rl treating them as leaf nodes
        // and then handle only R1 differently
        for (int sg = 2; sg <= l; sg++)
        {
            relation_index = sg - 1; // The indexing of star_query.relations starts with 0 
            relation = star_query.relations.get(relation_index);
            new_stage = new ArrayList<TDP_State_Node>(relation.tuples.size());

            // Just create state nodes for their tuples
            for (Tuple t : relation.tuples)
            {
                new_node = new TDP_State_Node(0, t);
                // Set their cost equal to 0 (their cost is accounted for by the decision that leads to them)
                new_node.set_to_terminal();
                // No decisions to add
                new_stage.add(new_node);
            } 
            stages.add(new_stage);
            // These relations are leaf nodes, hence they have no children (empty list)
            this.add_parent_stage_to_tree(sg, new ArrayList<Integer>());
        }

        // Build one hash table for each child relation
        List<HashMap<Double, List<TDP_State_Node>>> children_hashes = 
            new ArrayList<HashMap<Double, List<TDP_State_Node>>>();
        for (List<TDP_State_Node> stage : stages) children_hashes.add(hash_stage(stage, 0));

        // Now create the stage of R1 which is the parent of R2-Rl
        int sg = 1;
        relation_index = 0;
        relation = star_query.relations.get(relation_index);
        new_stage = new ArrayList<TDP_State_Node>(relation.tuples.size());

        // We also need to hash the nodes of R1, once for each join
        HashMap<Double, TDP_State_Node> parent_hash = new HashMap<Double, TDP_State_Node>();
        // Go through the tuples of R1 and join each one with the children using the hashes
        for (Tuple t : relation.tuples)
        {
            new_node = new TDP_State_Node(l - 1, t);

            // All the relations join with the first attribute of R1
            join_value_parent = t.values[0];
            node_same_key = parent_hash.get(join_value_parent);
            //System.out.println("Considering " + new_node + " , branch " + branch);
            // Every node of R1 is added to the hash table with its join attribute value as a key
            // only if it is the first one with that key
            // If it is not the first then we make it share the same decisions as the first one
            if (node_same_key == null)
            {
                // Key not present in hash table
                // This is the first tuple (node) with these join attribute value
                parent_hash.put(join_value_parent, new_node); // Insert into the hash table

                // We have l-1 branches, go through each one separately
                for (int j = 2; j <= l; j++)
                {
                    // The corresponding index in the hash tables
                    branch = j - 2;

                    // Lookup the nodes (tuples) that join on the child stage (relation)
                    child_hash = children_hashes.get(branch);
                    joining_nodes = child_hash.get(join_value_parent);
                    //System.out.println("Joins with " + joining_nodes);
                    if (joining_nodes != null)
                    {
                        // If we can't find those values in the child hash table, then the tuple will be thrown away
                        // Else add an edge for each matching tuple
                        for (TDP_State_Node join_node : joining_nodes)
                        {
                            // For each one of them add a decision to the new node
                            // Only if the child node can reach all the terminal states
                            // If it cant, it is redundant and can be removed from the graph
                            // (we do not remove it, just leave it unconnected)
                            if (!join_node.is_dead_end())
                            {
                                //System.out.println("Adding a decision from " + new_node + " to " + join_node);
                                cost = ((Tuple) join_node.state_info).cost;
                                new_node.add_decision(branch, join_node, cost); 
                            }
                               
                        }                        
                    }
                }
            }
            else
            {
                //System.out.println("R1's hash table contains " + node_same_key + " with the same join value");
                // We have already visited a node of the child with this join attribute value
                // Just make the new one share the same decisions
                for (int j = 2; j <= l; j++)
                {
                    branch = j - 2;
                    new_node.share_decisions(node_same_key, branch);
                }     
            }            
            // After we are done with all branches, add the state to the stage only if 
            // it can reach all the terminal states
            if (!new_node.is_dead_end()) new_stage.add(new_node);
            //System.out.println("Node " + new_node + " of R1 has min ach cost = " + new_node.get_subtree_opt_cost());
        }
        stages.add(new_stage);
        List<Integer> children_of_R1 = new ArrayList<Integer>();
        for (int j = 2; j <= l; j++) children_of_R1.add(j);
        this.add_parent_stage_to_tree(sg, children_of_R1);

        // Finally, instantiate the starting node and connect it to all the states of stage 1 that can reach the leaves
        // The starting node has no local information (null)
        starting_node = new TDP_State_Node(1, null);
        prev_stage = new_stage;
        for (TDP_State_Node child_node : prev_stage)
        {
            //System.out.println("Adding " + child_node + " to starting node");
            child_tuple = ((Tuple) child_node.state_info); // cast so that we can lookup the cost
            starting_node.add_decision(0, child_node, child_tuple.cost);
        }
        // R0 has only R1 as a child in the tree
        List<Integer> children_of_R0 = new ArrayList<Integer>();
        children_of_R0.add(1);
        this.add_parent_stage_to_tree(0, children_of_R0);
    }

    // TODO: This is duplicated, find a way to reuse it.    
    /** 
     * Hashes the nodes of a particular stage of T-DP.
     * The key is one of the attributes of the associated tuples that will be used for the join.
     * @param stage The stage to be hashed.
     * @param join_attribute The index of the attribute that will be used as the key.
     * @return HashMap<Double, List<TDP_State_Node>>
     */
    private HashMap<Double, List<TDP_State_Node>> hash_stage(List<TDP_State_Node> stage, int join_attribute)
    {
        Tuple tup;
        double join_value;
        List<TDP_State_Node> node_list_same_key;

        // Each entry in the hashtable is a list of nodes
        // whose associated tuples share the same join attribute values
        HashMap<Double, List<TDP_State_Node>> hash = 
            new HashMap<Double, List<TDP_State_Node>>();
        for (TDP_State_Node node : stage)
        {
            tup = (Tuple) node.state_info; // get the tuple associated with the TDP state
            join_value = tup.values[join_attribute];
            node_list_same_key = hash.get(join_value);
            if (node_list_same_key == null)
            {
                // Key not present in hash table
                // Initialize a list with the current node and add it to the hash table
                node_list_same_key = new ArrayList<TDP_State_Node>();
                node_list_same_key.add(node);
                hash.put(join_value, node_list_same_key);
            }
            else
            {
                // We have already hashed a node associated with a tuple with the same join attribute value
                // Just add the new node to the list
                node_list_same_key.add(node);
            }
        } 
        return hash;
    }
    
    public static void main(String args[]) 
    {
        // Database_Query_Generator gen = new BinaryRandomPattern(3, 4, 3, "star");
        // gen.create();
        // gen.print_database();
        // List<Relation> db = gen.get_database();
        // Star_Equijoin_Query query = new Star_Equijoin_Query(db);

        // TDP_BinaryStar_Equijoin_Instance instance = new TDP_BinaryStar_Equijoin_Instance(query);
        // instance.bottom_up();
        // // Print the whole graph
        // instance.print_edges();

        // // Print the optimal solution
        // System.out.println("Optimal cost: " + instance.starting_node.get_subtree_opt_cost());
        // if (instance.starting_node.get_subtree_opt_cost() != Double.POSITIVE_INFINITY)
        // {
        //     System.out.print("Optimal solution:");
        //     TDP_State_Node curr;
        //     Queue<TDP_State_Node> q = new ArrayDeque<TDP_State_Node>();
        //     q.offer(instance.starting_node);
        //     while (!q.isEmpty())
        //     {
        //         curr = q.poll();
        //         System.out.print(" " + curr);
        //         for (TDP_State_Node child : curr.get_best_children()) q.offer(child);
        //     }
        //     System.out.println();
        // }


        Database_Query_Generator gen = new BinaryRandomPattern(3, 4, 3, "star");
        gen.create();
        gen.print_database();
        List<Relation> db = gen.get_database();
        Star_Equijoin_Query query = new Star_Equijoin_Query(db);

        TDP_BinaryStar_Equijoin_Instance instance = new TDP_BinaryStar_Equijoin_Instance(query);
        instance.bottom_up();

        // Print the optimal solution
        System.out.println("Optimal cost: " + instance.starting_node.get_opt_cost());
        if (instance.starting_node.get_opt_cost() != Double.POSITIVE_INFINITY)
        {
            System.out.print("Optimal solution:");
            TDP_State_Node curr;
            Queue<TDP_State_Node> q = new ArrayDeque<TDP_State_Node>();
            q.offer(instance.starting_node);
            while (!q.isEmpty())
            {
                curr = q.poll();
                System.out.print(" " + curr);
                for (TDP_State_Node child : curr.get_best_children()) q.offer(child);
            }
            System.out.println();
        }

        System.out.println("Eager:");
        TDP_Anyk_Iterator iter = new TDP_Eager(instance, null);
        while (true)
        {
            TDP_Solution sol = iter.get_next();
            if (sol != null) System.out.println(sol);
            else break;
        }

        System.out.println("Batch:");
        iter = new Tree_BatchSorting(instance, null);
        while (true)
        {
            TDP_Solution sol = iter.get_next();
            if (sol != null) System.out.println(sol);
            else break;
        }
    }
}
