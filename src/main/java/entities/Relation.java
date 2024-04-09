package entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/** 
 * A relation is an arraylist of tuples with a common list of attributes
 * The schema of the relation specifies those attributes
 * The relation is empty upon construction
 * @author Nikolaos Tziavelis
*/
public class Relation
{
    public String relation_id;
    public String[] schema;
    public ArrayList<Tuple> tuples;

	/** 
     * @param relation_identifier A string identifier for the relation
     * @param relation_schema An array of attribute names
    */
    public Relation(String relation_identifier, String[] relation_schema)
    {
        this.relation_id = relation_identifier;
        this.schema = relation_schema;
        this.tuples = new ArrayList<Tuple>();
    }

    /** 
     * @param t The tuple to be inserted at the end of the relation
     */
    public void insert(Tuple t)
    {
        this.tuples.add(t);
    }

    /** 
     * @param index The index of a tuple
     * @return Tuple The tuple at the specified index
     */
    public Tuple get(int index)
    {
        return this.tuples.get(index);
    }

    /** 
     * @param ts A collection of tuples to be inserted all at once
     */
    // Inserts a collection of tuple in the relation
    public void insertAll(Collection<Tuple> ts)
    {
        this.tuples.addAll(ts);
    }

	/** 
	 * Sorts the tuples in the relation according to their compareTo() method
	 * @see entities.Tuple#compareTo
     */
    public void sort()
    {
        Collections.sort(this.tuples);
    }
    
    /** 
     * @return int The number of tuples in the relation
     */
    public int get_size()
    {
        return tuples.size();
    }
    
    /** 
     * @return String The contents of the relation in string format without the printing the tuple costs
     */
    public String toString_no_cost()
    {
        StringBuilder str = new StringBuilder();
        str.append("Relation " + this.relation_id + "\n");
        for (String attribute : this.schema)
            str.append(attribute + " ");
        str.append("\n");
        for (Tuple t : this.tuples)
            str.append(t.flat_format_no_cost() + "\n");
        str.append("End of " + this.relation_id + "\n");
        return str.toString();
    }

    /** 
     * @return String The contents of the relation in string format
     */
    @Override
    public String toString()
    {
        StringBuilder str = new StringBuilder();
        str.append("Relation " + this.relation_id + "\n");
        for (String attribute : this.schema)
            str.append(attribute + " ");
        str.append("\n");
        for (Tuple t : this.tuples)
            str.append(t.flat_format() + "\n");
        str.append("End of " + this.relation_id + "\n");
        return str.toString();
    }
}
