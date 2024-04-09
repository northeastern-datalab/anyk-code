package query_parser;


import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import entities.Join_Predicate;
import entities.Relation;
import entities.trees.Tree_ThetaJoin_Query;
import util.RelationParser;


/**
 * JsonParserTree consists of end to end functions for running an any-k worker.
 */
public class JsonParserTree {

    Gson gson;
    private JsonInput input;
    private JsonQuery[] query_elements;

    // map relation to a unique id
    private Map<String, List<String>> parent_to_children = new HashMap<>();
    // map each relation to a generated unique id
    private Map<String, Integer> relation_to_id = new HashMap<>();
    // map relation name from json file to Relation object
    private Map<String, Relation> relationName_to_relation = new HashMap<>();


    // TODO: band join
    // map to check join predicate type
    private Map<String, String> join_to_type = new HashMap<>();

    // create a map between relation_name to query element
    private Map<String, JsonQuery> relationName_to_element = new HashMap<>();

    public JsonParserTree(String path) {
        join_to_type.put("=", "E");
        join_to_type.put("<", "IL");
        join_to_type.put(">", "IG");
        join_to_type.put("!=", "N");

        gson = new Gson();
        try {
            Reader reader = Files.newBufferedReader(Paths
                    .get(path));
            input = gson.fromJson(reader, JsonInput.class);
            query_elements = input.query;
            setup();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public JsonParserTree(String path, int dummy) {
        gson = new Gson();
        try {
            Reader reader = Files.newBufferedReader(Paths
                    .get(path));
            input = gson.fromJson(reader, JsonInput.class);
            query_elements = input.query;


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public JsonQuery[] getJsonQueryElements() {
        return query_elements;
    }

    public Map<String, Relation> getRelationName_to_relation() {
        return relationName_to_relation;
    }

    public Map<String, JsonQuery> getRelationName_to_element() {
        return relationName_to_element;
    }

    // return query elements of each relation to spark program
    public Map<String, String> getRelationToFileName() {
        Map<String, String> relation_to_fileName = new HashMap<>();
        for (JsonQuery ele : query_elements) {
            relation_to_fileName.put(ele.getRelation_name(), ele.getFile_name());
        }
        return relation_to_fileName;
    }

    /**
     * Set up data structures for parsing JsonQuery element
     */
    private void setup() {
        // 1st traversal
        // go through each element to create a mapping between parent and its children
        for (JsonQuery ele : query_elements) {
            String relation_name = ele.getRelation_name();
            String parent_name = ele.getParent_name();
            parent_to_children.computeIfAbsent(parent_name, k -> new ArrayList<>()).add(relation_name);
            // populate the map between relation_name to query element
            relationName_to_element.put(relation_name, ele);

            // Create relation object
            Relation r = new Relation(ele.getRelation_name(), ele.getRelation_schema());
            relationName_to_relation.put(ele.getRelation_name(), r);
        }
        assignNodeId();
    }

    /**
     * Traverse JsonQuery to assign each node a unique id
     */
    private void assignNodeId() {
        String root_node = parent_to_children.get("null").get(0);
        // Root node has an id = 0 and its parent id = -1
        int root_id = 0;
        int index = 1; // regular node id starts at 1
        String cur_node;
        relation_to_id.put(root_node, root_id);

        // starting node is "null", the parent of the root node
        List<String> children = parent_to_children.get("null");
        List<String> active_nodes;
        boolean isEmpty = false;

        // 2nd traversal
        // assign unique id for each node in the tree
        while (!isEmpty) {
            active_nodes = new ArrayList<>();
            for (int i = 0; i < children.size(); i++) {
                cur_node = children.get(i); // i = 0, curr_node = R2
                if (parent_to_children.containsKey(cur_node)) {
                    List<String> next_children = parent_to_children.get(cur_node);
                    active_nodes.addAll(next_children);
                }
            }
            for (int i = 0; i < active_nodes.size(); i++) {
                relation_to_id.put(active_nodes.get(i), index++);
            }
            children = active_nodes;
            isEmpty = active_nodes.isEmpty();
        }
    }

    /**
     * Parse join condition from the query.json file
     * @param tree_query
     */
    private void parseJoinConditions(Tree_ThetaJoin_Query tree_query) {
        for (JsonQuery ele : query_elements) {
            int node_id;
            int parent_id;

            // Create join predicate
            // if it is a root node, then set up the node id and its parent id
            if (ele.getParent_name().equals("null")) {
                node_id = relation_to_id.get(ele.getRelation_name());
                parent_id = -1;
                tree_query.add_to_tree_wDNF(relationName_to_relation.get(ele.getRelation_name()),
                        node_id, parent_id, null);
            } else {
                // NOT ROOT NODE
                node_id = relation_to_id.get(ele.getRelation_name());
                parent_id = relation_to_id.get(ele.getParent_name());
                List<List<String>> join_condition = ele.getJoin_condition();

                List<Join_Predicate> conjunctions;
                List<List<Join_Predicate>> disjunctions = new ArrayList<>();

                if (join_condition.size() > 1) {
                    for (List<String> outer_list : join_condition) {
                        conjunctions = new ArrayList<>();
                        // handle disjunction differently
                        for (String inner_list : outer_list) {
                            // parts = ["R-left.to", "=" "R-right.from"]
                            String[] parts = inner_list.split(" ");
                            // left_attr_tokens = ["R-left", "to"]
                            String[] left_attr_tokens = parts[0].split("\\.");
                            // left_attr = "to"
                            String left_attr = left_attr_tokens[1];
                            String parent_name = left_attr_tokens[0];

                            // user_join_type = "="
                            String user_join_type = parts[1];
                            // right_attr_tokens = ["R-right", "from"]
                            String[] right_attr_tokens = parts[2].split("\\.");
                            // right_attr = "from"
                            String right_attr = right_attr_tokens[1];
                            String curr_name = right_attr_tokens[0];

                            // check if the join condition is specified correctly with parent relation name on
                            // the left side and this current relation name on the right side of join type
                            if (!parent_name.equals(ele.getParent_name())
                                    || !curr_name.equals(ele.getRelation_name())) {
                                System.out.println("Incorrect relation name in join condition");
                                System.exit(1);
                            }

                            String join_type = " ";
                            // Convert join symbol to encoded letters
                            if (join_to_type.containsKey(user_join_type)) {
                                join_type = join_to_type.get(user_join_type);
                            } else {
                                System.out.println("There is no join type: " + user_join_type);
                            }
                            // create join predicate for inner list
                            int left_index = List.of((relationName_to_element.get(ele.getParent_name()))
                                    .getRelation_schema()).indexOf(left_attr);
                            int right_index = List.of((relationName_to_element.get(ele.getRelation_name()))
                                    .getRelation_schema()).indexOf(right_attr);

                            Join_Predicate p = new Join_Predicate(join_type, left_index, right_index, null);

                            // System.out.println("join_predicate: " + left_attr + " " + user_join_type + " " + right_attr);
                            conjunctions.add(p);
                        }
                        disjunctions.add(conjunctions);
                    }

                    tree_query.add_to_tree_wDNF(relationName_to_relation.get(ele.getRelation_name()),
                            node_id, parent_id, disjunctions);
//                    System.out.println("**********disjunctions************: " + disjunctions);

                } else {
                    node_id = relation_to_id.get(ele.getRelation_name());
                    parent_id = relation_to_id.get(ele.getParent_name());
                    for (List<String> outer_list : join_condition) {
                        conjunctions = new ArrayList<>();
                        for (String inner_list : outer_list) {
                            String[] parts = inner_list.split(" ");
                            String[] left_attr_tokens = parts[0].split("\\.");
                            String left_attr = left_attr_tokens[1];
                            String parent_name = left_attr_tokens[0];

                            String user_join_type = parts[1];
                            String[] right_attr_tokens = parts[2].split("\\.");
                            String right_attr = right_attr_tokens[1];
                            String curr_name = right_attr_tokens[0];

                            if (!parent_name.equals(ele.getParent_name())
                                    || !curr_name.equals(ele.getRelation_name())) {
                                System.out.println("Incorrect relation name in join condition");
                                System.exit(1);
                            }

                            String join_type = " ";
                            // Convert join symbol to encoded letters
                            if (join_to_type.containsKey(user_join_type)) {
                                join_type = join_to_type.get(user_join_type);

                            } else {
                                System.out.println("There is no join type: " + user_join_type);
                            }
                            // create join predicate for inner list
                            int left_index = List.of((relationName_to_element.get(ele.getParent_name()))
                                    .getRelation_schema()).indexOf(left_attr);
                            int right_index = List.of((relationName_to_element.get(ele.getRelation_name()))
                                    .getRelation_schema()).indexOf(right_attr);

                            Join_Predicate p = new Join_Predicate(join_type, left_index, right_index, null);
                            // System.out.println("join_predicate: " + left_attr + " " + user_join_type + " " + right_attr);
                            conjunctions.add(p);
                        }
                        tree_query.add_to_tree_wConjunction(relationName_to_relation.get(ele.getRelation_name()),
                                node_id, parent_id, conjunctions);
//                        System.out.println("**********conjunctions************: " + conjunctions);
                    }
                }
            }
        }
    }


    // parse query from JSON file
    public Tree_ThetaJoin_Query parseQuery() {

        // construct tree thetajoin query
        Tree_ThetaJoin_Query tree_query = new Tree_ThetaJoin_Query();
        parseJoinConditions(tree_query);

        // populate all relations in the relation list from input file
        for ( Map.Entry<String, Relation> entry: relationName_to_relation.entrySet() ) {
            JsonQuery ele = relationName_to_element.get(entry.getKey());
            // retrieve weight column index
            String weight_column = ele.getWeight_column();
            int weight_index = List.of(ele.getRelation_schema()).indexOf(weight_column);
            RelationParser rParser = new RelationParser(weight_index, entry.getValue());
            // populate relation with tuples from input files using relation parser
            rParser.parse_file(ele.getFile_name());
        }
        return tree_query;
    }
}
