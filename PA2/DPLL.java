import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class DPLL {
    static Set<Integer> allAtoms = new HashSet<Integer>();

    public static void main(String[] args) throws Exception {
        try {
            File file = new File("./frontendOutput.txt");
            BufferedReader br = new BufferedReader(new FileReader(file));
            File outputFile = new File("./dpllOutput.txt");
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
            String st = "";
            String rest = "";
            Set<String> CS = new HashSet<String>();
            for (; ((st = br.readLine()) != null);) {
                if (st.trim().equals("0"))
                    break;
                CS.add(st.trim());
                Arrays.stream(st.split(" ")).forEach(e -> allAtoms.add(atomOf(e)));
            }
            for (; ((st = br.readLine()) != null);) {
                rest += st + "\n";
            }
            HashMap<Integer, Boolean> B = dpll(CS, new HashMap<Integer, Boolean>());
            if (B == null) {
                System.out.println("NO SOLUTION");
                return;
            }
            for (Integer atom : allAtoms) {
                System.out.println(atom + " " + (B.containsKey(atom) ? (B.get(atom) ? "T" : "F") : "T/F"));
            }
            System.out.print("0\n" + rest);
        } catch (Exception e) {
            System.out.println("Could not open file");
        }
    }

    public static HashMap<Integer, Boolean> dpll(Set<String> CS, HashMap<Integer, Boolean> B) {
        for (Set<String> _CS = new HashSet<String>(CS);; _CS = new HashSet<String>(CS)) {
            if (CS.isEmpty())
                return B;
            if (CS.stream().anyMatch(e -> e.trim().equals("")))
                return null;
            List<Object> easyCase = easyCaseIn(CS, B);
            if (easyCase != null) {
                CS = (Set<String>) easyCase.get(0);
                B = (HashMap<Integer, Boolean>) easyCase.get(1);
            }
            if (_CS.equals(CS))
                break;
        }
        Set<String> CSCopy = new HashSet<String>(CS);
        HashMap<Integer, Boolean> BCopy = new HashMap<Integer, Boolean>(B);
        Integer P = getNextUnboundAtom(B);
        List<Object> newVals = propagate(P, true, CSCopy, BCopy);
        CSCopy = (Set<String>) newVals.get(0);
        BCopy = (HashMap<Integer, Boolean>) newVals.get(1);
        HashMap<Integer, Boolean> answer = dpll(CSCopy, BCopy);
        if (answer != null)
            return answer;
        List<Object> newVals1 = propagate(P, false, CS, B);
        CS = (Set<String>) newVals1.get(0);
        B = (HashMap<Integer, Boolean>) newVals1.get(1);
        return dpll(CS, B);
    }

    public static List<Object> easyCaseIn(Set<String> CS, HashMap<Integer, Boolean> B) {
        HashMap<Integer, Integer> counts = new HashMap<Integer, Integer>();
        for (String _clause : CS) {
            String clause = _clause.trim();
            String[] splitClause = clause.split(" ");
            if (splitClause.length == 1) {
                return propagate(atomOf(clause), valueOf(clause), CS, B);
            }
            Arrays.stream(splitClause).forEach(atom -> {
                Integer key = Integer.parseInt(atom.trim());
                if (counts.containsKey(key)) {
                    counts.put(key, 1 + counts.get(key));
                } else {
                    counts.put(key, 1);
                }
            });
        }
        for (Integer atom : counts.keySet()) {
            if (!counts.containsKey(-atom)) {
                return propagate(atomOf(atom + ""), valueOf(atom + ""), CS, B);
            }
        }
        return null;
    }

    public static Integer atomOf(String L) {
        return Math.abs(Integer.parseInt(L));
    }

    public static Boolean valueOf(String L) {
        return Integer.parseInt(L) >= 0;
    }

    public static List<Object> propagate(Integer _A, Boolean V, Set<String> _CS, HashMap<Integer, Boolean> _B) {
        HashMap<Integer, Boolean> B = new HashMap<Integer, Boolean>(_B);
        B.put(_A, V);
        Integer A = !V ? _A * -1 : _A;
        String notA = (-A) + "";
        Set<String> CS = new HashSet<String>(_CS);
        for (String clause : _CS) {
            String[] splitClause = clause.split(" ");
            if (Arrays.stream(splitClause).anyMatch(e -> e.equals(A + ""))) {
                CS.remove(clause);
                continue;
            }
            if (Arrays.stream(splitClause).anyMatch(e -> e.equals(notA))) {
                CS.remove(clause);
                CS.add(Arrays.stream(splitClause)
                        .filter(s -> !s.equals(notA) && !s.isEmpty())
                        .collect(Collectors.joining(" "))
                        .trim());
            }
        }
        return Arrays.asList(CS, B);

    }

    public static Integer getNextUnboundAtom(HashMap<Integer, Boolean> B) {
        for (Integer atom : allAtoms) {
            if (!B.containsKey(atom)) {
                return atom;
            }
        }
        return null;
    }
}