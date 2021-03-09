package entities.paths;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import entities.Relation;
import entities.Tuple;
import util.Common;

/** 
 * A class for DP problems that are equi-join path queries.
 * The goal of DP is to find the minimum total weight of a query result.
 * Inside this class it is safe to downcast {@link entities.paths.DP_State_Node#state_info} to {@link entities.Tuple}
 * (except the starting_state).
 * @author Nikolaos Tziavelis
*/
public class DP_Path_Equijoin_Instance extends DP_Problem_Instance
{
    /** 
     * The query that creates the DP problem.
    */
	Path_Equijoin_Query path_query;

    /** 
     * For each tuple, we create a DP state. 
     * Every relation maps to a stage of DP.
     * The starting node is artificial and the terminal node is implicit.
     * The cost of transitioning to a tuple is the cost of that tuple.
    */
    public DP_Path_Equijoin_Instance(Path_Equijoin_Query query)
    {
        super();
        this.path_query = query;

        DP_State_Node new_node, node_same_key;
        Tuple right_tuple;
        List<DP_State_Node> joining_nodes;
        ArrayList<DP_State_Node> new_stage, prev_stage;
        Relation relation;
        int[] join_attributes_right, join_attributes_left;
        ArrayList<Double> join_values_left;
        int relation_index;
        double cost;

        // The number of stages for an equi-join will be equal to the length of the path query
        int l = path_query.length;

        // The tuples of the last relation in the path correspond 
        // to states that all reach the terminal node with zero cost
        relation = path_query.relations.get(l - 1);
        new_stage = new ArrayList<DP_State_Node>(relation.tuples.size());
        for (Tuple t : relation.tuples)
        {
            new_node = new DP_State_Node(t);
            new_node.set_to_terminal();
            new_stage.add(new_node);
        } 

        // For all the other relations, insert a stage to the left
        // Discover the edges between the new stage and the previous stage
        // by hashing the new tuples and the tuples contained in the previous stage
        for (int sg = l - 1; sg >= 1; sg--)
        {
            relation_index = sg - 1;    // because indexing of path_query.relations starts with 0 

            relation = path_query.relations.get(relation_index);
            prev_stage = new_stage;
            new_stage = new ArrayList<DP_State_Node>(relation.tuples.size());

            // First, hash the previous stage so that its nodes
            // are grouped by join attribute values
            // join_conditions contains pairs - the indexes of the right relation are the second value
            join_attributes_right = path_query.join_conditions.get(relation_index).getValue1();
            // Each entry in the hashtable is a list of node
            // whose associated tuples share the same join attribute values
            HashMap<List<Double>, List<DP_State_Node>> right_hash = Common.hash_stage(prev_stage, join_attributes_right);

            // Now hash the nodes in the new (left) stage
            // join_conditions contains pairs - the indexes of the left relation are the first value
            join_attributes_left = path_query.join_conditions.get(relation_index).getValue0();
            // Every tuple (node) in the left relation is added to the hash table with its join attribute values as a key
            // only if it is the first one with that key
            // If it is not the first then we make it share the same decisions as the first one
            HashMap<ArrayList<Double>, DP_State_Node> left_hash = 
                new HashMap<ArrayList<Double>, DP_State_Node>();
            for (Tuple t : relation.tuples)
            {
                new_node = new DP_State_Node(t);
                join_values_left = Common.createSublist(t.values, join_attributes_left);
                node_same_key = left_hash.get(join_values_left);
                if (node_same_key == null)
                {
                    // Key not present in hash table
                    // This is the first tuple (node) with these join attribute values
                    left_hash.put(join_values_left, new_node); // Insert into the hash table
                    // Lookup the nodes (tuples) that join on the right stage (relation)
                    joining_nodes = right_hash.get(join_values_left);
                    if (joining_nodes != null)
                    {
                        // If we can't find those values in the right hash table, throw away the tuple
                        // Else add an edge for each matching tuple
                        for (DP_State_Node join_node : joining_nodes)
                        {
                            // For each one of them add a decision to the new node
                            cost = ((Tuple) join_node.state_info).cost;
                            new_node.add_decision(join_node, cost);                                
                        }                        
                    }
                }
                else
                {
                    // We have already visited a node on the left with these join attribute values
                    // Just make the new one share the same decisions
                    new_node.share_decisions(node_same_key);                 
                }
                // Keep the node only if it can reach the terminal node
                if (new_node.get_number_of_children() > 0)
                    new_stage.add(new_node);
            } 
        }

        // Finally, instantiate the starting node and connect it to all the states of stage 1 
        // except those that do not have any children.
        // (We don't want to create dead-ends)
        // The starting node has no local information (null)
        starting_node = new DP_State_Node(null);
        prev_stage = new_stage;
        for (DP_State_Node right_node : prev_stage)
        {
            if (right_node.get_number_of_children() > 0 || right_node.is_terminal())
            {
                right_tuple = ((Tuple) right_node.state_info); // cast so that we can lookup the cost
                starting_node.add_decision(right_node, right_tuple.cost);
            }
        }
        ArrayList<DP_State_Node> starting_stage = new ArrayList<DP_State_Node>();
        starting_stage.add(starting_node);
    }

    public static void main(String args[]) 
    {
        // Run the example
        Path_Equijoin_Query example_query = new Path_Equijoin_Query();
        DP_Path_Equijoin_Instance instance = new DP_Path_Equijoin_Instance(example_query);
        instance.bottom_up();
        // Print the cost of the optimal solution
        //System.out.println("Optimal cost = " + instance.starting_node.get_opt_cost());

        // Print the optimal solution
        System.out.print("Optimal solution:");
        DP_State_Node curr = instance.starting_node.get_best_child();
        while (true)
        {
            System.out.print(" " + curr);
            if (curr.get_best_decision() == null) break;
            curr = curr.get_best_child();

        }
        System.out.println("");

        // Print the whole graph
        instance.print_edges();

        // Verify that tuples with the same join attribute values share the same decision objects
        System.out.println("\nModifying the cost of the transition from R3:[2, 1, 3, 1] to its child to 1000");
        System.out.println("Because decisions are shared, the cost of transitioning from R3:[2, 2, 3, 1] to its child should change too");
        instance.starting_node.get_children().get(1).get_children().get(0).get_children().get(0).decisions.list_of_decisions.get(0).cost = 1000.0;
        System.out.println("");
        instance.print_edges();

        System.out.println("Graph size = " + instance.graph_size());
    }
}
