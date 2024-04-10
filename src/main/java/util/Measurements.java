package util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import entities.Tuple;

/** 
 * A class that gathers timing and memory measurements during execution.
 * @author Nikolaos Tziavelis
*/
public class Measurements 
{
    /** 
     * This counter adds up the result values to make sure they are computed.
    */
    private int dummy_counter;

    /** 
     * For each sampled time point, we record the time.
    */
    private List<List<Double>> time_list;
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

    /** 
     * Keeps track of how many times we have run the algorithm.
    */
    private int runs;
    /** 
     * The number of iterations of the algorithm we have recorded.
    */
    private int record_iters;

    private int k, max_k;
    // For timing
    private long startTime;
    private double elapsedTime;

    public Measurements(int sample_rate, int max_k)
    {
        this.time_list = new ArrayList<List<Double>>();
        this.memory_list = new ArrayList<String>();
        this.k_list = new ArrayList<Integer>();

        this.sample_rate = sample_rate;
        this.k = 1;
        this.max_k = max_k;
        this.dummy_counter = 0;

        this.runs = 1;
        this.record_iters = 0;

        // Clear the memory (hopefully)
        System.gc();

        // Initialize time
        this.elapsedTime = 0.0;
        this.startTime = System.nanoTime();
    }

    /** 
     * Use this method whenever a new ranked enumeration algorithm starts.
    */
    public void start_new_run()
    {
        this.k = 1;
        this.runs += 1;
        this.record_iters = 0;

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
        for (Tuple t : result) dummy_counter += t.values[0];

        if (k % sample_rate == 0 || k == 1 || k == max_k) 
        {
            elapsedTime += (double) (System.nanoTime() - startTime) / 1_000_000_000.0;
            if (this.runs == 1) 
            {
                this.k_list.add(k);
                this.time_list.add(new ArrayList<Double>());
                this.memory_list.add(get_memory());
            }
            this.time_list.get(record_iters).add(elapsedTime);
            
            this.record_iters += 1;
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
        dummy_counter += result.values[0];

        if (k % sample_rate == 0 || k == 1 || k == max_k) 
        {
            elapsedTime += (double) (System.nanoTime() - startTime) / 1_000_000_000.0;
            if (this.runs == 1) 
            {
                this.k_list.add(k);
                this.time_list.add(new ArrayList<Double>());
                this.memory_list.add(get_memory());
            }
            this.time_list.get(record_iters).add(elapsedTime);
            
            this.record_iters += 1;
            startTime = System.nanoTime();
        }
        k += 1;
    }

    /** 
     * Records the result of queries with a single value (boolean or count).
     * @param result An integer that depends on the query output (to make sure the execution it is not compiled away)
    */
    public void add_single_output(int result)
    {
        // Add to the counter 0 if false
        // 1 if true
        dummy_counter += result;

        if (k % sample_rate == 0 || k == 1 || k == max_k) 
        {
            elapsedTime += (double) (System.nanoTime() - startTime) / 1_000_000_000.0;
            if (this.runs == 1) 
            {
                this.k_list.add(k);
                this.time_list.add(new ArrayList<Double>());
                this.memory_list.add(get_memory());
            }
            this.time_list.get(record_iters).add(elapsedTime);
            
            this.record_iters += 1;
            startTime = System.nanoTime();
        }
        k += 1;
    }

    /** 
     * Prints out (in the standard output) the measurements that have been sampled from the execution.
    */
    public void print()
    {          
        // Print out the information stored in the lists of the class
        for (int i = 0; i < time_list.size(); i++)
        {
            System.out.println("k= " + k_list.get(i) +" Time= " + median(time_list.get(i)) + " sec");
            System.out.println(memory_list.get(i));
        }
        System.out.println("(Ignore) Dummy counter = " + dummy_counter);
    }

    /** 
     * Prints out in csv format
     * the measurements that have been sampled from the execution.
    */
    public void print_to_csv_file(String path_to_timings_file) throws IOException
    { 
        OpenCsvWriter openCsv = new OpenCsvWriter(path_to_timings_file);
        openCsv.writeLine("ResultNumber Time(sec)");
        // Print out the information stored in the lists of the class
        for (int i = 0; i < time_list.size(); i++)
        {
            openCsv.writeLine(k_list.get(i) + " " + median(time_list.get(i)));
        }
        System.err.println("(Ignore) Dummy counter = " + dummy_counter);

        openCsv.flushWriter();
        openCsv.closeWriter();
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

    public static double median(List<Double> list) 
    {
        Collections.sort(list);
        if (list.size() % 2 != 0) return list.get(list.size() / 2);
        return (list.get(list.size() / 2) + list.get((list.size() - 1) / 2)) / 2.0;
    }

    /** 
     * Prints out (in the standard output) seomthing to make sure the compiler has not eliminated the code.
    */
    public void consume()
    {          
        // Print out the information stored in the lists of the class
        for (int i = 0; i < time_list.size(); i++)
        {
            dummy_counter += median(time_list.get(i));
            dummy_counter += Double.parseDouble(memory_list.get(i).split(" ")[1]);
            dummy_counter += Double.parseDouble(memory_list.get(i).split(" ")[3]);
            dummy_counter += Double.parseDouble(memory_list.get(i).split(" ")[5]);
        }
        System.out.println("(Ignore) Dummy counter = " + dummy_counter);
    }
}
