package entities.cycles;

import java.util.ArrayList;
import java.util.Collection;

import entities.Relation;
import entities.Tuple;

/** 
 * An equi-join query where the relational atoms are binary and organized in a cycle.
 * Therefore, each relation joins with the previous one on its first attribute
 * and with the next on its second attribute.
 * @author Nikolaos Tziavelis
*/
public class SimpleCycle_Equijoin_Query
{
    public int length;
    public ArrayList<Relation> relations;

    /** 
     * A cycle query initialized with a single relation.
    */
    public SimpleCycle_Equijoin_Query(Relation r)
    {
        this.relations = new ArrayList<Relation>();
        this.relations.add(r);
        this.length = 1;
    }

    /** 
     * A cycle query initialized with a collection of relations.
    */
    public SimpleCycle_Equijoin_Query(Collection<Relation> rs)
    {
        this.relations = new ArrayList<Relation>();
        for (Relation r : rs)
            this.relations.add(r);
        this.length = rs.size();
    }

    /** 
     * Returns a small example for debugging purposes.
    */
    public SimpleCycle_Equijoin_Query()
    {
        /*
                R1                      R2                    R3                       R4
               ----                    ----                  ----                     ----
            A1  |  A2               A2  |  A3             A3  |  A4                 A4  |  A1 
            ----------------        -------------         ---------------           -------------
            1   |   1    (10)       1   |   5  (10)       2   |   1   (5)           1   |   3  (5) 
            2   |   2    (1)        3   |   2  (5)        2   |   2   (35)          2   |   3  (5)  
            3   |   3    (5)                              5   |   5   (10)          5   |   1  (1) 

            k = 1:    (3, 3), (3, 2), (2, 1), (1, 3) Cost = 20
            k = 2:    (1, 1), (1, 5), (5, 5), (5, 1) Cost = 31
            k = 3:    (3, 3), (3, 2), (2, 2), (2, 3) Cost = 50
        */

        double[] valArray;
        String[] stringArray;
        Relation r;
        this.relations = new ArrayList<Relation>();

        stringArray = new String[]{"A1", "A2"};
        r = new Relation("R1", stringArray);
        valArray = new double[]{1, 1};
        r.insert(new Tuple(valArray, 10.0, r));
        valArray = new double[]{2, 2};
        r.insert(new Tuple(valArray, 1.0, r));
        valArray = new double[]{3, 3};
        r.insert(new Tuple(valArray, 5.0, r));
        this.relations.add(r);

        stringArray = new String[]{"A2", "A3"};
        r = new Relation("R2", stringArray);
        valArray = new double[]{1, 5};
        r.insert(new Tuple(valArray, 10.0, r));
        valArray = new double[]{3, 2};
        r.insert(new Tuple(valArray, 5.0, r));
        this.relations.add(r);        

        stringArray = new String[]{"A3", "A4"};
        r = new Relation("R3", stringArray);
        valArray = new double[]{2, 1};
        r.insert(new Tuple(valArray, 5.0, r));
        valArray = new double[]{2, 2};
        r.insert(new Tuple(valArray, 35.0, r));
        valArray = new double[]{5, 5};
        r.insert(new Tuple(valArray, 10.0, r));
        this.relations.add(r);  

        stringArray = new String[]{"A4", "A1"};
        r = new Relation("R4", stringArray);
        valArray = new double[]{1, 3};
        r.insert(new Tuple(valArray, 5.0, r));
        valArray = new double[]{2, 3};
        r.insert(new Tuple(valArray, 5.0, r));
        valArray = new double[]{5, 1};
        r.insert(new Tuple(valArray, 1.0, r));
        this.relations.add(r);   

        this.length = 4;
    }    

    // Inserts a relation to the cycle
    /*
    public void insert(Relation r)
    {
        this.relations.add(r);
        this.length++;
    }
    */
}
