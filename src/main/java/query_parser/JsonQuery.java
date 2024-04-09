package query_parser;

import java.io.Serializable;
import java.util.List;

/**
 * This class represents JsonQuery from query.json file
 */
public class JsonQuery implements Serializable {
    private String relation_name;
    private String[] relation_schema;
    private String weight_column;
    private String parent_name;
    private List<List<String>> join_condition;
    private String file_name;

    public String getRelation_name() {
        return relation_name;
    }

    public String[] getRelation_schema() { return relation_schema; }

    public String  getWeight_column() { return weight_column; }

    public String getParent_name() { return parent_name; }

    public List<List<String>> getJoin_condition() { return join_condition; }

    public String getFile_name() { return file_name; }
}


