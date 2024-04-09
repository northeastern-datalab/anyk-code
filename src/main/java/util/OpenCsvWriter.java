package util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * A class for writing results in different format to csv files
 */
public class OpenCsvWriter {
    private static final String COMMA = ",";
    private static final String DEFAULT_SEPARATOR = COMMA;
    private static final String DOUBLE_QUOTES = "\"";
    private static final String EMBEDDED_DOUBLE_QUOTES = "\"\"";
    private static final String NEW_LINE_UNIX = "\n";
    private static final String NEW_LINE_WINDOWS = "\r\n";
    private static final String VERTICAL_LINE = "|";

    BufferedWriter bwriter;

    // create csv writer with header
    public OpenCsvWriter(String file_path, String[] headers) throws IOException {

        bwriter = new BufferedWriter(new FileWriter(file_path));
        for (String header : headers) {
            bwriter.write(header);
        }
    }

    // create csv writer without header
    public OpenCsvWriter(String file_path) throws IOException {
        bwriter = new BufferedWriter(new FileWriter(file_path));
    }

    // test writing value to a file
    public void WriteCsvRecord(String solution, String cost) throws IOException {
        // write solution record in a format "solution",cost
        bwriter.write(DOUBLE_QUOTES + solution + DOUBLE_QUOTES + DEFAULT_SEPARATOR + cost + NEW_LINE_UNIX);
    }

    public void WriteCsvRecordWithTimeStamp(String solution, String cost, String timeUTC, String region_key)
            throws IOException {
        // write solution record in a format "solution",cost
        bwriter.write(DOUBLE_QUOTES + solution + DOUBLE_QUOTES +
                VERTICAL_LINE + cost +
                VERTICAL_LINE + timeUTC +
                VERTICAL_LINE + region_key +
                NEW_LINE_UNIX);
    }

    public void writeLine(String line) throws IOException {
        bwriter.write(line + NEW_LINE_UNIX);
    }

    public void writeRegionkeyCount(String region_key, Integer count) throws IOException {
        bwriter.write(region_key + DEFAULT_SEPARATOR + count + NEW_LINE_UNIX);
    }

    public void flushWriter() throws IOException {
        bwriter.flush();
    }

    public void closeWriter() throws IOException {
        bwriter.close();
    }

}
