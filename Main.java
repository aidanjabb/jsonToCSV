import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import org.json.JSONObject;
import org.json.JSONArray;



public class Main {

    // get a list of all keys of the given JSON obj
    private static List<String> getKeys(JSONObject obj) {
        List<String> keys = new ArrayList<>();
        Iterator<String> keyIterator = obj.keys();
        while (keyIterator.hasNext()) {
            keys.add(keyIterator.next());
        }

        return keys;
    }

    // get file contents as JSON
    private static JSONObject getContentAsJSON(String file) {
        String contents = null;
        try {
            contents = new String(Files.readAllBytes(Paths.get(file)));
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            return null;
        }

        JSONObject json = new JSONObject(contents);
        return json;
    }

    // convert string in ISO datetime format ("YYYY-MM-DDThh:mm:ss") to a LocalDateTime obj
    private static LocalDateTime getDateTime(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        LocalDateTime dateTime = null;
        try {
            dateTime = LocalDateTime.parse(date, formatter);
        } catch (DateTimeParseException e) {
            System.err.println("Error converting from str to LocalDataTime: " + e.getMessage());
        }

        return dateTime;
    }

    public static void main(String[] args) {
        String dir = args[0];
        String filterBy = args[1];

        LocalDateTime filterByDate = getDateTime(filterBy);

        // get keys
        File[] files = new File(dir).listFiles();
        String firstFile = files[0].getAbsolutePath();
        JSONObject json = getContentAsJSON(firstFile);
        JSONObject firstRecord = json.getJSONArray("objects").getJSONObject(0);
        

        // write keys to file as header line
        String filePath = "data.csv";
        List<String> keys = getKeys(firstRecord);
        String line = String.join(",", keys);

        FileWriter fw = null;
        try {
            fw = new FileWriter(filePath);
            fw.write(line);
            fw.append("\n");
            fw.close();
        } catch (IOException e) {
            System.err.println("Error writing header to CSV file: " + e.getMessage());
        }

        // iterate through the files
        for (int i = 0; i < files.length; i++) {
            String file = files[i].getAbsolutePath();
            json = getContentAsJSON(file);
            JSONArray arr = json.getJSONArray("objects");
            // for given file, iterate through all of the data records
            for (int j = 0; j < arr.length(); j++) {
                JSONObject record = arr.getJSONObject(j);
                String endTime = record.get("end_time").toString();
    
                // compare endTime to filterBy date
                LocalDateTime date = getDateTime(endTime);

                // filter out dates based on filterBy param
                if (date.compareTo(filterByDate) >= 0) {
                        continue;
                }

                // add values to StringBuilder one by one
                int size = record.length();
                int k = 0;
                StringBuilder sb = new StringBuilder();
                for (String key : record.keySet()) {
                    String val = record.get(key).toString();
                    sb.append(val);
                    if (k < size - 1) {
                        sb.append(",");
                    }
                }
                sb.append("\n");

                // write to file
                try {
                    fw = new FileWriter(filePath, true);
                    fw.write(sb.toString());
                    fw.close();
                } catch (IOException e) {
                    System.err.println("An error occurred while writing to the CSV file: " + e.getMessage());
                }
            }
        }

        
    }
}
