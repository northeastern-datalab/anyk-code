package entities;

import java.util.Arrays;
import java.util.List;

/** 
 * A database tuple with some associated cost
 * The attribute list of the tuple must agree with the schema of the relation it belongs to
 * @author Nikolaos Tziavelis
*/
public class Tuple implements Comparable<Tuple>
{
    public double[] values;
    public double cost;
    public Relation relation;

    /** 
     * Constructs a tuple based on a given list of values for the attributes<br>
     * CAUTION: The object will contain a reference to the passed list of values
     * No deep copy is made
     * @param values_list A list of values for the attributes of the relation
     * @param tuple_cost The cost of the tuple
     * @param rel The relation the tuple belongs to
    */
    public Tuple(double[] values_list, double tuple_cost, Relation rel)
    {
        this.values = values_list;
        /*
        // Make a deep copy of the values inside this object
        int length = values_list.size();
        this.values = ArrayList<Integer>();
        for (int i = 0; i < length; i++) this.values.add(values_list.get(i);
        */
        this.cost = tuple_cost;
        this.relation = rel;
    }

    /** 
     * Constructs a new tuple by concatenating a list of other tuples
     * The cost of the new tuple is an aggregation of the other costs
     * @param list_of_tuples A list of tuples that will be merged to create a new one
     * @param rel The relation the tuple belongs to
    */
    public Tuple(List<Tuple> list_of_tuples, Relation rel)
    {
        // First, find the size of the new tuple
        int size = 0;
        for (Tuple t : list_of_tuples)
            size += t.values.length;
        // Now allocate an array of that size
        double[] vals = new double[size];
        // Iterate through the list and add values and costs
        int i = 0;
        double cost = 0;
        for (Tuple t : list_of_tuples)
        {
            cost += t.cost;
            for (double val : t.values)
            {
                vals[i] = val;
                i++;
            }            
        }
        this.values = vals;
        this.cost = cost;
        this.relation = rel;
    }

    
    /** 
     * @return String A string representation of the values of the tuple
     */
    public String valuesToString()
    {
        return Arrays.toString(values).replaceAll("\\[|\\]|,", "");
    }

    
    /** 
     * @return double
     */
    public double get_cost()
    {
        return this.cost;
    }

    
    /** 
     * A tuple is compared with another according to their associated cost
     * @param other
     * @return int
     */
    @Override
    public int compareTo(Tuple other)
    {
        // compareTo should return < 0 if this is supposed to be less than other, 
        // > 0 if this is supposed to be greater than other  
        // and 0 if they are supposed to be equal
        if (this.cost < other.cost) return -1;
        else if (this.cost > other.cost) return 1;
        else return 0;
    }

    
    /** 
     * @return String A string representation of the values of the tuple 
     * together with its cost
     */
    public String flat_format()
    {
        return Arrays.toString(values).replaceAll("\\[|\\]|,", "") + " " + cost;
    }

    /** 
     * @return int
     */
    @Override
    public int hashCode() 
    {
        return Arrays.hashCode(this.values);
    }
    
    /** 
     * A Tuple is equal to another when they agree on the values
     * (but not necessarily the cost)
     * @param o
     * @return boolean
     */
    @Override
    public boolean equals(Object o) 
    {
        if (o == this) return true;
        if (!(o instanceof Tuple)) return false;
        Tuple other_tuple = (Tuple) o;
        return Arrays.equals(this.values, other_tuple.values);
    }

    
    /** 
     * @return String A string representation of the values of the tuple 
     * together with the id of the relation it belongs to
     */
    @Override
    public String toString()
    {
        return relation.relation_id + ":" + Arrays.toString(values); 
        // return Arrays.toString(values).replaceAll("\\[|\\]|,", "") + " " + cost;
    }
}
