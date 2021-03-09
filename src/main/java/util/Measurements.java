package util;

import java.util.ArrayList;
import java.util.List;

import entities.Tuple;

/** 
 * A class that gathers timing and memory measurements during execution.
 * @author Nikolaos Tziavelis
*/
public class Measurements 
{
    /** 
     * To make sure the results are really computed and the compiler doesn't skip anything,
     * we keep a small subset (100) of the most recent results at all times.
     * At the end of the computation {@link #print} adds the values of the tuples of the small result set and prints it. 
    */
    private List<List<Tuple>> results;
    /** 
     * This counter adds up the result values.
    */
    private long dummy_counter;

    /** 
     * For each sampled time point, we record the time.
    */
    private List<Double> time_list;
    /** 
     * For each sampled time point, we record the memory.
    */
    private List<String> memory_list;
    /** 
     * For each sampled time point, we record its rank.
    */
    private List<Integer> k_list;
    
    /** 
     * The sample rate determines how often the measurements will be recorded and stored.
    */
    private int sample_rate;

    private int k, max_k;
    // For timing
    private long startTime;
    private double elapsedTime;

    public Measurements(int sample_rate, int max_k)
    {
        this.time_list = new ArrayList<Double>();
        this.memory_list = new ArrayList<String>();
        this.k_list = new ArrayList<Integer>();

        this.sample_rate = sample_rate;
        this.k = 1;
        this.max_k = max_k;
        this.dummy_counter = 0;
        this.results = new ArrayList<List<Tuple>>(100);
        for (int i = 0; i < 100; i++) this.results.add(new ArrayList<Tuple>());

        // Clear the memory (hopefully)
        System.gc();

        // Initialize time
        this.elapsedTime = 0.0;
        this.startTime = System.nanoTime();
    }

    /** 
     * Records a result.
     * @param result An output tuple as a list of input tuples.
    */
    public void add_k(List<Tuple> result)
    {
        // Add the result to a small result subset, possibly overriding a previous one
        this.results.set(k % 100, result);

        if (k % sample_rate == 0 || k == 1 || k == max_k) 
        {
            elapsedTime += (double) (System.nanoTime() - startTime) / 1_000_000_000.0;
            this.k_list.add(k);
            time_list.add(elapsedTime);
            memory_list.add(get_memory());
            startTime = System.nanoTime();
        }
        k += 1;
    }

    /** 
     * Records a result.
     * @param result An output tuple as a single tuple.
    */
    public void add_k(Tuple result)
    {
        // We don't want to keep all the solutions, just add the value to a counter
        dummy_counter += result.values[0];

        if (k % sample_rate == 0 || k == 1 || k == max_k) 
        {
            elapsedTime += (double) (System.nanoTime() - startTime) / 1_000_000_000.0;
            this.k_list.add(k);
            time_list.add(elapsedTime);
            memory_list.add(get_memory());
            startTime = System.nanoTime();
        }
        k += 1;
    }

    /** 
     * Records the result of a boolean query (true/false).
     * @param result A boolean value.
    */
    public void add_boolean(boolean result)
    {
        // Add to the counter 0 if false
        // 1 if true
        dummy_counter += result ? 1 : 0;

        if (k % sample_rate == 0 || k == 1 || k == max_k) 
        {
            elapsedTime += (double) (System.nanoTime() - startTime) / 1_000_000_000.0;
            this.k_list.add(k);
            time_list.add(elapsedTime);
            memory_list.add(get_memory());
            startTime = System.nanoTime();
        }
        k += 1;
    }

    /** 
     * Prints out (in the standard output) the measurements that have been sampled from the execution.
    */
    public void print()
    {
        // Add the results to the dummy counter so that we are sure that they are computed
        for (List<Tuple> result : this.results)
        {
            for (Tuple t : result) dummy_counter += t.values[0];
        }
            
        // Print out the information stored in the lists of the class
        for (int i = 0; i < time_list.size(); i++)
        {
            System.out.println("k= " + k_list.get(i) +" Time= " + time_list.get(i) + " sec");
            System.out.println(memory_list.get(i));
        }
        System.out.println("Dummy counter = " + dummy_counter);
    }

    /** 
     * Prints out (in the standard output) in csv format
     * the measurements that have been sampled from the execution.
    */
    public void print_to_csv()
    {
        // Add the results to the dummy counter so that we are sure that they are computed
        for (List<Tuple> result : this.results)
        {
            for (Tuple t : result) dummy_counter += t.values[0];
        }
            
        System.out.println("ResultNumber Time(sec)");
        // Print out the information stored in the lists of the class
        for (int i = 0; i < time_list.size(); i++)
        {
            System.out.println(k_list.get(i) + " " + time_list.get(i));
        }
        System.err.println("Dummy counter = " + dummy_counter);
    }

    /** 
     * Helper function that records the state of the memory.
     * @return A string that lists the memory usage.
    */
    private String get_memory()
    {
        Runtime rt = Runtime.getRuntime();
        long total = rt.totalMemory();
        long free = rt.freeMemory();
        long used = total - free;
        return "TotalMem: " + total + " FreeMem: " + free + " UsedMem: " + used + " (Bytes)";
    }
}
