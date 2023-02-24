package entities;

/** 
 * A join predicate is a boolean formula that specifies a condition that needs to hold 
 * in order for 2 tuples (1 from each relation) to be joinable.
 * We assume that all predicates are binary, hence they refer to 1 attribute from each relation.
 * We also allow to a real number to be added to the second attribute for most types of conditions.
 * @author Nikolaos Tziavelis
*/
public class Join_Predicate
{
    /** 
     * The type of the join predicate:
    * <ul>
    * <li>E: Equality of the type A = B + epsilon.</li>
    * <li>IL: Inequality of the type A < B + epsilon.</li>
    * <li>IG: Inequality of the type A > B + epsilon.</li>
    * <li>N: Non-equality of the type A != B + epsilon.<\li>
    * <li>B: Band of the type |A - B| < epsilon.<\li>
    * </ul>
    */
    public String type;
    /** 
     * The indexes of the two attributes in the relations.
    */
    public int attr_idx_1, attr_idx_2;
    /** 
     * Specifies the epsilon parameter.
     * That is the range for band conditions or a real number to be added to the second attribute for the others.
    */
    public Double parameter;

    public Join_Predicate(String type, int attr_idx_1, int attr_idx_2, Double parameter)
    {
        this.type = type;
        this.attr_idx_1 = attr_idx_1;
        this.attr_idx_2 = attr_idx_2;
        if (parameter == null) this.parameter = 0.0;
        else this.parameter = parameter;
    }
    
    /** 
     * Tests if two tuples satisfy this predicate.
     * @param t1 Tuple from the first relation.
     * @param t2 Tuple from the second relation.
     * @return boolean True if the tuples satisfy the predicate, false otherwise.
     */
    public boolean satisfied_by(Tuple t1, Tuple t2)
    {
        double val1 = t1.values[attr_idx_1];
        double val2 = t2.values[attr_idx_2];
        if (this.type.equals("E")) return val1 == (val2 + parameter);
        else if (this.type.equals("IL")) return val1 < (val2 + parameter);
        else if (this.type.equals("IG")) return val1 > (val2 + parameter);
        else if (this.type.equals("N")) return val1 != (val2 + parameter);
        else if (this.type.equals("B")) return Math.abs(val1 - val2) < this.parameter;
        else
        {
            System.err.println("Unknown Predicate");
            System.exit(1);
        }
        return false;
    }

    /** 
     * @return String The predicate in string format
     */
    @Override
    public String toString()
    {
        StringBuilder str = new StringBuilder();
        if (this.type.equals("E"))
        {
            str.append("Idx " + attr_idx_1 + " = Idx " + attr_idx_2 + " ");
            if (parameter != 0) str.append(String.format("%+f", parameter));
        }
        else if (this.type.equals("IL"))
        {
            str.append("Idx " + attr_idx_1 + " < Idx " + attr_idx_2 + " ");
            if (parameter != 0) str.append(String.format("%+f", parameter));
        }
        else if (this.type.equals("IG"))
        {
            str.append("Idx " + attr_idx_1 + " > Idx " + attr_idx_2 + " ");
            if (parameter != 0) str.append(System.out.format("%+f", parameter));
        }
        else if (this.type.equals("N"))
        {
            str.append("Idx " + attr_idx_1 + " != Idx " + attr_idx_2 + " ");
            if (parameter != 0) str.append(System.out.format("%+f", parameter));
        }
        else if (this.type.equals("B"))
        {
            str.append("|Idx " + attr_idx_1 + " - Idx " + attr_idx_2 + "| < " + parameter);
        }
        else
        {
            str.append("UnknownPredicate");
        }
        return str.toString();
    }
}
