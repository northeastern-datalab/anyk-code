package algorithms.cycles;

import java.util.Collections;
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
 * Sorts the output, hence calling {@link #get_next} returns the output tuples in ranked order.
 * @author Nikolaos Tziavelis
*/
public class NPRR_Sort extends NPRR
{
	public NPRR_Sort(SimpleCycle_Equijoin_Query query)
    {
        super(query);
        Collections.sort(out_relation.tuples);
    }

    public static void main(String args[]) 
    {
        SimpleCycle_Equijoin_Query query = new SimpleCycle_Equijoin_Query();
        NPRR_Sort iter = new NPRR_Sort(query);

        Tuple res;
        while ((res = iter.get_next()) != null)
            System.out.println("Cost= " + res.cost + "  " + res);

        System.out.println("==============================");

        
        Database_Query_Generator gen = new Cycle_HeavyLightPattern(4, 6);
        gen.create();
        gen.print_database();
        List<Relation> database = gen.get_database();
        query = new SimpleCycle_Equijoin_Query(database);
        
        iter = new NPRR_Sort(query);
        System.out.println("==== NPRR (w/sort) ====");
        while ((res = iter.get_next()) != null)
            System.out.println("Cost= " + res.cost + "  " + res);
        
        
        System.out.println("==== Recursive ====");
        SimpleCycle_Anyk_Iterator iter2 = new SimpleCycle_Anyk_Iterator(query, "Recursive", null);

        while ((res = iter2.get_next()) != null)
        {
            System.out.println(res);
            System.out.println("Cost= " + res.cost + "  " + res);
        }
    }
}