package algorithms.trees;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
// import java.util.PriorityQueue;

import algorithms.Configuration;
import entities.trees.Star_Equijoin_Query;
import entities.trees.TDP_Decision;
import entities.trees.TDP_DecisionSet;
import entities.trees.TDP_Problem_Instance;
import entities.trees.TDP_Solution;
import entities.trees.TDP_Star_Equijoin_Instance;

/** 
 * Take2 is a variant of Anyk-Part that implements {@link #get_successors} 
 * by returning 2 successors instead of the single true one.
 * Those 2 successors are determined by a binary heap where each node points to 2 children.
 * The partial order is then the one imposed by the binary heap.
 * Constructing the binary heap in {@link #initialize_partial_order} takes time proportional
 * to the number of elements with a standard heapify().
 * Then we set the appropriate successor pointers and resolve each {@link #get_successors} 
 * call in constant time.
 * @author Nikolaos Tziavelis
*/
public class TDP_Take2 extends TDP_Part
{
	public TDP_Take2(TDP_Problem_Instance inst, Configuration conf)
    {
    	super(inst, conf);
    }
    
    public void initialize_partial_order(TDP_DecisionSet decisions)
    {
        // Initialize the successor lists
        for (TDP_Decision dec : decisions.list_of_decisions)
            dec.successors = new ArrayList<TDP_Decision>();

        // Build a heap with the decisions
        List<TDP_Decision> heap_of_decisions = heapify(decisions.list_of_decisions);

        // Use standard library
        // PriorityQueue<DP_Decision> heap_of_decisions = new PriorityQueue<DP_Decision>(decisions.list_of_decisions);
        // DP_Decision[] arr_heap = heap_of_decisions.toArray(new DP_Decision[heap_of_decisions.size()]);

        // Cast to an array representation
        // assert heap_property(heap_of_decisions);
        // In a binary heap h, the children of the node at h[i] are found at h[(2 * i) + 1] and h[(2 * i) + 2]
        // Thus, the parent of node at h[i] is found at h[(i - 1) / 2]
        // Each parent will have its direct children as its successors
        for (int i = 1; i < heap_of_decisions.size(); i++)
        {
            heap_of_decisions.get((i - 1) / 2).successors.add(heap_of_decisions.get(i));
        }

        // If standard library (array representation is used)
        //for (int i = 1; i < arr_heap.length; i++)
        //{
        //    arr_heap[(i - 1) / 2].successors.add(arr_heap[i]);
        //}

        // Make the root of the heap have only one successor to improve performance
        // Only makes sense if the heap has more than 3 elements

        // Remove for now
        /*
        if (heap_of_decisions.size() >= 3)
        {
            DP_Decision worse_child;
            DP_Decision left_child = heap_of_decisions.get(1);
            DP_Decision right_child = heap_of_decisions.get(2);
            if (left_child.compareTo(right_child) <= 0)
            {
                // Left child (index 1 in the heap, index 0 in successor list) is better
                worse_child = heap_of_decisions.get(0).successors.remove(1);
                left_child.successors.add(worse_child);
            }
            else
            {
                // Right child (index 2 in the heap, index 1 in successor list) is better
                worse_child = heap_of_decisions.get(0).successors.remove(0);
                right_child.successors.add(worse_child);                
            }
        }
        */
    }

    public List<TDP_Decision> get_successors(TDP_Decision dec)
    {
        TDP_DecisionSet decision_set = dec.belongs_to();
        if (!decision_set.partial_order_computed)
        {
            initialize_partial_order(decision_set);
            decision_set.partial_order_computed = true;
        }
        // assert dec.successors.size() <= 2;
        // We can have three successors if we use a unique child for the root node of the heap!
        // assert dec.successors.size() <= 3;
        return dec.successors;
    }

    // Inspired by https://github.com/awangdev/LintCode/blob/master/Java/Heapify.java
    private List<TDP_Decision> heapify(List<TDP_Decision> a) 
    {
        int n = a.size();
        if (n == 0) return a;
        
        int curr = 0, left = 0, right = 0, child = 0;
        
        for (int i = n / 2 - 1; i >= 0; i--) 
        {
            curr = i;
            while (curr * 2 + 1 < n) 
            {
                // pick feasible child. 
                left = curr * 2 + 1;
                right = curr * 2 + 2;
                // Pick nums[left] < nums[right], if later curr < nums[left], then curr < nums[right] as well
                if (right >= n || a.get(left).compareTo(a.get(right)) < 0) 
                    child = left;
                else
                    child = right;
                if (a.get(curr).compareTo(a.get(child)) < 0) // meets min-heap requirement
                    break;
                else
                    Collections.swap(a, curr, child);

                // check all children if applicable
                curr = child;
            }
        }
        return a;
    }
    
/*
    public void initialize_partial_order(DP_DecisionSet decisions)
    {
        // Initialize the successor lists
        for (DP_Decision dec : decisions.list_of_decisions)
            dec.successors = new ArrayList<DP_Decision>();

        // Build a heap with the decisions
        PriorityQueue<DP_Decision> heap_of_decisions = new PriorityQueue<DP_Decision>(decisions.list_of_decisions);
        // Cast to an array representation
        ArrayList<DP_Decision> array_heap = new ArrayList<DP_Decision>(heap_of_decisions);
        // assert heap_property(array_heap);
        // In a binary heap h, the children of the node at h[i] are found at h[(2*i)+1] and h[(2*i)+2]
        // Thus, the parent of node at h[i] is found at h[(i - 1) / 2]
        // Each parent will have its direct children as its successors
        for (int i = 1; i < array_heap.size(); i++)
        {
            array_heap.get((i - 1) / 2).successors.add(array_heap.get(i));
        }
    }

    public void initialize_partial_order(DP_DecisionSet decisions)
    {
        // Initialize the successor lists
        for (DP_Decision dec : decisions.list_of_decisions)
            dec.successors = new ArrayList<DP_Decision>();

        // Build a heap with the decisions
        BHeap<DP_Decision> heap_of_decisions = new BHeap<DP_Decision>(decisions.list_of_decisions);
        // Cast to an array representation
        Object[] array_heap = heap_of_decisions.get_array();
        // assert heap_property(array_heap);
        // In a binary heap h, the children of the node at h[i] are found at h[(2*i)+1] and h[(2*i)+2]
        // Thus, the parent of node at h[i] is found at h[(i - 1) / 2]
        // Each parent will have its direct children as its successors
        for (int i = 2; i < array_heap.length; i++)
        {
            ((DP_Decision) array_heap[i / 2]).successors.add((DP_Decision) array_heap[i]);
        }
    }
*/

    /*
    private boolean heap_property(DP_Decision[] array_heap)
    {
        if (array_heap == null)
        {   // check whether the array_heap is null itself, not the element
            return false; // here we assume null is not a heap (open for discussion)
        }
        for (int i = 1; i < array_heap.length; i++)
        {
            if (array_heap[i].compareTo(array_heap[(i - 1) / 2]) < 0)
            {   // the node is less than its parent
                return false; // we know there is an error, so return false
            }
       }
       return true; // all checks succeeded, return true
    }
    */

    /*
    private boolean heap_property(List<DP_Decision> array_heap)
    {
        if (array_heap == null)
        {   // check whether the array_heap is null itself, not the element
            return false; // here we assume null is not a heap (open for discussion)
        }
        for (int i = 1; i < array_heap.size(); i++)
        {
            if (array_heap.get(i).compareTo(array_heap.get((i - 1) / 2)) < 0)
            {   // the node is less than its parent
                return false; // we know there is an error, so return false
            }
       }
       return true; // all checks succeeded, return true
    }
    */

    /*
    // Function to convert a list to a heap
    private List<DP_Decision> build_heap(List<DP_Decision> a)
    {
        // Build-Heap: Call heapify starting from last internal node all the way upto the root node
        if (a.size() == 0) return a;


        int i = (a.size() - 2) / 2;
        while (i >= 0) 
            heapify(a, i--, a.size());
        return a;
    }
    */

    /*
    // Function to convert a list to a heap
    private List<DP_Decision> build_heap(List<DP_Decision> a)
    {
        // Build-Heap: Call heapify starting from last internal node all the way upto the root node
        int i = (a.size() - 2) / 2;
        while (i >= 0) 
            heapify(a, i--, a.size());
        return a;
    }
    */

    /*
    // Function based on https://www.techiedelight.com/convert-max-heap-min-heap-linear-time/
    // Recursive Heapify-down algorithm
    // the node at index i and its two direct children
    // violates the heap property
    private void heapify(List<DP_Decision> a, int i, int size)
    {
        // get left and right child of node at index i
        int left = 2 * i + 1;
        int right = 2 * i + 2;

        int smallest = i;

        // compare a[i] with its left and right child and find smallest value
        if (left < size && a.get(left).compareTo(a.get(i)) < 0)
            smallest = left;

        if (right < size && a.get(right).compareTo(a.get(smallest)) < 0)
            smallest = right;

        // swap with child having lesser value and call heapify-down on the child
        if (smallest != i) 
        {
            // Swap i with smallest
            Collections.swap(a, i, smallest); 
            heapify(a, smallest, size);
        }
    }
    */

    public static void main(String args[]) 
    {
        // Run the example
        Star_Equijoin_Query example_query = new Star_Equijoin_Query();
        TDP_Star_Equijoin_Instance instance = new TDP_Star_Equijoin_Instance(example_query);
        instance.bottom_up();
        TDP_Anyk_Iterator iter = new TDP_Take2(instance, null);
        
        System.out.println("All solutions:");
        TDP_Solution sol;
        while ((sol = iter.get_next()) != null)
            System.out.println(sol.solutionToString() + "  Cost = " + sol.get_cost());
    }

}