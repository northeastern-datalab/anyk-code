package small_examples;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import entities.Tuple;
import entities.paths.DP_Path_Equijoin_Instance;
import entities.paths.DP_State_Node;
import entities.paths.Path_Equijoin_Query;

class Test_Path_Equijoin_DP 
{
    DP_Path_Equijoin_Instance instance;

    @BeforeEach                                         
    void setUp() 
    {
        Path_Equijoin_Query example_query = new Path_Equijoin_Query();
        instance = new DP_Path_Equijoin_Instance(example_query);
        instance.bottom_up();
    }

    @Test                                            
    void test_Top1() 
    {
        double cost = 0;
        DP_State_Node curr = instance.starting_node.get_best_child();
        while (true)
        {
            cost += ((Tuple) curr.state_info).cost;
            if (curr.get_best_decision() == null) break;
            curr = curr.get_best_child();
        }

        assertEquals(20, cost, "Cost of top-1 solution");  
        assertEquals(20, instance.starting_node.get_opt_cost(), "Cost of top-1 solution");
    }

    @Test                                            
    void test_Shared_Decisions() 
    {
        instance.starting_node.get_children().get(1).get_children().get(0).get_children().get(0).
            decisions.list_of_decisions.get(0).cost = 1000.0;

        assertEquals(1000, 
            instance.starting_node.get_children().get(1).get_children().get(0).get_children().get(1).
            decisions.list_of_decisions.get(0).cost, 
            "Decision with common end affected by a cost change");
    }
}