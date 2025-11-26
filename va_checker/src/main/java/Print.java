import java.util.*;
/**
 * During testing of Parser I got tired of writing System.out.print every time and having to write loops for hashmaps
 * and other datastructures. This utility class covers most conceivable use cases for print.
 */
public class Print {
    // print String version
    public static void p(String arg) {
        System.out.println(arg);
    }
    // print integer version
    public static void p(String[] arg) {
        for (String i:arg) {
            System.out.println(i);
        }
    }
    public static void p(int arg) {
        System.out.println(arg);
    }
    // print integer array version
    public static void p(int[] arg) {
        for (int i:arg) {
            System.out.println(i);
        }
    }
    public static void p(List<String> arg) {
        for (String i:arg) {
            System.out.println(i);
        }
    }
    public static void p(Set<String> arg) {
        for (String i:arg) {
            System.out.println(i);
        }
    }
    public static void p(LinkedHashMap<String, String> arg) {
        System.out.println(arg.keySet());
        System.out.println(arg.values());
    }
    public static void p(HashMap<String, String> arg) {
        System.out.println(arg.keySet());
        System.out.println(arg.values());
    }
    public static void p(Boolean arg) {
        System.out.println(arg);
    }
}