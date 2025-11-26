import java.util.*;
public class Gen {
    static List <Nodes> nodes = new ArrayList <Nodes> ();
    static List <String> report = new ArrayList <String> ();
    static HashMap <String, String> input = new HashMap <String, String> ();
    static final String[] VALID_KEYS = Parser.VALID_KEYS;
    static final String[] NAMES = {"Verhältnismäßigkeit", "Geeignetheit", "Erforderlichkeit", "Angemessenheit",
            "Form i.e.S", "Rechtsbehelf", "Begründetheit"};
    private Gen() {} // prevents this utility class from being instantiated
    // creates complete dummy set of keys and their values. Later those are set according to filedata.
    public static void setNodes() {
        if (nodes.isEmpty()) {
            for (int i = 0; i < VALID_KEYS.length; i++) {
                Nodes node = new Nodes();
                node.key = VALID_KEYS[i];
                node.name = NAMES[i];
                // set default value to "u" (undefined), later will be reset according to filedata
                node.value = "u";
                node.parent = false;
                nodes.add(node);
            }
        }
    }
    // sets parents AND adds children to parents.children
    public static void setParents() {
        for (Nodes parentCand: nodes) {
            if (parentCand.children.isEmpty()) { // avoids accumulation of children
                for (Nodes other: nodes) {
                    if (other.key.contains(parentCand.key + ".")) {
                        parentCand.parent = true;
                        parentCand.children.add(other);
                    }
                }
            }
        }
    }
    // Generates the Intro sentence (=Obersatz -> constant, independent of anything in filedata) and 
    // result (=Ergebnissatz) of Top Node.
    public static void setParentText() {
        // better put parents in a separate List? -> no need to look them up every time.
        for (Nodes node: nodes) {
            if (node.parent) {
                node.text = "";
                node.text = String.format("O) %s ist erfüllt, wenn ", node.name);
                for (Nodes child: node.children) {
                    node.text += String.format("%s erfüllt ist und ", child.name);
                }
                node.text = node.text.replaceAll(" und $", "."); // $ indicates replacing only last occurrence
                node.text += "\n\n";
                List < String > sum = new ArrayList <String> ();
                for (Nodes child: node.children) {
                    sum.add(child.value);
                }
                if (sum.contains("u")) {
                    node.value = "u";
                    node.result = String.format("E) Zur Prüfung von %s fehlt es an Informationen...\n\n", node.name);
                }
                else if (sum.contains("n")) {
                    node.value = "n";
                    node.result = String.format("E) Folglich ist %s nicht gegeben.\n\n", node.name);
                }
                else {
                    node.value = "y";
                    node.result = String.format("E) Folglich ist %s gegeben.\n\n", node.name);
                }
            }
        }
    }
    // compares expected fully stacked filedata to filedtata passed to it, generates node text and result
    // accordingly. If entries are missing in passed filedata, it will formulate text and result 
    // to be consistent with a partial report. -> does not crash if filedata lacks some validkeys/values
    public static void setChildText() {
        // Needs to be run first, because child values and child text is assigned here, so values can be summed to
        // yield parent value
        for (Nodes node: nodes) {
            if (!node.parent) {
                for (String inkey: input.keySet()) {
                    if (inkey.equals(node.key)) {
                        node.value = input.get(inkey);
                        if (node.value.equals("y")) {
                            node.text = String.format("      - %s wurde beachtet.\n\n", node.name);
                        }
                        else if (node.value.equals("n")) {
                            node.text = String.format("      - %s wurde nicht beachtet.\n\n", node.name);
                        }
                    }
                }
                if (node.value.equals("u")) {
                    node.text = String.format("      - %s ist ungeklärt...\n\n", node.name);
                }
            }
        }
    }
    public static void setReport() {
        for (Nodes node: nodes) {
            if (node.parent) // IDEA: put parents into List, so no need to lookup every time
            {
                node.block.add(node.text); // Obersatz
                for (Nodes child: node.children) {
                    node.block.add(child.text); // Subsumtion Unterpunkte
                }
                node.block.add(node.result + "\n"); // Ergebnissatz
                for (String line: node.block) {
                    report.add(line);
                }
            }
        }
    }
    // IMPORTANT: if not called -> text duplication and accumulation if Gen is called repeatedly
    // ( = clicking on check multiple times in GUI)
    // other solution that doesn't need clear(): condition for runAllMethods()
    public static void clearAll() {
        //IMPORTANT file_data.clear(), because parser has no checks whether file_data is already filled ->
        // -> keeps trying to add already existing keys but can't because of check for duplicates in
        // Parser.validator() -> excessive error messages in parser.errors
        Parser.file_data.clear();
        Parser.errors.clear();
        nodes.clear();
        input.clear();
        report.clear();
    }
    // Overloading genOutput handles two cases. 1) result by loading file. 2) user checks boxes without loading file
    // produces as a result from user checking boxes without loading a file.
    public static String genOutput(HashMap<String, String> data) {
        clearAll();
        input.putAll(data);
        // reassign input to data does not work -> .putAll() correct way to update input
        setNodes();
        setParents();
        setChildText();
        setParentText();
        setReport();
        StringBuilder output = new StringBuilder();
        for (String line: report) {
            output.append(line);
        }
        return output.toString();
    }
    public static String genOutput(String path) {
        return genOutput(Parser.parse(path));  // return needed in Gui.load()
    }
}