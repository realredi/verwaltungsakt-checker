import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;


// what about duplicate entries? -> solved: first entry has priority
// what about child->parent value consistency? solved : children completely determine parent value
public class Parser extends Print {
    static final String[] VALID_KEYS = {
            "A.1",
            "A.1.1",
            "A.1.2",
            "A.1.3",
            "A.2",
            "A.2.1",
            "A.2.2"
    };
    static final String[] VALID_VALUES = {
            "y",
            "n",
            "u"
    };
    static final String DELIMITER = "=";
    static List <String> errors = new ArrayList < String > ();
    // *linked* hashmap maybe unnecessary???
    static LinkedHashMap <String, String> file_data = new LinkedHashMap <String, String>();
    // checks basic semantics
    public static void validate(String[] line_split, int line_count) {
        String valid_key = "";
        String valid_value = "";
        // is there a better solution, with while-loop?
        for (int i = 0; i < VALID_KEYS.length; i++) {
            if (line_split[0].equals(VALID_KEYS[i])) {
                valid_key = line_split[0];
                break;
            }
            else if (i == VALID_KEYS.length - 1 && !line_split[0].equals(VALID_KEYS[i])) {  //TODO: sensible construct?
                errors.add("Error in file_data" + ", in line " + line_count + " : Invalid key");
            }
        }
        // convert string array of valid values into a list to access .contains()
        if (Arrays.asList(VALID_VALUES).contains(line_split[1])) {
            valid_value = line_split[1];
        }
        else {
            errors.add("Error in file_data" + ", in line " + line_count + " : Invalid value");
        }
        // parsable & valid -> add to file_data
        // hashmap
        if (!valid_key.isEmpty() && !valid_value.isEmpty()) {
            // checks if key already in file_data
            // 1st entry has priority, good?
            if (file_data.containsKey(valid_key))
            {
                errors.add("Error in file_data" + ", in line " + line_count + " : Key duplicate found");
            }
            else {
                file_data.put(valid_key, valid_value);
            }
        }
    }
    //checks basic syntax
    public static void checkFileContent(String line, int line_count)
    {
        if (!line.isEmpty()){ 	// ignore empty lines, would result in excessive error messages otherwise.
            if (line.contains(DELIMITER)) {
                //split parameter negative: a) doesn't ignore trailing empty strings and b)???
                //int len; // if omitted -> error???
                String[] line_split = line.split(DELIMITER, -1);
                int len = line_split.length;
                // parseable line
                if (len == 2) {
                    if (line_split[0].isEmpty()) {
                        errors.add("Error in file_data, line " + line_count + " : Key not found");
                    }
                    if (line_split[1].isEmpty()) {
                        errors.add("Error in file_data" + ", line " + line_count + " : Value not found");
                    }
                    // if above conditions not met => parseable line (= has a key, delimiter and value).
                    // next step is validating whether parsable line contains valid keys and values
                    // only place where file_data
                    // is modified is in validate()!
                    if (!line_split[0].isEmpty() && !line_split[1].isEmpty()){
                        validate(line_split, line_count);
                    }
                }
                else if (len < 2) {
                    errors.add("Error in file_data" +
                            ", line " + line_count + " : Expected number of elements not found");
                }

                else if (len > 2) {  // condition might look redundant, but this case documents a distinct error type.

                    errors.add("Error in file_data" +
                            ", line " + line_count + " : Expected number of elements exceeded");
                }
            }
            else {
                errors.add("Error in file_data" + ", line " + line_count + " : Delimiter not found");
            }
        }
    }
    public static HashMap <String, String> parse(String path) {
        errors.clear(); // start with an empty list every time parse is called
        int line_count = 0;
        String line;
        File file = new File(path);
        Scanner scanner;
        try {
            scanner = new Scanner(file);
            if (!file.canRead()) {
                p("__file readable__ : false");
                errors.add("file not readable");
            }
            else {
                p("__file readable__ : true");
                while (scanner.hasNext()) {
                    line = scanner.nextLine().replace(" ", "");
                    line_count++;
                    checkFileContent(line, line_count);
                }
                if (file_data.isEmpty()) { //checks if input file  empty/ non-retrievable
                    errors.add("Error in file_data" + ": No data retrieved");
                }
            }
        }
        catch (FileNotFoundException e) {
            errors.add("Error : File not found: \""+path+"\".");
            p("file not found");
        }
        return file_data;
    }
}
