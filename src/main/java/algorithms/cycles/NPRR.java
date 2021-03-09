package algorithms.cycles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import data.Cycle_HeavyLightPattern;
import data.Database_Query_Generator;
import entities.Relation;
import entities.Tuple;
import entities.cycles.SimpleCycle_Equijoin_Query;

/** 
 * Worst-case optimal join algorithm from Ngo et al., JACM'98 
 * <a href="https://doi/10.1145/3180143">https://doi/10.1145/3180143</a>. 
 * Implements the simplified algorithm for simple cycles (Lemma 8.1 in the paper).
 * Expected to be faster than the generic algorithm due to fewer hash tables needed.
 * Does NOT sort the output.
 * @author Nikolaos Tziavelis
*/
public class NPRR
{
    /** 
     * The relations that form the cycle.
    */
    List<Relation> relations;    
    /** 
     * The length of the cycle. 
    */ 
    int cyc_length;       
    /** 
     * Indexes on the relations that indicate which ones we use for cartesian product and which ones for filtering.
    */      
    int cartesian_start, filter_start;  
    /** 
     * Hash tables for the filtering relations.
    */ 
    List<HashMap<List<Double>,Double>> hashes;
    /** 
     * The relation that output tuples use as a reference.
     * @see entities.Relation
    */
    Relation out_relation;
    /** 
     * Size of the output.
    */ 
    int tuples_no;
    /** 
     * Index of last output tuple enumerated.
    */ 
    int current_index;

	public NPRR(SimpleCycle_Equijoin_Query query)
    {
        this.relations = query.relations;
        this.cyc_length = query.length;

        String[] out_attributes = new String[cyc_length];
        for (int i = 1; i <= cyc_length; i++) out_attributes[i - 1 ] = ("A" + i);
        out_relation = new Relation("Rout", out_attributes);

        if (this.cyc_length % 2 == 0)
        {
            even_cycle();
        }
        else
        {
            System.err.println("NPRR currently only supports even length cycles");
            System.exit(1);
        }

        // Collections.sort(out_relation.tuples);

        this.tuples_no = out_relation.get_size();
        this.current_index = -1;
    }

    /** 
     * @return Tuple Returns the next output tuple (or null if none remain).
     */
    public Tuple get_next()
    {
        this.current_index++;
        if (current_index == this.tuples_no) return null;
        Tuple ret = out_relation.get(current_index);
        return ret;
    }

    /** 
     * Produces all the tuples of the simple cycle join for cycles of even length.
     */
    private void even_cycle()
    {
        // Compare |R0| * |R2| * |R4| * ... with |R1| * |R3| * |R5| * ...
        // to see which cartesian product is less expensive
        long even_size = 1;
        for (int i = 0; i < cyc_length; i += 2) even_size = even_size * relations.get(i).get_size();
        long odd_size = 1;
        for (int i = 1; i < cyc_length; i += 2) odd_size = odd_size * relations.get(i).get_size();        

        // If the odd cartesian product is smaller, then start joining with index 0 (= R0) 
        // and filtering with index 1 (= R1)
        // Else the opposite
        if (even_size <= odd_size)
        {
            this.cartesian_start = 0;
            this.filter_start = 1;
        }
        else
        {
            this.cartesian_start = 1;
            this.filter_start = 0;            
        }

        // First build hash tables for the filtering relations
        // Those hashes use the list of tuple values as a key and they return the tuple cost
        hashes = new ArrayList<HashMap<List<Double>,Double>>();
        for (int i = filter_start; i < cyc_length; i += 2)
        {
            HashMap<List<Double>,Double> hash = new HashMap<List<Double>,Double>();
            for (Tuple t : relations.get(i).tuples)
            {
                List<Double> key_list = new ArrayList<Double>();
                key_list.add(t.values[0]);
                key_list.add(t.values[1]);
                hash.put(key_list, t.cost);
            }
            hashes.add(hash);
        }

        // Essentially we want one for loop for each other relation starting from cartesian_start
        // We need recursion to implement that
        cartesian_product(cartesian_start, new LinkedList<Tuple>());
    }

    
    /** 
     * Performs a for loop for each relation in the Cartesian Product.
     * Implemented recursively since the number of for loops is specified by the input.
     * The arguments keep track of the recursion state.
     * @param relation_idx
     * @param curr_tuples
     */
    private void cartesian_product(int relation_idx, List<Tuple> curr_tuples)
    {
        //System.out.println("Cartesian index = " + relation_idx + "   Tuples = " + curr_tuples);
        if (relation_idx >= cyc_length)
        {
            // We went through all the relations - the recursion ends
            // At this point we want to start filtering against the other relations
            // (the ones in-between that we skipped)
            filter(filter_start, curr_tuples, 0.0);
            return;
        }

        for (Tuple t : relations.get(relation_idx).tuples)
        {
            // For each of the yuples in the current relation, go to the next relation
            curr_tuples.add(t);
            cartesian_product(relation_idx + 2, curr_tuples);
            // Remove the tuple before returning
            curr_tuples.remove(curr_tuples.size() - 1);
        }
    }

    
    /** 
     * Given a list of tuples from the Cartesian Product of half the relations,
     * this recursive function filters the result against the other half.
     * We also keep track of the cost of the tuples from the filtering relations.
     * @param relation_idx
     * @param tuples_to_filter
     * @param cost_from_filters
     */
    private void filter(int relation_idx, List<Tuple> tuples_to_filter, double cost_from_filters)
    {
        //System.out.println("Filter = " + relation_idx + "   Tuples = " + tuples_to_filter + "  Cost = " + cost_from_filters);

        if (relation_idx >= cyc_length)
        {
            // We went through all the relations - the recursion ends
            // The tuples survived the filtering so instantiate the result
            double[] vals = new double[cyc_length];
            double cartesian_cost = 0.0;
            int attr_counter = 0;
            for (Tuple t : tuples_to_filter) 
            {
                vals[attr_counter] = t.values[0];
                vals[attr_counter + 1] = t.values[1];
                attr_counter += 2;
                cartesian_cost += t.cost;
            }
            Tuple new_res = new Tuple(vals, cartesian_cost + cost_from_filters, out_relation);
            out_relation.insert(new_res);
            return;
        }

        // Half the relations don't have hashes
        HashMap<List<Double>,Double> hash = hashes.get(relation_idx / 2);
        // Check our tuples against the hash
        List<Double> vals_to_check = new ArrayList<Double>();

        // Find the attribute values we have to check from our tuples
        // Their position depends on which half of the relations we use as a filter
        // and which half as a cartesian product
        int pos_of_first_attr, pos_of_second_attr;
        if (cartesian_start == 0)
        {
            pos_of_first_attr = relation_idx / 2;
            if (pos_of_first_attr == (cyc_length / 2) - 1) pos_of_second_attr = 0;
            else pos_of_second_attr = pos_of_first_attr + 1;
            vals_to_check.add(tuples_to_filter.get(pos_of_first_attr).values[1]);
            vals_to_check.add(tuples_to_filter.get(pos_of_second_attr).values[0]);
        }
        else
        {
            pos_of_second_attr = relation_idx / 2;
            if (pos_of_second_attr == 0) pos_of_first_attr = (cyc_length / 2) - 1;
            else pos_of_first_attr = pos_of_second_attr - 1;    
            vals_to_check.add(tuples_to_filter.get(pos_of_first_attr).values[1]);
            vals_to_check.add(tuples_to_filter.get(pos_of_second_attr).values[0]);
            //System.out.println("Pos1 = " + pos_of_first_attr + " Pos2 = " + pos_of_second_attr + "   Vals = " + vals_to_check);
        }
        Double cost_of_filter;
        if ((cost_of_filter = hash.get(vals_to_check)) == null)
        {
            // Does not pass the filter
            return;
        }
        else
        {
            // Our tuples pass this filter, proceed with the next relation
            filter(relation_idx + 2, tuples_to_filter, cost_from_filters + cost_of_filter);
        }
    }

    public static void main(String args[]) 
    {
        SimpleCycle_Equijoin_Query query = new SimpleCycle_Equijoin_Query();
        NPRR iter = new NPRR(query);

        Tuple res;
        while ((res = iter.get_next()) != null)
            System.out.println("Cost= " + res.cost + "  " + res);

        System.out.println("==============================");

        
        Database_Query_Generator gen = new Cycle_HeavyLightPattern(4, 6);
        gen.create();
        gen.print_database();
        List<Relation> database = gen.get_database();
        query = new SimpleCycle_Equijoin_Query(database);
        
        iter = new NPRR(query);
        System.out.println("==== NPRR ====");
        while ((res = iter.get_next()) != null)
            System.out.println("Cost= " + res.cost + "  " + res);
    }
}