import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Backend {
    public static void main(String[] args) throws Exception {
        File file = new File("./dpllOutput.txt");
        BufferedReader br = new BufferedReader(new FileReader(file));
        TreeMap<Integer, Boolean> B = new TreeMap<Integer, Boolean>();
        TreeMap<Integer, String> mapping = new TreeMap<Integer, String>();
        String st = "";
        for (; ((st = br.readLine()) != null);) {
            if (st.trim().equals("NO SOLUTION")) {
                System.out.println("NO SOLUTION");
                return;
            }
            if (st.trim().equals("0"))
                break;
            String[] pair = st.split(" ");
            B.put(Integer.parseInt(pair[0]), pair[1].equals("T") ? true : false);
        }
        for (; ((st = br.readLine()) != null);) {
            String[] pair = st.trim().split(" ");
            mapping.put(Integer.parseInt(pair[0]), pair[1]);
        }
        String path = "";
        for (Map.Entry<Integer, Boolean> entry : B.entrySet()) {
            Integer k = entry.getKey();
            Boolean v = entry.getValue();
            if (v) {
                path += format(mapping.get(k));
            }
        }
        System.out.println(path.trim());
    }

    public static String format(String s) {
        String regex = "At\\((\\S+),\\d+\\)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(s);
        if (matcher.find()) {
            return matcher.group(1) + " ";
        }
        return "";
    }
}
