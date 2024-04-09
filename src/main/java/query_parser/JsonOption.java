package query_parser;

public class JsonOption {
    private String result_output_file;
    private String timings_output_file;
    private String algorithm;
    private Integer max_k;
    private Double weight_cutoff;
    private Integer timing_frequency;
    private Integer timing_measurements;
    private Integer estimated_result_size;
    private String factorization_method;
    private String path_optimization;

    public String getResult_Output_File() { return result_output_file; }

    public String getTimings_Output_File() { return timings_output_file; }

    public String getAlgorithm() { return algorithm; }

    public Integer getMax_k() { return max_k; }

    public Double getWeight_cutoff() { return weight_cutoff; }

    public Integer getTiming_frequency() { return timing_frequency; }

    public Integer getTiming_measurements() { return timing_measurements; }

    public Integer getEstimated_result_size() { return estimated_result_size; }

    public String getFactorization_method() { return factorization_method; }

    public String getPath_optimization() { return path_optimization; }
}
