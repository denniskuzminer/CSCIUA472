import java.io.*;
import java.util.*;

public class Frontend {
    static TreeMap<String, TreeSet<String>> graph = new TreeMap<String, TreeSet<String>>(new StartComparator());
    static TreeMap<String, ArrayList<String>> treasures = new TreeMap<String, ArrayList<String>>();
    static int MAX_STEPS = 0;
    static int currAtom = 1;
    static HashMap<String, Integer> mapping = new HashMap<String, Integer>();

    public static void main(String[] args) throws Exception {
        File file = new File("./input.txt");
        BufferedReader br = new BufferedReader(new FileReader(file));
        File outputFile = new File("./frontendOutput.txt");
        if (outputFile.exists())
            outputFile.delete();
        try {
            outputFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileOutputStream outputStream = new FileOutputStream(outputFile);
        PrintStream printStream = new PrintStream(outputStream);
        System.setOut(printStream);
        String st = br.readLine();
        Arrays.stream(st.trim().split(" ")).forEach(v -> addVertex(v));
        st = br.readLine();
        Arrays.stream(st.trim().split(" ")).forEach(v -> treasures.put(v, new ArrayList<String>()));
        st = br.readLine();
        MAX_STEPS = Integer.parseInt(st.trim());
        for (; ((st = br.readLine()) != null);) {
            boolean t = false, n = false;
            String src = "";
            ArrayList<String> treasuresToAssign = new ArrayList<String>();
            ArrayList<String> destinationsToAssign = new ArrayList<String>();
            for (String v : st.trim().split(" ")) {
                if (src.equals("")) {
                    src = v;
                } else {
                    if (v.equals("TREASURES")) {
                        t = true;
                        continue;
                    }
                    if (v.equals("NEXT")) {
                        n = true;
                        continue;
                    }
                    if (t && !n) {
                        treasuresToAssign.add(v);
                        continue;
                    }
                    if (t && n) {
                        destinationsToAssign.add(v);
                        continue;
                    }
                }
            }
            for (String dest : destinationsToAssign) {
                addEdge(src, dest);
            }
            for (String treasure : treasuresToAssign) {
                treasures.get(treasure).add(src);
            }
        }
        // Category 1
        for (int k = 0; k <= MAX_STEPS; k++) {
            for (int i = 0; i < graph.keySet().size(); i++) {
                for (int j = i; j < graph.keySet().size(); j++) {
                    ArrayList<String> keys = new ArrayList<String>(graph.keySet());
                    if (keys.get(j).equals(keys.get(i)))
                        continue;
                    String A = "At(" + keys.get(i) + "," + k + ")";
                    String B = "At(" + keys.get(j) + "," + k + ")";
                    int ANum = !mapping.containsKey(A) ? currAtom++ : mapping.get(A);
                    int BNum = !mapping.containsKey(B) ? currAtom++ : mapping.get(B);
                    System.out.println(-ANum + " " + -BNum);
                    mapping.putIfAbsent(A, ANum);
                    mapping.putIfAbsent(B, BNum);
                }
            }
        }
        // Category 2
        for (int k = 0; k < MAX_STEPS; k++) {
            for (String key : graph.keySet()) {
                String x = "-" + mapping.get("At(" + key + "," + k + ")");
                for (String v : graph.get(key)) {
                    x += " " + mapping.get("At(" + v + "," + (k + 1) + ")");
                }
                System.out.println(x);
            }
        }
        // Category 3
        for (int k = 0; k <= MAX_STEPS; k++) {
            for (String treasure : treasures.keySet()) {
                for (String v : treasures.get(treasure)) {
                    String B = "-" + mapping.get("At(" + v + "," + k + ")");
                    String A = "Has(" + treasure + "," + k + ")";
                    int ANum = !mapping.containsKey(A) ? currAtom++ : mapping.get(A);
                    System.out.println(B + " " + ANum);
                    mapping.putIfAbsent(A, ANum);
                }
            }
        }
        // Category 4
        for (int k = 0; k < MAX_STEPS; k++) {
            for (String treasure : treasures.keySet()) {
                String A = "-" + mapping.get("Has(" + treasure + "," + k + ")");
                String B = "" + mapping.get("Has(" + treasure + "," + (k + 1) + ")");
                System.out.println(A + " " + B);
            }
        }
        // Category 5
        for (int k = 0; k < MAX_STEPS; k++) {
            for (String treasure : treasures.keySet()) {
                String A = "" + mapping.get("Has(" + treasure + "," + k + ")");
                String B = "-" + mapping.get("Has(" + treasure + "," + (k + 1) + ")");
                String C = "";
                for (String v : treasures.get(treasure)) {
                    C += " " + mapping.get("At(" + v + "," + (k + 1) + ")");
                }
                System.out.println(A + " " + B + " " + C.trim());
            }
        }
        // Category 6
        System.out.println(mapping.get("At(START,0)"));
        // Category 7
        for (String treasure : treasures.keySet()) {
            System.out.println("-" + mapping.get("Has(" + treasure + ",0)"));
        }
        // Category 8
        for (String treasure : treasures.keySet()) {
            System.out.println(mapping.get("Has(" + treasure + "," + MAX_STEPS + ")"));
        }
        // Mapping
        System.out.println(0);
        TreeMap<String, Integer> sortedMap = new TreeMap<String, Integer>(new Comparator<String>() {
            public int compare(String s1, String s2) {
                return mapping.get(s1).compareTo(mapping.get(s2));
            }
        });
        sortedMap.putAll(mapping);
        for (Map.Entry<String, Integer> entry : sortedMap.entrySet()) {
            System.out.println(entry.getValue() + " " + entry.getKey());
        }
    }

    // https://progressivecoder.com/graph-implementation-in-java-using-hashmap/
    public static void addEdge(String source, String destination) {
        if (!graph.containsKey(source)) {
            addVertex(source);
        }
        if (!graph.containsKey(destination)) {
            addVertex(destination);
        }
        graph.get(source).add(destination);
        graph.get(destination).add(source);
    }

    private static void addVertex(String vertex) {
        if (vertex.equals(""))
            return;
        graph.put(vertex, new TreeSet<String>(new StartComparator()));
    }

    public static boolean hasEdge(String source, String destination) {
        return graph.get(source).contains(destination);
    }
}

class StartComparator implements Comparator<String> {
    @Override
    public int compare(String o1, String o2) {
        if (o1.equals("START")) {
            if (o2.equals("START")) {
                return 0;
            } else {
                return -1;
            }
        } else if (o2.equals("START")) {
            return 1;
        } else {
            return o1.compareTo(o2);
        }
    }
}