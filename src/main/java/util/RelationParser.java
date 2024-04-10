package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import entities.Relation;
import entities.Tuple;

/**
 * This class parses each input record to create a Relation
 */
public class RelationParser {
    Integer weight_attribute_index = null;

    // relation from JsonParser
    Relation relation;

    public RelationParser(Relation relation)
    {
        this.relation = relation;
    }

    public RelationParser(Integer weight_attribute_index, Relation relation)
    {
        this.relation = relation;
        this.weight_attribute_index = weight_attribute_index;
    }

    public void parse_file(String file_path)
    {
        File f = new File(file_path);
        parse_file(f);
    }


    public void parse_file(File f)
    {
        FileReader fr = null;
        BufferedReader br = null;
        try
        {
            // Open file
            fr = new FileReader(f);
            br = new BufferedReader(fr);

            // populate relation with tuples from input files
            this.relation = parse_file(br);
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
    }

    public Relation parse_file(BufferedReader br)
    {
        String sCurrentLine;
        String[] tokens;
        Tuple t;
        double[] tuple_vals;
        double tuple_cost;

        try
        {
            while ((sCurrentLine = br.readLine()) != null) {
                if (this.relation != null) {
                    // This line contains a tuple with its cost
                    tokens = sCurrentLine.split("\\s+"); // splits by whitespace
                    /*
                     from to weight
                     100 200 0
                     token.length = 3
                     tuple_vals = length 3
                     tokens = [100, 200, 0]
                     weight_attribute_index = 2
                     */
                    tuple_vals = new double[tokens.length];
                    for (int i = 0; i < tuple_vals.length; i++) {
                        tuple_vals[i] =  Double.parseDouble(tokens[i]);
                    }
                    if (this.weight_attribute_index == null)
                        tuple_cost = 0.0;
                    else
                        tuple_cost = Double.parseDouble(tokens[weight_attribute_index]);
                    t = new Tuple(tuple_vals, tuple_cost, this.relation);
                    this.relation.insert(t);
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return this.relation;
    }
}
