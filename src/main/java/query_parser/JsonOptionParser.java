package query_parser;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.google.gson.Gson;

public class JsonOptionParser {
    Gson gson;
    JsonOption options;
    public JsonOptionParser(String path) {
        gson = new Gson();
        try {
            Reader reader = Files.newBufferedReader(Paths.get(path));
            options = gson.fromJson(reader, JsonOption.class);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public JsonOption getJsonOption() {
        return options;
    }
}
