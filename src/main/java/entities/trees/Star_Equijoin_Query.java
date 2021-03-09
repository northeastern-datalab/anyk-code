package entities.trees;

import java.util.ArrayList;
import java.util.Collection;

import entities.Relation;
import entities.Tuple;

/** 
 * An equi-join query where the relational atoms are organized in a star.
 * The first relation is at the center of the star and all the other relations join with it.
 * @author Nikolaos Tziavelis
*/
public class Star_Equijoin_Query
{
    /** 
     * The number of relations.
    */
    public int size;
    /** 
     * The relations that are part of the query.
    */
    public ArrayList<Relation> relations;

    /** 
     * A star query initialized with a single relation (the center one).
    */
    public Star_Equijoin_Query(Relation r)
    {
        this.relations = new ArrayList<Relation>();
        this.relations.add(r);
        this.size = 1;
    }
    
    /** 
     * Inserts a relation to the star.
     * @param r
     */
    public void insert(Relation r)
    {
        this.relations.add(r);
        this.size++;
    }

    /** 
     * A star query initialized with a collection of relations.
     * The first one has to be the center (parent) relation with l-1 attributes.
    */
    public Star_Equijoin_Query(Collection<Relation> rs)
    {
        this.relations = new ArrayList<Relation>();
        for (Relation r : rs)
            this.relations.add(r);
        this.size = rs.size();
    }

    /** 
     * Returns a small example for debugging purposes.
     */
    public Star_Equijoin_Query()
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
        this.relations = new ArrayList<Relation>();

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
        this.relations.add(r);

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
        this.relations.add(r);        

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
        this.relations.add(r);  

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
        this.relations.add(r);   

        this.size = 4;
    }    

}
