import java.util.*;

public class Nodes
{
    boolean parent;
    List<Nodes> children = new ArrayList<Nodes>();
    List<String> block = new ArrayList<String>();
    String value;
    String text; // child has only text(=subsumtion), parents have text (= obersatz) and result (=ergebnis)
    String result; // hardcode obersätze generate dynamically from children???
    String key;
    String name;
}





