package factorization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import entities.Join_Predicate;
import entities.Relation;
import entities.Tuple;
import entities.paths.DP_Decision;
import entities.paths.DP_Path_ThetaJoin_Instance;
import entities.paths.DP_State_Node;
import entities.paths.Path_ThetaJoin_Query;
import entities.trees.Tree_ThetaJoin_Query;
import util.Common;
import util.DatabaseParser;
/** 
 * This class contains methods that convert a theta-join query to an equi-join query.
 * It returns copies of the input relations that contain new columns V1, V2, ...
 * These new columns encode the resulting equi-join.
 * @author Nikolaos Tziavelis
*/
public class Equijoin_Converter 
{
    
    public static List<Relation> convert_to_equijoin_binary_part(Tree_ThetaJoin_Query query, boolean deduplicate_relations)
    {
        int l = query.relations.size();
        // Initially the new relations are the same as the original ones
        List<Relation> res = new ArrayList<Relation>(query.relations);

        for (int r_no = 1; r_no < l; r_no++)
        {
            // Check if relation with index r_no and its parent have join conditions that are not equalities
            if (!Common.is_conjunction_of_simple_equalities(query.join_conditions.get(r_no)))
            {
                // Create a binary join query where the parent is the left relation and the child is the right
                Path_ThetaJoin_Query temp_query = new Path_ThetaJoin_Query(res.get(query.parents.get(r_no)));
                temp_query.insert_wDNF(res.get(r_no), query.join_conditions.get(r_no));

                // Construct the DP instance (graph) of the binary join
                DP_Path_ThetaJoin_Instance dp = new DP_Path_ThetaJoin_Instance(temp_query, "binary_part");
                
                // Now extract the edges of DP into relations
                List<DP_State_Node> left_nodes = new ArrayList<>(res.get(query.parents.get(r_no)).tuples.size());
                for (DP_Decision start_dec : dp.starting_node.decisions.list_of_decisions)
                    left_nodes.add(start_dec.target);

                // Store numbers for middle nodes
                HashMap<DP_State_Node, Integer> mnodes_to_int = new HashMap<DP_State_Node, Integer>(); 
                int counter = 0;

                // Populate the new left relation
                Relation old_left_rel = res.get(query.parents.get(r_no));
                String[] new_left_schema = new String[old_left_rel.schema.length + 1];
                for (int j = 0; j < old_left_rel.schema.length; j++) new_left_schema[j] = old_left_rel.schema[j];
                new_left_schema[old_left_rel.schema.length] = "V" + r_no;
                Relation new_left_relation = new Relation(old_left_rel.relation_id, new_left_schema);
                for (DP_State_Node left_node : left_nodes)
                {
                    for (DP_Decision left_dec : left_node.decisions.list_of_decisions)
                    {
                        DP_State_Node middle_node = left_dec.target;
                        int middle_val;
                        if (mnodes_to_int.get(middle_node) != null)
                        {
                            middle_val = mnodes_to_int.get(middle_node);
                        }
                        else
                        {
                            middle_val = counter;
                            mnodes_to_int.put(middle_node, middle_val);
                            counter += 1;
                        }

                        // Create a new tuple
                        double[] left_vals = ((Tuple) left_node.state_info).values;
                        double[] new_vals = new double[left_vals.length + 1];
                        for (int j = 0; j < left_vals.length; j++) new_vals[j] = left_vals[j];
                        new_vals[left_vals.length] = middle_val;
                        Tuple new_left_tuple = new Tuple(new_vals, 0, new_left_relation);
                        new_left_relation.insert(new_left_tuple);
                    }
                }

                // Populate the new right relation
                Relation old_right_rel = res.get(r_no);
                String new_right_id;
                if (deduplicate_relations) new_right_id = old_right_rel.relation_id + r_no + "'";
                else new_right_id = old_right_rel.relation_id + "'";
                String[] new_right_schema = new String[old_right_rel.schema.length + 1];
                for (int j = 0; j < old_right_rel.schema.length; j++) new_right_schema[j] = old_right_rel.schema[j];
                new_right_schema[old_right_rel.schema.length] = "V" + r_no;
                Relation new_right_relation = new Relation(new_right_id, new_right_schema);
                for (DP_State_Node middle_node : mnodes_to_int.keySet())
                {
                    for (DP_Decision right_dec : middle_node.decisions.list_of_decisions)
                    {
                        DP_State_Node right_node = right_dec.target;
                        int middle_val = mnodes_to_int.get(middle_node);

                        // Create a new tuple
                        double[] right_vals = ((Tuple) right_node.state_info).values;
                        double[] new_vals = new double[right_vals.length + 1];
                        for (int j = 0; j < right_vals.length; j++) new_vals[j] = right_vals[j];
                        new_vals[right_vals.length] = middle_val;
                        Tuple new_right_tuple = new Tuple(new_vals, 0, new_right_relation);
                        new_right_relation.insert(new_right_tuple);
                    }
                }

                // Update the returned list with the new relations
                res.set(r_no, new_right_relation);
                res.set(query.parents.get(r_no), new_left_relation);
            }
        }
        return res;
    }

    // E.g. 
    // java -cp target/any-k-1.0.jar factorization.Equijoin_Converter -i src/main/resources/reddit -q QR1 -l 3
    public static void main(String args[]) 
    {
        // ======= Parse the command line =======
        Options options = new Options();

        Option input_option = new Option("i", "input", true, "path of input file");
        input_option.setRequired(true);
        options.addOption(input_option);

        Option query_option = new Option("q", "query", true, "query type");
        query_option.setRequired(true);
        options.addOption(query_option);

        Option l_option = new Option("l", "relationNo", true, "number of relations");
        l_option.setRequired(true);
        options.addOption(l_option);

        Option fact_method_option = new Option("fm", "factorization method", true, "can be binary_part|multi_part|shared_ranges");
        fact_method_option.setRequired(false);
        options.addOption(fact_method_option);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;
        try
        {
            cmd = parser.parse(options, args);
        } 
        catch (ParseException e) 
        {
            System.err.println(e.getMessage());
            formatter.printHelp(Thread.currentThread().getStackTrace()[2].getClassName(), options);
            System.exit(1);
        }



        // ======= Initialize parameters =======
        String input_file = null;
        String query_type = cmd.getOptionValue("query");
        String factorization_method = null;
        if (cmd.hasOption("factorization method")) factorization_method = cmd.getOptionValue("factorization method");
        else factorization_method = "binary_part";   // let the classes choose by themselves
        if (!factorization_method.equals("binary_part"))
        {
            System.err.println("Only binary partitioning is currently supported");
            System.exit(1);
        }
        int l = Integer.parseInt(cmd.getOptionValue("relationNo"));



        // Construct query and database
        // Self-join: Add the same relation as many times as needed
        List<Relation> database = null;
        Tree_ThetaJoin_Query query = null;
        if (query_type.equals("QR1"))
        {
            input_file = cmd.getOptionValue("input");
            int weight_attribute = 3;
            DatabaseParser db_parser = new DatabaseParser(weight_attribute);
            database = db_parser.parse_file(input_file);
            // Our graphs have only one relation (edges)
            // Add the same relation as many times as needed to search for paths of length l
            for (int i = 1; i < l; i++) database.add(database.get(0));
            query = new Tree_ThetaJoin_Query();
            query.add_to_tree_wConjunction(database.get(0), 0, -1, null);
            
            List<Join_Predicate> ps = new ArrayList<Join_Predicate>();
            ps.add(new Join_Predicate("E", 1, 0, null));
            ps.add(new Join_Predicate("IL", 2, 2, null));

            query.add_to_tree_wConjunction(database.get(0), 1, 0, ps);
            query.add_to_tree_wConjunction(database.get(0), 2, 1, ps);
        }
        else
        {
            System.err.println("Query not recognized.");
            System.exit(1);
        }
        
        List<Relation> new_relations = convert_to_equijoin_binary_part(query, true);
        for (int i = 0; i < new_relations.size(); i++)
            System.out.println(new_relations.get(i).toString_no_cost());

    }
}
