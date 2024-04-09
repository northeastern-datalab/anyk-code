package entities.trees;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import entities.Join_Predicate;
import entities.Relation;
import entities.Tuple;

/** 
 * A theta-join query where the relational atoms are organized in a tree.
 * The join conditions between the relations are specified as a list of lists of @link{entities.Join_Predicate}.
 * Each element of the list refers to one of the joins between a relation and its parent in the tree.
 * (The first element that corresponds to the root relation is null).
 * The predicates are given in a DNF form as a list of lists.
 * The elements of the outer list are combined with disjunction.
 * The elements of the inner lists are combined with conjunction.
 * WARNING: we assume that for every @link{entities.Join_Predicate}, the first attribute refers to the parent and the second to the child.
 * @author Nikolaos Tziavelis
*/
public class Tree_ThetaJoin_Query
{
    /** 
     * The number of relations.
    */
    public int length;
    /** 
     * The relations that are part of the query (the nodes of the tree).
    */
    public List<Relation> relations;
    /** 
     * Encodes the tree structure by storing the parent of each relation (which is unique).
    */
    public List<Integer> parents;
    /** 
     * join_conditions[i] refers to the join between relation i and its parent.
    */
    public List<List<List<Join_Predicate>>> join_conditions;  

    /** 
     * A tree query initialized with no relations.
    */
    public Tree_ThetaJoin_Query()
    {
        this.relations = new ArrayList<Relation>();
        this.parents = new ArrayList<Integer>();
        this.length = 0;
        this.join_conditions = new ArrayList<List<List<Join_Predicate>>>();
    }

    /** 
     * Adds a relation to the tree with a DNF as the join condition.
     * @param r The relation to be added.
     * @param idx The index of the relation to be added (a child index cannot be smaller than its parent's).
     * @param parent_idx The index of the parent relation (the root should have index 0 and its parent -1).
     * @param ps A list of conjuncts for the join condition between the new relation and its parent.
     */
    public void add_to_tree_wDNF(Relation r, int idx, int parent_idx, List<List<Join_Predicate>> cond)
    {
        if (idx < parent_idx)
        {
            System.err.println("Index of relation cannot be smaller than the index of its parent in the join tree");
            System.exit(1);
        }
        // If the idx of the added relation is not the next one, grow the lists we keep up to that position
        while (relations.size() <= idx) relations.add(null);
        while (parents.size() <= idx) parents.add(null);
        while (join_conditions.size() <= idx) join_conditions.add(null);
        this.relations.set(idx, r);
        this.parents.set(idx, parent_idx);
        this.join_conditions.set(idx, cond);
        this.length++;
    }  

    /** 
     * Adds a relation to the tree with a conjunction as the join condition.
     * @param r The relation to be added.
     * @param idx The index of the relation to be added (a child index cannot be smaller than its parent's).
     * @param parent_idx The index of the parent relation (the root should have index 0 and its parent -1).
     * @param ps A list of conjucts for the join condition between the new relation and its parent.
     */
    public void add_to_tree_wConjunction(Relation r, int idx, int parent_idx, List<Join_Predicate> ps)
    {
        if (idx < parent_idx)
        {
            System.err.println("Index of relation cannot be smaller than the index of its parent in the join tree");
            System.exit(1);
        }
        // If the idx of the added relation is not the next one, grow the lists we keep up to that position
        while (relations.size() <= idx) relations.add(null);
        while (parents.size() <= idx) parents.add(null);
        while (join_conditions.size() <= idx) join_conditions.add(null);
        this.relations.set(idx, r);
        this.parents.set(idx, parent_idx);
        this.join_conditions.set(idx, Arrays.asList(ps));
        this.length++;
    }  

    /** 
     * Sets the same join condition between all pairs of relations in the tree.
     * The condition has to be a conjunction of atomic predicates.
     * Disjunctions are not supported by this method.
     * @param preds A conjunction of predicates as a list.
     */
    public void set_join_conditions_as_conjunction(List<Join_Predicate> preds)
    {
        for (int i = 1; i < this.length; i++) 
            this.join_conditions.set(i, Arrays.asList(preds));
    }

    /** 
     * Sets the same join condition between all pairs of relations in the tree.
     * The condition has to be a disjunction of conjunctions (DNF).
     * @param cond A DNF formula as a list of lists of atomic predicates.
     */
    public void set_join_conditions_as_dnf(List<List<Join_Predicate>> cond)
    {
        for (int i = 0; i < this.length; i++) 
            this.join_conditions.set(i, cond);
    }

    /** 
     * Returns whether a particular relation is a leaf in the join-tree of the query.
     * The current implementation of this method takes linear time in the size of the query.
     * @param relation_idx The index of the relation to be checked.
     */
    public boolean relation_is_leaf(int relation_idx)
    {
        for (Integer parent_idx : parents)
        {
            if (relation_idx == parent_idx) return false;
        }
        return true;
    }

    /** 
     * Returns the number of children that a relation has in the join-tree of the query.
     * The current implementation of this method takes linear time in the size of the query.
     * @param relation_idx The index of the relation.
     */
    public int num_children(int relation_idx)
    {
        int count = 0;
        for (Integer parent_idx : parents)
        {
            if (relation_idx == parent_idx) count += 1;
        }
        return count;
    }

    /** 
     * Returns a list that contains in position i the children of node i.
     * Takes linear time in the size of the query.
     * @param relation_idx The index of the relation.
     */
    public List<List<Integer>> get_children_lists()
    {
        List<List<Integer>> children_lists = new ArrayList<List<Integer>>();
        for (int i = 0; i < this.length; i++) 
            children_lists.add(new ArrayList<Integer>());
        
        for (int i = 1; i < this.length; i++)
        {
            int parent_of_i = this.parents.get(i);
            children_lists.get(parent_of_i).add(i);
        }
        return children_lists;
    }

    /** 
     * Returns the of children that a relation has in the join-tree of the query.
     * The current implementation of this method takes linear time in the size of the query.
     * @param relation_idx The index of the relation.
     */
    public List<Integer> get_children(int relation_idx)
    {
        List<Integer> res = new ArrayList<Integer>();
        for (int i = 0; i < parents.size(); i++)
        {
            if (relation_idx == parents.get(i)) res.add(i);
        }
        return res;
    }

    /** 
     * Returns a small example for debugging purposes.
     */
    public Tree_ThetaJoin_Query(int no)
    {
        if (no == 1)
        {

            /*
                                A4 > A2              A5=A5,  A4 > A6
                    R2                        R1(parent)               R3                 
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
            Join_Predicate p, p2;

            this.relations = new ArrayList<Relation>();
            this.parents = new ArrayList<Integer>();
            this.length = 0;
            this.join_conditions = new ArrayList<List<List<Join_Predicate>>>();

            // R2
            stringArray = new String[]{"A1", "A2", "A3"};
            r = new Relation("R2", stringArray);
            valArray = new double[]{0, 2, 6};
            r.insert(new Tuple(valArray, 1.0, r));
            valArray = new double[]{0, 1, 5};
            r.insert(new Tuple(valArray, 10.0, r));
            valArray = new double[]{0, 4, 8};
            r.insert(new Tuple(valArray, 2.0, r));
            valArray = new double[]{0, 3, 7};
            r.insert(new Tuple(valArray, 5.0, r));
            p = new Join_Predicate("IG", 0, 1, null);
            this.add_to_tree_wDNF(r, 1, 0, Arrays.asList(Arrays.asList(p)));

            // R1 (root)
            stringArray = new String[]{"A4", "A5"};
            r = new Relation("R1", stringArray);
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
            this.add_to_tree_wDNF(r, 0, -1, null);

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
            p = new Join_Predicate("E", 1, 0, null);
            p2 = new Join_Predicate("IG", 0, 1, null);
            this.add_to_tree_wConjunction(r, 2, 0, Arrays.asList(p, p2));
        }
        else if (no == 2)
        {
            /*
                    R1                            R2                    R3                        R4
                ----                          ----                  ----                      ----
                A1  |  A2  | A3               A1  |  A4             A2  |  A5                 A3  |  A6
                ---------------------        -------------         ---------------           -------------
                1   |   2  |  3  (10)         1   |   1  (10)       5   |   1   (1)           9   |   1  (10) 
                4   |   0  |  0  (100)        4   |   2  (1)        8   |   2   (3)           6   |   2  (1)  
                7   |   8  |  9  (5)          7   |   3  (2)        8   |   3   (4)           9   |   3  (20) 
                4   |   5  |  6  (1)          4   |   0  (100)      0   |   0   (0)           0   |   0  (0)

                k = 1:    (4, 5, 6), (4, 2), (5, 1), (6, 2) Cost = 4
                k = 2:    (7, 8, 9), (7, 3), (8, 2), (9, 1) Cost = 20
                k = 3:    (7, 8, 9), (7, 3), (8, 3), (9, 1) Cost = 21
                k = 4:    (7, 8, 9), (7, 3), (8, 2), (9, 3) Cost = 30
                k = 5:    (7, 8, 9), (7, 3), (8, 3), (9, 3) Cost = 31
                k = 6:    (4, 0, 0), (4, 2), (0, 0), (0, 0) Cost = 101
                k = 7:    (4, 5, 6), (4, 0), (5, 1), (6, 2) Cost = 103
                k = 8:    (4, 0, 0), (4, 0), (0, 0), (0, 0) Cost = 200
            */

            double[] valArray;
            String[] stringArray;
            Relation r;
            Join_Predicate p;

            this.relations = new ArrayList<Relation>();
            this.parents = new ArrayList<Integer>();
            this.length = 0;
            this.join_conditions = new ArrayList<List<List<Join_Predicate>>>();

            stringArray = new String[]{"A1", "A2", "A3"};
            r = new Relation("R1", stringArray);
            valArray = new double[]{1, 2, 3};
            r.insert(new Tuple(valArray, 10.0, r));
            valArray = new double[]{7, 8, 9};
            r.insert(new Tuple(valArray, 5.0, r));
            valArray = new double[]{4, 0, 0};
            r.insert(new Tuple(valArray, 100.0, r));
            valArray = new double[]{4, 5, 6};
            r.insert(new Tuple(valArray, 1.0, r));
            this.add_to_tree_wDNF(r, 0, -1, null);

            stringArray = new String[]{"A1", "A4"};
            r = new Relation("R2", stringArray);
            valArray = new double[]{1, 1};
            r.insert(new Tuple(valArray, 10.0, r));
            valArray = new double[]{4, 2};
            r.insert(new Tuple(valArray, 1.0, r));
            valArray = new double[]{7, 3};
            r.insert(new Tuple(valArray, 2.0, r));
            valArray = new double[]{4, 0};
            r.insert(new Tuple(valArray, 100.0, r));
            p = new Join_Predicate("E", 0, 0, null);
            this.add_to_tree_wConjunction(r, 1, 0, Arrays.asList(p));   

            stringArray = new String[]{"A2", "A5"};
            r = new Relation("R3", stringArray);
            valArray = new double[]{5, 1};
            r.insert(new Tuple(valArray, 1.0, r));
            valArray = new double[]{8, 2};
            r.insert(new Tuple(valArray, 3.0, r));
            valArray = new double[]{8, 3};
            r.insert(new Tuple(valArray, 4.0, r));
            valArray = new double[]{0, 0};
            r.insert(new Tuple(valArray, 0.0, r));
            p = new Join_Predicate("E", 1, 0, null);
            this.add_to_tree_wConjunction(r, 2, 0, Arrays.asList(p));   

            stringArray = new String[]{"A3", "A6"};
            r = new Relation("R4", stringArray);
            valArray = new double[]{9, 1};
            r.insert(new Tuple(valArray, 10.0, r));
            valArray = new double[]{6, 2};
            r.insert(new Tuple(valArray, 1.0, r));
            valArray = new double[]{9, 3};
            r.insert(new Tuple(valArray, 20.0, r));
            valArray = new double[]{0, 0};
            r.insert(new Tuple(valArray, 0.0, r));        
            p = new Join_Predicate("E", 2, 0, null);
            this.add_to_tree_wConjunction(r, 3, 0, Arrays.asList(p));    
        }
        else if (no == 3)
        {
            /*
                    R1                        R2                      R3                             R4
                    ----                      ----                    ----                           ----
                A1  |  A2  |  A3            A3  |  A4             A4  |  A5  |  A6  |  A7           A6  |  A7 
                -------------------       -------------          -------------------------         ------------
                1   |   1  |  1  (10)       1   |   5  (10)       2   |   1  |  3   |  1  (5)       3   |   1  (5) 
                2   |   2  |  2  (1)        3   |   2  (5)        2   |   2  |  3   |  1  (35)      3   |   2  (10)  
                3   |   3  |  3  (5)                              5   |   5  |  3   |  2  (10)      7   |   7  (1) 

                k = 1:    (3, 3, 3), (3, 2), (2, 1, 3, 1), (3, 1) Cost = 20
                k = 2:    (1, 1, 1), (1, 5), (5, 5, 3, 2), (3, 2) Cost = 40
                k = 3:    (3, 3, 3), (3, 2), (2, 2, 3, 1), (3, 1) Cost = 50
            */

            double[] valArray;
            String[] stringArray;
            Relation r;
            Join_Predicate p, p1, p2;

            this.relations = new ArrayList<Relation>();
            this.parents = new ArrayList<Integer>();
            this.length = 0;
            this.join_conditions = new ArrayList<List<List<Join_Predicate>>>();

            stringArray = new String[]{"A1", "A2", "A3"};
            r = new Relation("R1", stringArray);
            valArray = new double[]{1, 1, 1};
            r.insert(new Tuple(valArray, 10.0, r));
            valArray = new double[]{2, 2, 2};
            r.insert(new Tuple(valArray, 1.0, r));
            valArray = new double[]{3, 3, 3};
            r.insert(new Tuple(valArray, 5.0, r));
            this.add_to_tree_wDNF(r, 0, -1, null);

            stringArray = new String[]{"A3", "A4"};
            r = new Relation("R2", stringArray);
            valArray = new double[]{1, 5};
            r.insert(new Tuple(valArray, 10.0, r));
            valArray = new double[]{3, 2};
            r.insert(new Tuple(valArray, 5.0, r));
            p = new Join_Predicate("E", 2, 0, null);
            this.add_to_tree_wConjunction(r, 1, 0, Arrays.asList(p)); 

            stringArray = new String[]{"A4", "A5", "A6", "A7"};
            r = new Relation("R3", stringArray);
            valArray = new double[]{2, 1, 3, 1};
            r.insert(new Tuple(valArray, 5.0, r));
            valArray = new double[]{2, 2, 3, 1};
            r.insert(new Tuple(valArray, 35.0, r));
            valArray = new double[]{5, 5, 3, 2};
            r.insert(new Tuple(valArray, 10.0, r));
            p = new Join_Predicate("E", 1, 0, null);
            this.add_to_tree_wConjunction(r, 2, 1, Arrays.asList(p)); 

            stringArray = new String[]{"A6", "A7"};
            r = new Relation("R4", stringArray);
            valArray = new double[]{3, 1};
            r.insert(new Tuple(valArray, 5.0, r));
            valArray = new double[]{3, 2};
            r.insert(new Tuple(valArray, 10.0, r));
            valArray = new double[]{7, 7};
            r.insert(new Tuple(valArray, 1.0, r));
            p1 = new Join_Predicate("E", 2, 0, null);
            p2 = new Join_Predicate("E", 3, 1, null);
            this.add_to_tree_wConjunction(r, 3, 2, Arrays.asList(p1, p2)); 
        }
    }    


    /** 
     * @return String The predicate in string format
     */
    @Override
    public String toString()
    {
        StringBuilder str = new StringBuilder();

        str.append("Parents: " + this.parents + "\n");
        str.append("Join Conditions: " + this.join_conditions + "\n");

        return str.toString();
    }
}
