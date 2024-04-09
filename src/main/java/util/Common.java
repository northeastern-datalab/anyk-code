package util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.javatuples.Pair;

import entities.Join_Predicate;
import entities.State_Node;
import entities.Tuple;
import entities.paths.DP_Solution;
import entities.trees.TDP_Solution;

/** 
 * This class contains static methods that are generally useful and may be used by multiple other classes.
 * @author Nikolaos Tziavelis
*/
public class Common
{
    public static String solution_to_output_string(DP_Solution solution)
    {
        return solution.solutionToTuples_strict_order().stream().map(i -> i.toString()).collect(Collectors.joining(";")) + "|" + solution.get_cost();
    }

    public static String solution_to_output_string(TDP_Solution solution)
    {
        return solution.solutionToTuples_strict_order().stream().map(i -> i.toString()).collect(Collectors.joining(";")) + "|" + solution.get_cost();
    }

    public static boolean is_conjunction_of_simple_equalities(List<List<Join_Predicate>> join_condition)
    {
        // Check if it is 1 conjunction
        if (join_condition.size() != 1) return false;
        for(Join_Predicate p : join_condition.get(0))
        {
            // Check if all the predicates are equalities
            if (!p.type.equals("E")) return false;
            // Check if the equality has an offset
            if (p.parameter != null && p.parameter != 0) return false;
        }
        return true;
    }

    /**
     * The element at index pivot_idx acts as a pivot.
     * Rearranges a[low, high] and returns the new position new_pivot_idx of the original pivot element.
     * In the rearranged array, all the elements smaller/larger than pivot appear before/after new_pivot_idx.
     * Based on the Lomuto partitioning scheme https://www.geeksforgeeks.org/hoares-vs-lomuto-partition-scheme-quicksort/
    **/
    public static <E extends Comparable<E>> int partition(List<E> a, int pivot_idx, int low, int high) 
    {
        E pivot = a.get(pivot_idx);
        // This scheme typically assumes that the pivot is the last element
        // so here we swap the chosen (random) pivot with the high index and proceed as usual    
        Collections.swap(a, high, pivot_idx);
        
        // Index of smaller element 
        int i = low - 1; 
      
        for (int j = low; j <= high- 1; j++) 
        { 
            // If current element is smaller than or equal to pivot 
            if (a.get(j).compareTo(pivot) <= 0) 
            { 
                i++; // increment index of smaller element 
                Collections.swap(a, i, j);
            } 
        } 
        Collections.swap(a, i + 1, high);
        return (i + 1); 
    } 

    /** 
     * Given an array of doubles, returns only the elements located at the specified indices.
     * Creates a new array.
     * @param oldArray The array to copy from.
     * @param indices The indexes of the array to be kept.
     * @return An ArrayList that contains only the specified elements.
    */
    public static double[] createSubarray(double[] oldArray, int[] indices)
    {
        double[] newArr = new double[indices.length];
        for (int i = 0; i < indices.length; i++)
            newArr[i] = oldArray[indices[i]]; 
        return newArr;
    }

    /** 
     * Given an array of doubles, returns only the elements located at the specified indices.
     * Creates a new list but doesn't perform a deep copy of the elements.
     * @param oldArray The array to copy from.
     * @param indices The indexes of the array to be kept.
     * @return An ArrayList that contains only the specified elements.
    */
    public static ArrayList<Double> createSublist(double[] oldArray, int[] indices)
    {
        ArrayList<Double> newList = new ArrayList<Double>();
        for (int i = 0; i < indices.length; i++)
                newList.add(oldArray[indices[i]]); //Adds specified indices to new list
        return newList;
    }

    /** 
     * Given an array of integers, returns only the elements located at the specified indices.
     * Creates a new list but doesn't perform a deep copy of the elements.
     * @param oldArray The array to copy from.
     * @param indices The indexes of the array to be kept.
     * @return An ArrayList that contains only the specified elements.
    */
    public static ArrayList<Integer> createSublist(int[] oldArray, int[] indices)
    {
        ArrayList<Integer> newList = new ArrayList<Integer>();
        for (int i = 0; i < indices.length; i++)
                newList.add(oldArray[indices[i]]); //Adds specified indices to new list
        return newList;
    }

    /** 
     * Binary search for the maximum element that is less than a max value
     * on a stage of (T-)DP that corresponds to the tuples of a relation 
     * (i.e., @link{entities.paths.State_Node#state_info} is @link{entities.Tuple}).
     * The attribute whose value we are searching for is specified by its index.
     * @param stage The (sorted) list of nodes to be searched.
     * @param attr_index The index of the attributes that will be used for searching.
     * @param max_val The maximum value we are searching for.
     * @param inclusive Indicates whether we want values equal to max_val to be returned.
     * @return The largest index whose value is less than (or equal to if inclusive) to the specified maximum value 
     *          or -1 if no element satisfies the requirement.
     */
    public static int binary_search_max(List<? extends State_Node> stage, int attr_index, double max_val, boolean inclusive) 
    {
        double current_val;
        int comparison;
        if (inclusive) comparison = 1;
        else comparison = 0;
        int res = -1;
        int low = 0;
        int high = stage.size() - 1;
        while (low <= high) 
        {
            int mid = (low + high) / 2;
            current_val = stage.get(mid).toTuple().values[attr_index];
            // current_val > max_val for inclusive
            // current_val >= max_val for non-inclusive
            // Go lower
            if (Double.compare(current_val, max_val) >= comparison)
            {
                high = mid - 1;
            }
            // If this is the last element, then we don't need to check the next one
            // next_val > max_val for inclusive
            // next_val >= max_val for non-inclusive
            else if (mid == stage.size() - 1 || 
                    Double.compare(stage.get(mid + 1).toTuple().values[attr_index], max_val) >= comparison)
            {
                res = mid;
                break;
            }
            // Otherwise we need to go higher to make the next value larger
            else
            {
                low = mid + 1;
            }
        }
        return res;
    }

    /** 
     * Binary search for the minimum element that is greater than a min value
     * on a stage of (T-)DP that corresponds to the tuples of a relation 
     * (i.e., @link{entities.paths.State_Node#state_info} is @link{entities.Tuple}).
     * The attribute whose value we are searching for is specified by its index.
     * @param stage The (sorted) list of nodes to be searched.
     * @param attr_index The index of the attributes that will be used for searching.
     * @param min_val The minimum value we are searching for.
     * @param inclusive Indicates whether we want values equal to min_value to be returned.
     * @return The smallest index whose value is greater than (or equal to if inclusive) to the specified minimum value
     *          or -1 if no element satisfies the requirement.
     */
    public static int binary_search_min(List<? extends State_Node> stage, int attr_index, double min_val, boolean inclusive) 
    {
        double current_val;
        int comparison;
        if (inclusive) comparison = -1;
        else comparison = 0;
        int res = -1;
        int low = 0;
        int high = stage.size() - 1;
        while (low <= high) 
        {
            int mid = (low + high) / 2;
            current_val = stage.get(mid).toTuple().values[attr_index];
            // current_val < min_val for inclusive
            // current_val <= min_val for non-inclusive
            // Go higher
            if (Double.compare(current_val, min_val) <= comparison)
            {
                low = mid + 1;
            }
            // If this is the first element, then we don't need to check the previous one
            // previous_val < min_val for inclusive
            // previous_val <= min_val for non-inclusive
            else if (mid == 0 || 
                    Double.compare(stage.get(mid - 1).toTuple().values[attr_index], min_val) <= comparison)
            {
                res = mid;
                break;
            }
            // Otherwise we need to go lower to make the previous value lower
            else
            {
                high = mid - 1;
            }
        }
        return res;
    }

    /** 
     * Sorts the nodes of a stage of (T-)DP that corresponds to the tuples of a relation 
     * (i.e., @link{entities.paths.State_Node#state_info} is @link{entities.Tuple}).
     * The attribute for sorting is specified by its index.
     * @param stage The stage to be sorted.
     * @param attr_index The index of the attributes that will be used for sorting.
     */
    public static void sort_stage(List<? extends State_Node> stage, int attr_index)
    {
        // Replace the sorting comparator with one that compares the attribute we want
        Collections.sort(stage, new Comparator<State_Node>() {
            @Override
            public int compare(State_Node node1, State_Node node2) 
            {
                return Double.valueOf(node1.toTuple().values[attr_index]).compareTo(node2.toTuple().values[attr_index]);           
            }
        });
    }

    /** 
     * Hashes the nodes of a stage of (T-)DP that corresponds to the tuples of a relation 
     * (i.e., @link{entities.paths.State_Node#state_info} is @link{entities.Tuple}).
     * The key is a list of attributes of the tuples that are associated with the nodes.
     * @param stage The stage to be hashed.
     * @param join_attributes The indexes of the attributes that will be used as the key.
     * @return HashMap<List<Double>, List<State_Node>>
     */
    public static HashMap<List<Double>, List<State_Node>> hash_stage(List<? extends State_Node> stage, int[] join_attributes)
    {
        Tuple tup;
        List<Double> join_values;
        List<State_Node> node_list_same_key;

        // Each entry in the hashtable is a list of nodes
        // whose associated tuples share the same join attribute values
        HashMap<List<Double>, List<State_Node>> hash = 
            new HashMap<List<Double>, List<State_Node>>();
        for (State_Node node : stage)
        {
            tup = (Tuple) node.state_info; // get the tuple associated with the DP state
            join_values = Common.createSublist(tup.values, join_attributes);
            node_list_same_key = hash.get(join_values);
            if (node_list_same_key == null)
            {
                // Key not present in hash table
                // Initialize a list with the current node and add it to the hash table
                node_list_same_key = new ArrayList<State_Node>();
                node_list_same_key.add(node);
                hash.put(join_values, node_list_same_key);
            }
            else
            {
                // We have already hashed a node associated with a tuple with the same join attribute values
                // Just add the new node to the list
                node_list_same_key.add(node);
            }
        } 
        return hash;
    }

    /** 
     * For 2 stages of (T-)DP that correspond to the tuples of relations,
     * splits the left-right tuples into multiple partitions according to the inequality condition attributes.
     * The left and right tuples have to be sorted beforehand and the number of distinct values has to be provided.
     * The resulting partitions satisfy the following:
     * <ul>
     * <li>Each partition has about the same number of distinct values.
     * <li>The partitions are sorted both locally and globally.
     * </ul>
     * The resulting list doesn't allocate new lists of nodes.
     * Instead, views of the original list as sublists are used.
     * @param left The first (sorted) subset of a relation/stage.
     * @param right The second (sorted) subset of a relation/stage.
     * @param inequality A single inequality predicate.
     * @param no_partitions The desired number of partitions.
     * @param max_values_per_partition The desired distinct values contained in each partition. 
     *                                  Any remaining ones will be assigned to the last partition.
     * @return List<Pair<List<State_Node>, List<State_Node>>> A partitioning of the nodes as a list of left-right pairs.
     */
    public static List<Pair<List<? extends State_Node>,List<? extends State_Node>>> 
        split_by_distinct(List<? extends State_Node> left, List<? extends State_Node> right, Join_Predicate inequality, int no_partitions, int max_values_per_partition)
    {
        List<Pair<List<? extends State_Node>,List<? extends State_Node>>> res = new ArrayList<Pair<List<? extends State_Node>,List<? extends State_Node>>>();

        int idx_left = inequality.attr_idx_1;
        int idx_right = inequality.attr_idx_2;
        double right_val_offset = inequality.parameter;

        // Indexes for left and right relation
        int i = 0, j = 0;
        // Starting indexes for each partition
        int pstart_left = 0, pstart_right = 0;
        // The next values indicate the values of the tuples that we are about to access
        // Will be null when we arrive at the end
        Double next_val_left = null, next_val_right = null;
        if (left.size() > 0) next_val_left = left.get(0).toTuple().values[idx_left];
        if (right.size() > 0) next_val_right = right.get(0).toTuple().values[idx_right] + right_val_offset;
        int current_distinct_cnt = 0;
        Double last_val = null;
        List<? extends State_Node> left_part, right_part;
        boolean read_left;

        // Create all but the last partition via one scan of the nodes
        while (true)
        {
            // Decide if we read from the left or right
            if (next_val_left == null && next_val_right == null) break;
            else if (next_val_right == null) read_left = true;
            else if (next_val_left == null) read_left = false;
            else if (Double.compare(next_val_left, next_val_right) < 0) read_left = true;
            else read_left = false;

            // Check if a new distinct value is about to be accesed
            if (last_val == null || (read_left && !next_val_left.equals(last_val))
                || (!read_left && !next_val_right.equals(last_val))) 
            {
                // Check if we reached the maximum distinct values for the current partition
                if (current_distinct_cnt == max_values_per_partition)
                {
                    // Add the current partition to our result
                    left_part = left.subList(pstart_left, i);
                    right_part = right.subList(pstart_right, j);
                    res.add(new Pair<List<? extends State_Node>,List<? extends State_Node>>(left_part, right_part));
                    // Reset the counter of distinct values
                    current_distinct_cnt = 0;
                    // Set the start of the next partition
                    pstart_left = i; 
                    pstart_right = j;

                    // End the loop when all but the last partition have been created
                    if (res.size() == no_partitions - 1) break;
                }      
                // Increment the counter of distinct values    
                current_distinct_cnt += 1;     
            }

            if (read_left)
            {
                // Include the left element in the current partition
                last_val = next_val_left;
                i += 1;
                if (i < left.size()) next_val_left = left.get(i).toTuple().values[idx_left];
                else next_val_left = null;
            }
            else
            {
                // Include the right element in the current partition
                last_val = next_val_right;     
                j += 1; 
                if (j < right.size()) next_val_right = right.get(j).toTuple().values[idx_right] + right_val_offset;
                else next_val_right = null;
            }
        }
        // Handle the last partition
        left_part = left.subList(pstart_left, left.size());
        right_part = right.subList(pstart_right, right.size());
        res.add(new Pair<List<? extends State_Node>,List<? extends State_Node>>(left_part, right_part));

        return res;
    }

    /** 
     * Given two stages of (T-)DP that correspond to relations, 
     * counts the number of distinct values of the tuples.
     * The attributes whose values we want to count are specified by their indexes.
     * We also allow a real number to be added as an offset to the values of one of the stages.
     * @param stage1 The first stage.
     * @param stage2 The second stage.
     * @param idx1 The index of the attribute whose values we count for the first stage.
     * @param idx2 The index of the attribute whose values we count for the second stage.
     * @param offset2 An offset to be added to the values of the second stage.
     */
    //TODO: make this more efficient by assuming that the stages have already been sorted
    public static int count_distinct_vals(List<? extends State_Node> stage1, List<? extends State_Node> stage2, int idx1, int idx2, double offset2)
    {
        // Hash both stages on their corresponding attributes
        Set<Double> hash = new HashSet<Double>();
        for (State_Node node1 : stage1) hash.add(node1.toTuple().values[idx1]);
        for (State_Node node2 : stage2) hash.add(node2.toTuple().values[idx2] + offset2);

        // Count the number of elements in the hash set
        return hash.size();
    }

    public static String[] concatenate_string_arrays(String[] first, String[] second)
    {
        String[] both = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, both, first.length, second.length);
        return both;
    }

    public static double[] concatenate_double_arrays(double[] first, double[] second)
    {
        double[] both = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, both, first.length, second.length);
        return both;
    }

    public static int[] concatenate_int_arrays(int[] first, int[] second)
    {
        int[] both = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, both, first.length, second.length);
        return both;
    }

    // from inclusive, to exclusive
    public static int[] int_range(int from, int to)
    {
        int[] res = new int[to - from];
        for (int i = 0; i < to - from; i++) res[i] = i + from;
        return res;
    }
}