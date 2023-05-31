import java.io.*;
import java.util.*;

public class Main {
    static int target;
    static boolean isVerbose;
    static boolean isHillClimbing;
    static int randomRestartTimes;
    static HashMap<Character, Integer> vertexValues = new HashMap<Character, Integer>();
    static HashMap<Character, LinkedList<Character>> graph = new HashMap<Character, LinkedList<Character>>();
    static String solution = "";

    public static void main(String[] args) throws Exception {
        File file = new File("./input.txt");
        BufferedReader br = new BufferedReader(new FileReader(file));
        String st = "";
        st = br.readLine();
        String[] line1 = st.split(" ");
        target = Integer.parseInt(line1[0]);
        isVerbose = 'V' == line1[1].charAt(0);
        if (line1.length == 3) {
            randomRestartTimes = Integer.parseInt(line1[2]);
            isHillClimbing = true;
        }
        for (; ((st = br.readLine()) != null);) {
            if (st.equals(""))
                break;
            String[] line = st.split(" ");
            vertexValues.put(line[0].charAt(0), Integer.parseInt(line[1]));
        }
        for (; ((st = br.readLine()) != null);) {
            String[] line = st.split(" ");
            addEdge(line[0].charAt(0), line[1].charAt(0));
        }
        if (isHillClimbing) {
            randomRestart();
        } else {
            ids();
        }
        br.close();
    }

    public static void ids() {
        for (int i = 1; i <= graph.size(); i++) {
            if (isVerbose) {
                System.out.println("Depth=" + i + ".");
            }
            dfs(0, i, "");
            if (!solution.equals("")) {
                System.out.println("\nFound solution " + solution.trim() + " Value=" + getValueOf(solution) + ".");
                return;
            }
        }
        System.out.println("No solution found");
    }

    public static void dfs(int currentLevel, int limit, String currentState) {
        if (currentLevel >= limit) {
            return;
        }
        for (String state : getSuccessorStates(currentState)) {
            if (!solution.equals("")) {
                return;
            }
            int stateValue = getValueOf(state);
            if (isVerbose) {
                System.out.println(state.trim() + " Value=" + stateValue + ".");
            }
            if (stateValue >= target) {
                solution = state;
                return;
            }
            dfs(currentLevel + 1, limit, state);
        }
    }

    public static ArrayList<String> getSuccessorStates(String state) {
        ArrayList<String> successors = new ArrayList<String>();
        String stateConnections = Arrays.stream(state.split(" ")).reduce("",
                (prev, curr) -> curr.equals("") ? prev : prev + " " + graph.get(curr.charAt(0)).toString());
        stateConnections += (" " + state);
        for (Character vertex : graph.keySet()) {
            if (stateConnections.indexOf(vertex) == -1) {
                successors.add(state + " " + vertex);
            }
        }
        return successors;
    }

    public static int getValueOf(String state) {
        String[] elems = state.split(" ");
        int total = 0;
        for (String elem : elems) {
            if (elem.equals("")) {
                continue;
            }
            total += vertexValues.get(elem.charAt(0));
        }
        return total;
    }

    public static int getErrorOf(String state) {
        String[] elems = state.split(" ");
        int cost = 0;
        for (int i = 0; i < elems.length; i++) {
            for (int j = i + 1; j < elems.length; j++) {
                if (!(j < elems.length)) {
                    break;
                }
                if (elems[j].equals("") || elems[i].equals("")) {
                    continue;
                }
                if (hasEdge(elems[i].charAt(0), elems[j].charAt(0))) {
                    cost += Math.min(vertexValues.get(elems[i].charAt(0)), vertexValues.get(elems[j].charAt(0)));
                }
            }
        }
        return Math.max(0, target - getValueOf(state)) + cost;
    }

    public static void randomRestart() {
        for (int i = 0; i < randomRestartTimes && solution.equals(""); i++) {
            String start = getRandomState();
            if (isVerbose) {
                System.out.println("Randomly chosen start state: " + start);
                System.out.println(start + " Value=" + getValueOf(start) + ". Error=" + getErrorOf(start) + ".");
            }
            hillClimbing(start);
        }
        if (!isVerbose && solution.equals("")) {
            System.out.println("No solution found");
        }
    }

    public static void hillClimbing(String start) {
        for (String bestNeighbor;; start = bestNeighbor) {
            if (isVerbose) {
                System.out.println("Neighbors: ");
            }
            bestNeighbor = getBestNeighbor(start);
            if (!solution.equals("")) {
                return;
            }
            int bestNeighborError = getErrorOf(bestNeighbor);
            int bestNeighborValue = getValueOf(bestNeighbor);
            if (bestNeighborError > getErrorOf(start)) {
                if (isVerbose) {
                    System.out.println("\nSearch Failed.\n");
                }
                return;
            } else {
                if (isVerbose) {
                    System.out.println(
                            "\nMove to " + bestNeighbor.trim() + " Value=" + bestNeighborValue + ". Error="
                                    + bestNeighborError);
                }
            }
        }
    }

    public static String getRandomState() {
        String state = "";
        for (Character vertex : graph.keySet()) {
            state += (Math.round(Math.random()) == 1 ? (vertex + " ") : "");
        }
        return state;
    }

    public static String getBestNeighbor(String state) {
        String[] elems = state.split(" ");
        int minError = Integer.MAX_VALUE;
        String minErrorState = null;
        for (String elem : elems) {
            String newState = state.replace(elem + " ", "").replace(elem, "").trim();
            int newValue = getValueOf(newState);
            int newError = getErrorOf(newState);
            if (newError < minError) {
                minError = newError;
                minErrorState = newState;
            }
            if (isVerbose) {
                System.out.println((newState.trim().equals("") && newValue == 0 ? "{}" : newState.trim()) + " Value="
                        + newValue + ". Error=" + newError + ".");
            }
            if (newError == 0 && newValue >= target) {
                solution = newState;
                System.out.println("\nFound solution " + solution.trim() + " Value=" + getValueOf(solution) + ".");
                return null;
            }
        }
        for (Character vertex : graph.keySet()) {
            if (state.indexOf(vertex) != -1) {
                continue;
            }
            String newState = state.trim() + " " + vertex;
            int newValue = getValueOf(newState);
            int newError = getErrorOf(newState);
            if (newError < minError) {
                minError = newError;
                minErrorState = newState;
            }
            if (isVerbose) {
                System.out.println(newState.trim() + " Value=" + newValue + ". Error=" + newError + ".");
            }
            if (newError == 0 && newValue >= target) {
                solution = newState;
                System.out.println("\nFound solution " + solution.trim() + " Value=" + getValueOf(solution) + ".");
                return null;
            }
        }
        return minErrorState;
    }

    // https://progressivecoder.com/graph-implementation-in-java-using-hashmap/
    public static void addEdge(Character source, Character destination) {
        if (!graph.containsKey(source)) {
            addVertex(source);
        }
        if (!graph.containsKey(destination)) {
            addVertex(destination);
        }
        graph.get(source).add(destination);
        graph.get(destination).add(source);
    }

    private static void addVertex(Character vertex) {
        graph.put(vertex, new LinkedList<Character>());
    }

    public static boolean hasEdge(Character source, Character destination) {
        return graph.get(source).contains(destination);
    }
}