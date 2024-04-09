package entities.trees;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import algorithms.trees.TDP_Anyk_Iterator;
import algorithms.trees.TDP_Eager;
import entities.Join_Predicate;
import entities.Relation;
import entities.State_Node;
import entities.Tuple;
import factorization.Binary_Partitioning;
import factorization.Node_Connector;
import util.Common;

/** 
 * A class for Τ-DP problems that are theta-join tree queries.
 * The relations can have arbitrary arities and be organized in an arbitrary tree structure.
 * Every relation maps to a stage of T-DP.
 * The starting node is artificial and the terminal nodes are implicit.
 * (To avoid the unnecessary overhead, we omit the terminal nodes).
 * The cost of transitioning to a tuple is the cost of that tuple.
 * The goal of T-DP is to find the minimum total weight of a query result.
 * Inside this class it is safe to downcast {@link entities.trees.TDP_State_Node#state_info} to {@link entities.Tuple}
 * (except the starting_state).
 * Currently only the binary partitioning method is supported as a factorization method.
 * @author Nikolaos Tziavelis
*/
public class TDP_Thetajoin_Instance extends TDP_Problem_Instance
{
    /** 
     * The query from which we create an instance of T-DP.
    */
	Tree_ThetaJoin_Query query;

    /** 
     * Creates a T-DP state/node for each tuple. 
     * @param query A theta-join query.
     * @param method Sets a particular method for the factorization of the join. If null, method selection is automatic.
    */
    public TDP_Thetajoin_Instance(Tree_ThetaJoin_Query query, String method)
    {
        super();
        this.query = query;

        if (method != null && !method.equals("binary_part")) System.out.println("Warning: currently only binary partitioning is supported for tree queries");

        Relation relation;
        TDP_State_Node new_node;
        Tuple child_tuple;
        List<TDP_State_Node> current_stage, child_stage;

        // Stores the nodes of the stages that correspond to relations (and not intermediate factorization nodes)
        List<List<TDP_State_Node>> relation_stages = new ArrayList<List<TDP_State_Node>>();
        for (int relation_idx = 0; relation_idx < query.length; relation_idx++) relation_stages.add(null);
        // Maps the indexes of the relations to the assigned stage labels
        List<Integer> relation_idx_to_stage_idx = new ArrayList<Integer>();
        for (int relation_idx = 0; relation_idx < query.length; relation_idx++) relation_idx_to_stage_idx.add(null);
        // Throughout the bottom-up pass we use this counter to label the stages
        // and at the end we will reverse the labeling so that the leaf stages appear last
        int stage_counter = 0;

        // We traverse the join-tree in bottom-up order
        // (We assume that the order of the indexes agrees with the tree order!!)
        for (int relation_idx = query.length - 1; relation_idx >= 0; relation_idx--)
        {
            relation = query.relations.get(relation_idx);
            current_stage = new ArrayList<TDP_State_Node>(relation.tuples.size());
            // Create state nodes for the tuples of the relation
            int num_branches = query.num_children(relation_idx);
            for (Tuple t : relation.tuples)
            {
                new_node = new TDP_State_Node(num_branches, t);
                if (num_branches == 0) new_node.set_to_terminal();
                current_stage.add(new_node);
            } 
            relation_stages.set(relation_idx, current_stage);

            // Create the connections between this stage and its children
            List<Integer> child_stage_indexes = new ArrayList<Integer>();
            int branch = 0;
            for (Integer child_relation_idx : query.get_children(relation_idx))
            {
                child_stage = relation_stages.get(child_relation_idx);
                int child_stage_idx = relation_idx_to_stage_idx.get(child_relation_idx);
                List<List<Join_Predicate>> join_condition = query.join_conditions.get(child_relation_idx);

                // Handle equi-join without intermediate nodes
                if (Common.is_conjunction_of_simple_equalities(join_condition))
                {
                    // Add stage to tree structure as a direct child of parent
                    child_stage_indexes.add(child_stage_idx);

                    // Convert predicates to lists of indexes of the attributes in the schema
                    int[] join_attributes_parent = join_condition.get(0).stream().mapToInt(p -> p.attr_idx_1).toArray();
                    int[] join_attributes_child = join_condition.get(0).stream().mapToInt(p -> p.attr_idx_2).toArray();

                    // Hash the nodes of the child and the parent so that they are grouped by the join attribute values
                    HashMap<List<Double>, List<State_Node>> child_hash = Common.hash_stage(child_stage, join_attributes_child);
                    HashMap<List<Double>, List<State_Node>> parent_hash = Common.hash_stage(current_stage, join_attributes_parent);

                    // For each child bucket
                    for (Map.Entry<List<Double>,List<State_Node>> map_entry : child_hash.entrySet())
                    {
                        List<Double> join_values = map_entry.getKey();
                        List<State_Node> child_nodes = map_entry.getValue(); 

                        // Look up parent bucket
                        List<State_Node> parent_nodes = parent_hash.get(join_values);
                        if (parent_nodes != null)
                        {
                            int j = 0;
                            for (State_Node parent_node : parent_nodes)
                            {
                                TDP_State_Node parent_node_tdp = (TDP_State_Node) parent_node;
                                if (j == 0)
                                {
                                    for (State_Node child_node : child_nodes)
                                    {
                                        TDP_State_Node child_node_tdp = (TDP_State_Node) child_node;
                                        parent_node_tdp.add_decision(branch, child_node_tdp, ((Tuple) child_node_tdp.state_info).cost); 
                                    }
                                }
                                else
                                {
                                    // Share the same decisions with the first node in the parent bucket
                                    parent_node_tdp.share_decisions(((TDP_State_Node) parent_nodes.get(0)), branch);
                                }
                                j += 1;
                            }
                        } 
                    }
                }
                else
                {
                    // The join condition between the two relations is given in DNF form
                    // To handle the disjunctions, construct a graph independently for each one
                    for (List<Join_Predicate> conjunction : join_condition)
                    {
                        Node_Connector.problem_setting = "T-DP";
                        Node_Connector.branch = branch;
                        Binary_Partitioning.factorize_conjunction(current_stage, child_stage, conjunction);
                    }

                    // Add the intermediate factorization stage to the tree structure
                    this.add_parent_stage_to_tree(stage_counter, Arrays.asList(child_stage_idx));
                    child_stage_indexes.add(stage_counter);
                    stage_counter += 1;
                }

                branch += 1;
            }

            // Remove dangling nodes
            current_stage.removeIf(node -> !node.is_terminal() && node.is_dead_end());
            // Add current stage to the tree structure
            this.add_parent_stage_to_tree(stage_counter, child_stage_indexes);
            relation_idx_to_stage_idx.set(relation_idx, stage_counter);
            stage_counter += 1;
        }

        // Finally, instantiate the starting node and connect it to all the states of stage 1 that can reach the leaves
        // The starting node has no local information (null)
        starting_node = new TDP_State_Node(1, null);
        for (TDP_State_Node child_node : relation_stages.get(0))
        {
            //System.out.println("Adding " + child_node + " to starting node");
            child_tuple = ((Tuple) child_node.state_info); // cast so that we can lookup the cost
            starting_node.add_decision(0, child_node, child_tuple.cost);
        }
        // R0 has only R1 as a child in the tree
        this.add_parent_stage_to_tree(stage_counter, Arrays.asList(stage_counter - 1));

        // We labeled the stages in reverse order, time to fix
        this.reverse_indexing();
    }

    /** 
     * Constructs the top-1 solution of the DP problem.
     */
    public List<Tuple> get_best_solution()
    {
        int next_stage, parent_stage, branch_idx;
    	TDP_Prefix_Solution current = new TDP_Prefix_Solution(this.starting_node.get_best_decision(0));
		TDP_Decision next_best_decision;
		TDP_State_Node parent_node;
		List<TDP_State_Node> node_list = current.solutionToNodes_strict_order();
		// Recall that stage 0 contains the starting node which is not encoded in the solutions
    	while (current.length < this.stages_no - 1)
    	{
			next_stage = current.length + 1;
			// Find the parent state for the next decision
			parent_stage = this.get_parent_stage(next_stage);
			// The starting state is not included so all stages are shifted by one
			parent_node = node_list.get(parent_stage - 1);
			branch_idx = this.get_branch_index(next_stage);
			// Follow the best decision from the parent node
    		next_best_decision = parent_node.get_best_decision(branch_idx);
			current = new TDP_Prefix_Solution(current, next_best_decision);
			// Add the latest node to the list
			node_list.add(next_best_decision.target);
    	}

        return current.solutionToTuples_strict_order();
    }
    
    public static void main(String args[]) 
    {
        Tree_ThetaJoin_Query query = new Tree_ThetaJoin_Query(3);

        TDP_Problem_Instance instance = new TDP_Thetajoin_Instance(query, null);
        instance.bottom_up();
        
        // Print the whole graph
        instance.print_edges();

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

        // Run any-k
        TDP_Anyk_Iterator iter = new TDP_Eager(instance, null);
        while (true)
        {
            TDP_Solution sol = iter.get_next();
            if (sol != null) System.out.println(sol);
            else break;
        }
    }
}
