package algorithms;
import java.util.ArrayList;
import java.util.List;

import entities.Join_Predicate;
import entities.Relation;
import entities.Tuple;
import entities.paths.Path_ThetaJoin_Query;

public class Naive_For_Verification 
{
    public static ArrayList<ArrayList<Tuple>> produce_all_result_tuples_4_onebranch_2attrs(List<Relation> database)
    {
        ArrayList<Tuple> result;
        ArrayList<ArrayList<Tuple>> ret = new ArrayList<ArrayList<Tuple>>();
        for (Tuple t1 : database.get(0).tuples)
        {
            for (Tuple t2 : database.get(1).tuples)
            {
                for (Tuple t3 : database.get(2).tuples)
                {
                    for (Tuple t4 : database.get(3).tuples)
                    {
                        if (t1.values[1] == t2.values[0] && t2.values[1] == t3.values[0] && t2.values[0] == t4.values[0])
                        {
                            result = new ArrayList<Tuple>();
                            result.add(t1);
                            result.add(t2);
                            result.add(t3);
                            result.add(t4);
                            ret.add(result);
                        }
                    }
                }
            }
        }
        return ret;
    }

    public static ArrayList<ArrayList<Tuple>> produce_all_result_tuples_4_path_2attrs(List<Relation> database)
    {
        ArrayList<Tuple> result;
        ArrayList<ArrayList<Tuple>> ret = new ArrayList<ArrayList<Tuple>>();
        for (Tuple t1 : database.get(0).tuples)
        {
            for (Tuple t2 : database.get(1).tuples)
            {
                for (Tuple t3 : database.get(2).tuples)
                {
                    for (Tuple t4 : database.get(3).tuples)
                    {
                        if (t1.values[1] == t2.values[0] && t2.values[1] == t3.values[0] && t3.values[1] == t4.values[0])
                        {
                            result = new ArrayList<Tuple>();
                            result.add(t1);
                            result.add(t2);
                            result.add(t3);
                            result.add(t4);
                            ret.add(result);
                        }
                    }
                }
            }
        }
        return ret;
    }

    public static ArrayList<ArrayList<Tuple>> produce_all_result_tuples_6_path_2attrs(List<Relation> database)
    {
        ArrayList<Tuple> result;
        ArrayList<ArrayList<Tuple>> ret = new ArrayList<ArrayList<Tuple>>();
        for (Tuple t1 : database.get(0).tuples)
        {
            for (Tuple t2 : database.get(1).tuples)
            {
                for (Tuple t3 : database.get(2).tuples)
                {
                    for (Tuple t4 : database.get(3).tuples)
                    {
                        for (Tuple t5 : database.get(4).tuples)
                        {
                            for (Tuple t6 : database.get(5).tuples)
                            {
                                if (t1.values[1] == t2.values[0] && t2.values[1] == t3.values[0] && t3.values[1] == t4.values[0] &&
                                    t4.values[1] == t5.values[0] && t5.values[1] == t6.values[0])
                                {
                                    result = new ArrayList<Tuple>();
                                    result.add(t1);
                                    result.add(t2);
                                    result.add(t3);
                                    result.add(t4);
                                    result.add(t5);
                                    result.add(t6);
                                    ret.add(result);
                                }
                            }
                        }
                    }
                }
            }
        }
        return ret;
    }

    public static ArrayList<ArrayList<Tuple>> produce_all_result_tuples_4_star_2attrs(List<Relation> database)
    {
        ArrayList<Tuple> result;
        ArrayList<ArrayList<Tuple>> ret = new ArrayList<ArrayList<Tuple>>();
        for (Tuple t1 : database.get(0).tuples)
        {
            for (Tuple t2 : database.get(1).tuples)
            {
                for (Tuple t3 : database.get(2).tuples)
                {
                    for (Tuple t4 : database.get(3).tuples)
                    {
                        if (t1.values[0] == t2.values[0] && t1.values[0] == t3.values[0] && t1.values[0] == t4.values[0])
                        {
                            result = new ArrayList<Tuple>();
                            result.add(t1);
                            result.add(t2);
                            result.add(t3);
                            result.add(t4);
                            ret.add(result);
                        }
                    }
                }
            }
        }
        return ret;
    }

    public static ArrayList<ArrayList<Tuple>> produce_all_result_tuples_6_star_2attrs(List<Relation> database)
    {
        ArrayList<Tuple> result;
        ArrayList<ArrayList<Tuple>> ret = new ArrayList<ArrayList<Tuple>>();
        for (Tuple t1 : database.get(0).tuples)
        {
            for (Tuple t2 : database.get(1).tuples)
            {
                for (Tuple t3 : database.get(2).tuples)
                {
                    for (Tuple t4 : database.get(3).tuples)
                    {
                        for (Tuple t5 : database.get(4).tuples)
                        {
                            for (Tuple t6 : database.get(5).tuples)
                            {
                                if (t1.values[0] == t2.values[0] && t1.values[0] == t3.values[0] && t1.values[0] == t4.values[0]
                                        && t1.values[0] == t5.values[0] && t1.values[0] == t6.values[0])
                                {
                                    result = new ArrayList<Tuple>();
                                    result.add(t1);
                                    result.add(t2);
                                    result.add(t3);
                                    result.add(t4);
                                    result.add(t5);
                                    result.add(t6);
                                    ret.add(result);
                                }
                            }
                        }
                    }
                }
            }
        }
        return ret;
    }

    public static ArrayList<Tuple> produce_all_result_tuples_4_cycle_2attrs(List<Relation> database)
    {
        ArrayList<Tuple> result;
        ArrayList<Tuple> ret = new ArrayList<Tuple>();
        Tuple tup;
        for (Tuple t1 : database.get(0).tuples)
        {
            for (Tuple t2 : database.get(1).tuples)
            {
                for (Tuple t3 : database.get(2).tuples)
                {
                    for (Tuple t4 : database.get(3).tuples)
                    {
                        if (t1.values[1] == t2.values[0] && t2.values[1] == t3.values[0] && t3.values[1] == t4.values[0] && t4.values[1] == t1.values[0])
                        {
                            result = new ArrayList<Tuple>();
                            result.add(t1);
                            result.add(t2);
                            result.add(t3);
                            result.add(t4);
                            tup = new Tuple(result, null);
                            ret.add(tup);
                        }
                    }
                }
            }
        }
        return ret;
    }

    public static ArrayList<Tuple> produce_all_result_tuples_6_cycle_2attrs(List<Relation> database)
    {
        ArrayList<Tuple> result;
        ArrayList<Tuple> ret = new ArrayList<Tuple>();
        Tuple tup;
        for (Tuple t1 : database.get(0).tuples)
        {
            for (Tuple t2 : database.get(1).tuples)
            {
                for (Tuple t3 : database.get(2).tuples)
                {
                    for (Tuple t4 : database.get(3).tuples)
                    {
                        for (Tuple t5 : database.get(4).tuples)
                        {
                            for (Tuple t6 : database.get(5).tuples)
                            {
                                if (t1.values[1] == t2.values[0] && t2.values[1] == t3.values[0] && t3.values[1] == t4.values[0] && t4.values[1] == t5.values[0]
                                    && t5.values[1] == t6.values[0] && t6.values[1] == t1.values[0])
                                {
                                    result = new ArrayList<Tuple>();
                                    result.add(t1);
                                    result.add(t2);
                                    result.add(t3);
                                    result.add(t4);
                                    result.add(t5);
                                    result.add(t6);
                                    tup = new Tuple(result, null);
                                    ret.add(tup);
                                }
                            }
                        }
                    }
                }
            }
        }
        return ret;
    }   

    public static ArrayList<ArrayList<Tuple>> produce_all_result_tuples_path_theta(Path_ThetaJoin_Query q)
    {
        if (q.length == 2) return produce_all_result_tuples_2path_theta(q.relations, q.join_conditions);
        else if (q.length == 3) return produce_all_result_tuples_3path_theta(q.relations, q.join_conditions);
        else if (q.length == 4) return produce_all_result_tuples_4path_theta(q.relations, q.join_conditions);
        else
        {
            System.err.println("Cannot handle a path of this length.");
            System.exit(1);
        }
        return null;
    }

    public static boolean condition_satisfied(Tuple t1, Tuple t2, List<List<Join_Predicate>> cond_dnf)
    {
        boolean res = true;
        for (List<Join_Predicate> conjunction : cond_dnf)
        {
            res = true;
            for (Join_Predicate p : conjunction)
            {
                if (!p.satisfied_by(t1, t2)) res = false;
            }
            if (res) break;
        }        
        return res;
    }

    public static ArrayList<ArrayList<Tuple>> produce_all_result_tuples_2path_theta(List<Relation> database, List<List<List<Join_Predicate>>> conds)
    {
        ArrayList<Tuple> result;
        ArrayList<ArrayList<Tuple>> ret = new ArrayList<ArrayList<Tuple>>();
        for (Tuple t1 : database.get(0).tuples)
        {
            for (Tuple t2 : database.get(1).tuples)
            {
                if (condition_satisfied(t1, t2, conds.get(0)))
                {
                    result = new ArrayList<Tuple>();
                    result.add(t1);
                    result.add(t2);
                    ret.add(result);
                }
            }
        }
        return ret;
    }

    public static ArrayList<ArrayList<Tuple>> produce_all_result_tuples_3path_theta(List<Relation> database, List<List<List<Join_Predicate>>> conds)
    {
        ArrayList<Tuple> result;
        ArrayList<ArrayList<Tuple>> ret = new ArrayList<ArrayList<Tuple>>();
        for (Tuple t1 : database.get(0).tuples)
        {
            for (Tuple t2 : database.get(1).tuples)
            {
                for (Tuple t3 : database.get(2).tuples)
                {
                    if (condition_satisfied(t1, t2, conds.get(0)) && condition_satisfied(t2, t3, conds.get(1)))
                    {
                        result = new ArrayList<Tuple>();
                        result.add(t1);
                        result.add(t2);
                        result.add(t3);
                        ret.add(result);
                    }
                }
            }
        }
        return ret;
    }

    public static ArrayList<ArrayList<Tuple>> produce_all_result_tuples_4path_theta(List<Relation> database, List<List<List<Join_Predicate>>> conds)
    {
        ArrayList<Tuple> result;
        ArrayList<ArrayList<Tuple>> ret = new ArrayList<ArrayList<Tuple>>();
        for (Tuple t1 : database.get(0).tuples)
        {
            for (Tuple t2 : database.get(1).tuples)
            {
                for (Tuple t3 : database.get(2).tuples)
                {
                    for (Tuple t4 : database.get(3).tuples)
                    {
                        if (condition_satisfied(t1, t2, conds.get(0)) && 
                            condition_satisfied(t2, t3, conds.get(1)) &&
                            condition_satisfied(t3, t4, conds.get(2)))
                        {
                            result = new ArrayList<Tuple>();
                            result.add(t1);
                            result.add(t2);
                            result.add(t3);
                            result.add(t4);
                            ret.add(result);
                        }
                    }
                }
            }
        }
        return ret;
    }
}
