package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import entities.Relation;
import entities.Tuple;

/** 
 * A class that reads a database as a list of relations from an input file.
 * Expected input format:
 * <br><br>
 * Relation [RelationName]
 * <br>
 * [Attribute1] [Attribute2] [Attribute3] ...
 * <br>
 * [val1] [val2] [val3]
 * <br>
 * [val1'] [val2'] [val3']
 * <br>
 * ...
 * <br>
 * End of [RelationName]
 * <br>
 * Relation [RelationName']
 * <br>
 * ...
 * @author Nikolaos Tziavelis
*/
public class DatabaseParser
{
    /** 
     * The index of the weight attribute.
     * By default (when null), it is the last attribute
     */
    Integer weight_attribute_index = null;

    public DatabaseParser(Integer weight_attribute_index)
    {
        this.weight_attribute_index = weight_attribute_index;
    }

    /** 
     * Given the path of an input file as a string, returns a database as a list of relations.
     * @param file_path Path of input file.
     * @return A database.
     */
    public List<Relation> parse_file(String file_path)
    {
        File f = new File(file_path);
        return parse_file(f);
    }

    /** 
     * Given an input stream, returns a database as a list of relations.
     * @param stream An input stream.
     * @return A database.
     */
    public List<Relation> parse_file(InputStream stream)
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        return parse_file(br);
    }

    /** 
     * Given a file object, returns a database as a list of relations.
     * @param f An input file.
     * @return A database.
     */
    public List<Relation> parse_file(File f)
    {
        List<Relation> database = null;
        FileReader fr = null;
        BufferedReader br = null;
        try 
        {
            // Open file
            fr = new FileReader(f);
            br = new BufferedReader(fr);

            database = parse_file(br); 
        }
        catch (IOException e) 
        {
            e.printStackTrace();
        } 
        finally 
        {
            try 
            {
                if (br != null) br.close();
                if (fr != null) fr.close();
            }
            catch (IOException ex) 
            {
                ex.printStackTrace();
            }
        }              
        return database;
    }

    /** 
     * Given a buffered reader object, returns a database as a list of relations.
     * @param br A buffered reader to read the input from.
     * @return A database.
     */
    public List<Relation> parse_file(BufferedReader br)
    {
        List<Relation> database = new ArrayList<Relation>();

        String sCurrentLine;
        String[] tokens;

        Relation curr_relation = null;
        int curr_attr_no = -1;
        Tuple t;
        String relation_id;
        double[] tuple_vals;
        double tuple_cost;

        try 
        {
            // Read line by line
            while ((sCurrentLine = br.readLine()) != null) 
            {
                // sCurrentLine contains a line of the file as a string
                if (sCurrentLine.startsWith("Relation"))
                {
                    // This line signals a new relation
                    tokens = sCurrentLine.split("\\s+"); // splits by whitespace
                    relation_id = tokens[1];
                    sCurrentLine = br.readLine();
                    tokens = sCurrentLine.split("\\s+"); // splits by whitespace
                    curr_attr_no = tokens.length;
                    curr_relation = new Relation(relation_id, tokens);
                }
                else if (sCurrentLine.startsWith("End"))
                {
                    // This line signals the end of a relation
                    database.add(curr_relation);
                    curr_relation = null;
                }
                else if (curr_relation != null)
                {
                    // This line contains a tuple with its cost
                    tokens = sCurrentLine.split("\\s+"); // splits by whitespace
                    tuple_vals = new double[curr_attr_no];
                    for (int i = 0; i < curr_attr_no; i++)
                        tuple_vals[i] = Double.parseDouble(tokens[i]);
                    if (weight_attribute_index == null)
                        tuple_cost = Double.parseDouble(tokens[curr_attr_no]);
                    else
                        tuple_cost = Double.parseDouble(tokens[weight_attribute_index]);
                    t = new Tuple(tuple_vals, tuple_cost, curr_relation);
                    curr_relation.insert(t);
                }           
            } 
        }
        catch (IOException e) 
        {
            e.printStackTrace();
        } 

        return database;
    }
}