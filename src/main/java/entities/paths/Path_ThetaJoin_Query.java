package entities.paths;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.javatuples.Pair;

import algorithms.paths.Path_Batch;
import entities.Join_Predicate;
import entities.Relation;
import entities.Tuple;
import util.Common;

/** 
 * A theta-join query where the relational atoms are organized in a path.
 * The join conditions between the relations are specified as a list of lists of @link{entities.Join_Predicate}.
 * Each element of the list refers to one of the joins between a pair of relations.
 * Currently only conjunctions are supported, hence each element of a list is a list of conjuncts.
 * TODO: generalize to DNF as a list of lists.
 * @author Nikolaos Tziavelis
*/
public class Path_ThetaJoin_Query
{
    /** 
     * The number of relations.
    */
    public int length;
    /** 
     * The relations that are part of the query.
    */
    public List<Relation> relations;
    /** 
     * join_conditions[i] refers to the join between relation i and i+1.
    */
    public List<List<Join_Predicate>> join_conditions;  

    /** 
     * A path query initialized with a single relation.
    */
    public Path_ThetaJoin_Query(Relation r)
    {
        this.relations = new ArrayList<Relation>();
        this.relations.add(r);
        this.length = 1;
        this.join_conditions = new ArrayList<List<Join_Predicate>>();
    }

    /** 
     * A path query initialized with a collection of relations.
    */
    public Path_ThetaJoin_Query(Collection<Relation> rs)
    {
        this.relations = new ArrayList<Relation>();
        for (Relation r : rs)
            this.relations.add(r);
        this.length = rs.size();
    }

    /** 
     * Sets the same join conditions between all the relations in the path.
     * The number of join conditions is l - 1 for a path of length l.
     * @param preds A conjunction of predicates as a list.
     */
    public void set_join_conditions(List<Join_Predicate> preds)
    {
        this.join_conditions = new ArrayList<List<Join_Predicate>>();
        for (int i = 0; i < this.length - 1; i++) this.join_conditions.add(preds);
    }

    /** 
     * A path query constructed from an existing equi-join query.
    */
    public Path_ThetaJoin_Query(Path_Equijoin_Query q)
    {
        this.relations = q.relations;
        this.length = q.length;
        this.join_conditions = new ArrayList<List<Join_Predicate>>();
        for (Pair<int[], int[]> equijoin : q.join_conditions)
        {
            List<Join_Predicate> ps = new ArrayList<Join_Predicate>();
            int[] left_attrs = equijoin.getValue0();
            int[] right_attrs = equijoin.getValue1();
            for (int i = 0; i < left_attrs.length; i++)
                ps.add(new Join_Predicate("E", left_attrs[i], right_attrs[i], null));
            this.join_conditions.add(ps);
        }
    }

    /** 
     * Inserts a relation to the path (on the right).
     * @param r The relation to be inserted on the right of the path.
     * @param ps A list of conjucts for the join condition between the new relation and the one on its left.
     */
    public void insert(Relation r, List<Join_Predicate> ps)
    {
        this.relations.add(r);
        this.length++;
        this.join_conditions.add(ps);
    }  

    /** 
     * Transforms the theta-join query to an equivalent equi-join on quadratically larger relations.
     * @return An equivalent equi-join.
     */
    public Path_Equijoin_Query to_Quadratic_Equijoin()
    {
        List<Relation> new_database = new ArrayList<Relation>(this.length);
        List<Pair<int[], int[]>> new_join_conditions = new ArrayList<Pair<int[], int[]>>(this.length);
        
        for (int i = 0; i < this.length - 1; i++)
        {
            // Materialize a quadratic relation for the pairs between relation i and i+1
            // To do that efficiently, use an efficient factorization between the relations
            Relation quad_r = new Relation("Quad" + i, 
                Common.concatenate_string_arrays(this.relations.get(i).schema, this.relations.get(i + 1).schema));
            Path_ThetaJoin_Query binary_q = new Path_ThetaJoin_Query(this.relations.subList(i, i + 2));
            binary_q.set_join_conditions(this.join_conditions.get(i));
            DP_Path_ThetaJoin_Instance binary_dp_instance = new DP_Path_ThetaJoin_Instance(binary_q, null);
            Path_Batch batch_alg = new Path_Batch(binary_dp_instance);
            for (DP_Solution sol : batch_alg.all_solutions)
            {
                List<Tuple> sol_tuples = sol.solutionToTuples_strict_order();
                Tuple new_tup;
                if (i == 0)
                {
                    // The first new relation gets both the costs of the original 2 relations
                    new_tup = new Tuple(Common.concatenate_double_arrays(sol_tuples.get(0).values, sol_tuples.get(1).values), 
                        sol_tuples.get(0).cost + sol_tuples.get(1).cost, quad_r);
                }
                else
                {
                    // All the others only get the cost of the second relation
                    new_tup = new Tuple(Common.concatenate_double_arrays(sol_tuples.get(0).values, sol_tuples.get(1).values), 
                        sol_tuples.get(1).cost, quad_r);
                }
                quad_r.insert(new_tup);
            }

            // For the first pair, we don't need any new equi-join conditions
            if (i != 0)
            {
                int join_attr_cnt = this.relations.get(i).schema.length;
                Relation prev_quad_relation = new_database.get(new_database.size() - 1);
                new_join_conditions.add(new Pair<int[],int[]>(
                    Common.int_range(prev_quad_relation.schema.length - join_attr_cnt, prev_quad_relation.schema.length), 
                    Common.int_range(0, join_attr_cnt)));
            }
            new_database.add(quad_r);
        }

        Path_Equijoin_Query res = new Path_Equijoin_Query(new_database);
        res.set_join_conditions(new_join_conditions);
        return res;
    }  

    /** 
     * Returns a small example for debugging purposes.
     */
    public Path_ThetaJoin_Query(int no)
    {
        if (no == 1)
        {

            /*
                                A2 < A4              A5=A5,  A4 > A6
                    R1                        R2                      R3                 
                    ----                      ----                    ----                  
                A1  |  A2  |  A3            A4  |  A5             A5  |  A6  |  A7
                -------------------       -------------          ------------------   
                0   |   1  |  5  (10)       1   |   1  (10)       1   |   0  |  0  (5)    
                0   |   2  |  6  (1)        2   |   1  (5)        1   |   3  |  3  (35)  
                0   |   3  |  7  (5)        3   |   1  (1)        2   |   1  |  2  (10) 
                0   |   4  |  8  (2)        4   |   2  (2)        2   |   6  |  1  (1)
                                            4   |   3  (0)


                k = 1:    (0, 2, 6), (3, 1), (1, 0, 0) Cost = 7
                k = 2:    (0, 2, 6), (4, 2), (2, 1, 2) Cost = 13
                k = 3:    (0, 1, 5), (3, 1), (1, 0, 0) Cost = 16
                k = 4:    (0, 3, 7), (4, 2), (2, 1, 2) Cost = 17
                k = 5:    (0, 1, 5), (2, 1), (1, 0, 0) Cost = 20
                k = 6:    (0, 1, 5), (4, 2), (2, 1, 2) Cost = 22
            */

            double[] valArray;
            String[] stringArray;
            Relation r;
            Join_Predicate p;
            List<Join_Predicate> list_of_ps;
            this.relations = new ArrayList<Relation>();
            this.join_conditions = new ArrayList<List<Join_Predicate>>();
            this.length = 3;

            // R1
            stringArray = new String[]{"A1", "A2", "A3"};
            r = new Relation("R1", stringArray);
            valArray = new double[]{0, 2, 6};
            r.insert(new Tuple(valArray, 1.0, r));
            valArray = new double[]{0, 1, 5};
            r.insert(new Tuple(valArray, 10.0, r));
            valArray = new double[]{0, 4, 8};
            r.insert(new Tuple(valArray, 2.0, r));
            valArray = new double[]{0, 3, 7};
            r.insert(new Tuple(valArray, 5.0, r));
            this.relations.add(r);

            // R2
            stringArray = new String[]{"A4", "A5"};
            r = new Relation("R2", stringArray);
            valArray = new double[]{1, 1};
            r.insert(new Tuple(valArray, 10.0, r));
            valArray = new double[]{3, 1};
            r.insert(new Tuple(valArray, 1.0, r));
            valArray = new double[]{2, 1};
            r.insert(new Tuple(valArray, 5.0, r));
            valArray = new double[]{4, 3};
            r.insert(new Tuple(valArray, 0.0, r));
            valArray = new double[]{4, 2};
            r.insert(new Tuple(valArray, 2.0, r));
            this.relations.add(r);
            list_of_ps = new ArrayList<Join_Predicate>();
            p = new Join_Predicate("IL", 1, 0, null);
            list_of_ps.add(p);
            this.join_conditions.add(list_of_ps);

            // R3
            stringArray = new String[]{"A5", "A6", "A7"};
            r = new Relation("R3", stringArray);
            valArray = new double[]{1, 3, 3};
            r.insert(new Tuple(valArray, 35.0, r));
            valArray = new double[]{1, 0, 0};
            r.insert(new Tuple(valArray, 5.0, r));
            valArray = new double[]{2, 6, 1};
            r.insert(new Tuple(valArray, 1.0, r));
            valArray = new double[]{2, 1, 2};
            r.insert(new Tuple(valArray, 10.0, r));
            this.relations.add(r);
            list_of_ps = new ArrayList<Join_Predicate>();
            p = new Join_Predicate("E", 1, 0, null);
            list_of_ps.add(p);
            p = new Join_Predicate("IG", 0, 1, null);
            list_of_ps.add(p);
            this.join_conditions.add(list_of_ps);
        }
        else if (no == 2)
        {
            /*
                              A2 < A3         
                    R1                     R2                                  
                    ----                  ----                     
                A1  |  A2              A3  |  A4            
                ---------------       -------------      
                0   |   1   (10)       1   |   1  (10)       
                0   |   3   (1)        2   |   1  (5)   
                1   |   3   (5)        3   |   1  (1)      
                2   |   3   (2)        3   |   2  (2)      
                0   |   5   (1)        4   |   3  (0)
                3   |   5   (3)        4   |   1  (10)
                1   |   6   (1)        7   |   0  (10)
                0   |   8   (2)        8   |   0  (5)
                0   |   9   (6)        9   |   0  (6)


                k = 1:    (0, 3), (4, 3) Cost = 1
            */

            double[] valArray;
            String[] stringArray;
            Relation r;
            Join_Predicate p;
            List<Join_Predicate> list_of_ps;
            this.relations = new ArrayList<Relation>();
            this.join_conditions = new ArrayList<List<Join_Predicate>>();
            this.length = 2;

            // R1
            stringArray = new String[]{"A1", "A2"};
            r = new Relation("R1", stringArray);
            valArray = new double[]{0, 1};
            r.insert(new Tuple(valArray, 10.0, r));
            valArray = new double[]{0, 3};
            r.insert(new Tuple(valArray, 1.0, r));
            valArray = new double[]{1, 3};
            r.insert(new Tuple(valArray, 5.0, r));
            valArray = new double[]{2, 3};
            r.insert(new Tuple(valArray, 2.0, r));
            valArray = new double[]{0, 5};
            r.insert(new Tuple(valArray, 1.0, r));
            valArray = new double[]{3, 5};
            r.insert(new Tuple(valArray, 3.0, r));
            valArray = new double[]{1, 6};
            r.insert(new Tuple(valArray, 1.0, r));
            valArray = new double[]{0, 8};
            r.insert(new Tuple(valArray, 2.0, r));
            valArray = new double[]{0, 9};
            r.insert(new Tuple(valArray, 6.0, r));
            this.relations.add(r);

            // R2
            stringArray = new String[]{"A3", "A4"};
            r = new Relation("R2", stringArray);
            valArray = new double[]{1, 1};
            r.insert(new Tuple(valArray, 10.0, r));
            valArray = new double[]{2, 1};
            r.insert(new Tuple(valArray, 5.0, r));
            valArray = new double[]{3, 1};
            r.insert(new Tuple(valArray, 1.0, r));
            valArray = new double[]{3, 2};
            r.insert(new Tuple(valArray, 2.0, r));
            valArray = new double[]{4, 3};
            r.insert(new Tuple(valArray, 0.0, r));
            valArray = new double[]{4, 1};
            r.insert(new Tuple(valArray, 10.0, r));
            valArray = new double[]{7, 0};
            r.insert(new Tuple(valArray, 10.0, r));
            valArray = new double[]{8, 0};
            r.insert(new Tuple(valArray, 5.0, r));
            valArray = new double[]{9, 0};
            r.insert(new Tuple(valArray, 6.0, r));
            this.relations.add(r);
            list_of_ps = new ArrayList<Join_Predicate>();
            p = new Join_Predicate("IL", 1, 0, null);
            list_of_ps.add(p);
            this.join_conditions.add(list_of_ps);
        }
        else
        {
            /*
                            |A2 - A3| < 2         
                    R1                     R2                                  
                    ----                  ----                     
                A1  |  A2                A3  |  A4            
                -----------------       ---------------      
                0   |   0.5   (10)       0    |   1  (10)       
                0   |   1     (1)        2    |   1  (5)   
                1   |   2     (5)        2.5  |   1  (1)      
                2   |   4     (2)        3    |   2  (2)      
                0   |   5     (1)        3.5  |   3  (0)
                3   |   5.5   (3)        4    |   1  (10)
                1   |   7     (1)        5    |   0  (10)
                0   |   7.5   (2)        6    |   0  (5)


                k = 1:    (0, 5), (3.5, 3) Cost = 0
            */            

            double[] valArray;
            String[] stringArray;
            Relation r;
            Join_Predicate p;
            List<Join_Predicate> list_of_ps;
            this.relations = new ArrayList<Relation>();
            this.join_conditions = new ArrayList<List<Join_Predicate>>();
            this.length = 2;

            // R1
            stringArray = new String[]{"A1", "A2"};
            r = new Relation("R1", stringArray);
            valArray = new double[]{0, 0.5};
            r.insert(new Tuple(valArray, 10.0, r));
            valArray = new double[]{0, 1};
            r.insert(new Tuple(valArray, 1.0, r));
            valArray = new double[]{1, 2};
            r.insert(new Tuple(valArray, 5.0, r));
            valArray = new double[]{2, 4};
            r.insert(new Tuple(valArray, 2.0, r));
            valArray = new double[]{0, 5};
            r.insert(new Tuple(valArray, 1.0, r));
            valArray = new double[]{3, 5.5};
            r.insert(new Tuple(valArray, 3.0, r));
            valArray = new double[]{1, 7};
            r.insert(new Tuple(valArray, 1.0, r));
            valArray = new double[]{0, 7.5};
            r.insert(new Tuple(valArray, 2.0, r));
            this.relations.add(r);

            // R2
            stringArray = new String[]{"A3", "A4"};
            r = new Relation("R2", stringArray);
            valArray = new double[]{0, 1};
            r.insert(new Tuple(valArray, 10.0, r));
            valArray = new double[]{2, 1};
            r.insert(new Tuple(valArray, 5.0, r));
            valArray = new double[]{2.5, 1};
            r.insert(new Tuple(valArray, 1.0, r));
            valArray = new double[]{3, 2};
            r.insert(new Tuple(valArray, 2.0, r));
            valArray = new double[]{3.5, 3};
            r.insert(new Tuple(valArray, 0.0, r));
            valArray = new double[]{4, 1};
            r.insert(new Tuple(valArray, 10.0, r));
            valArray = new double[]{5, 0};
            r.insert(new Tuple(valArray, 10.0, r));
            valArray = new double[]{6, 0};
            r.insert(new Tuple(valArray, 5.0, r));
            this.relations.add(r);
            list_of_ps = new ArrayList<Join_Predicate>();
            p = new Join_Predicate("B", 1, 0, 2.0);
            list_of_ps.add(p);
            this.join_conditions.add(list_of_ps);
        }
    }    
}
