import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main {
    static String words = "about all along also although among and any anyone anything are around because been before being both but came come coming could did each else every for from get getting going got gotten had has have having her here hers him his how however into its like may most next now only our out particular same she should some take taken taking than that the then there these they this those throughout too took very was went what when which while who why will with without would yes yet you your com doc edu encyclopedia fact facts free home htm html http information internet net new news official page pages resource resources pdf site sites usa web wikipedia www one ones two three four five six seven eight nine ten tens eleven twelve dozen dozens thirteen fourteen fifteen sixteen seventeen eighteen nineteen twenty thirty forty fifty sixty seventy eighty ninety hundred hundreds thousand thousands million millions";
    static Set<String> stopWords = new HashSet<String>(Arrays.asList(words.split(" ")));
    static double E = .1;

    public static void main(String[] args) throws Exception {
        // Step 1 and 3.1.1
        int N = Integer.parseInt(args[1]);
        List<Bio> bios = parseBiographies(args[0]);

        List<Bio> trainBios = bios.subList(0, N);
        List<Bio> testBios = bios.subList(N, bios.size());
        Set<String> trainWords = getWordSet(trainBios);

        // Step 3.1.2
        HashMap<String, Long> bioCategories = countCategories(trainBios); // OccT (C)
        HashMap<String, HashMap<String, Long>> bioOccurrences = countWordOccurrencesInCategories(trainBios, trainWords); // OccT(W|C)
        // Step 3.1.3
        HashMap<String, Double> categoryProbs = getCategoryProbabilities(bioCategories, trainBios.size()); // L(C)
        HashMap<String, HashMap<String, Double>> wordProbs = getWordProbabilities(bioOccurrences, bioCategories); // L(W|C)

        // Step 3.2
        long numCorrect = 0L;
        for (Bio bio : testBios) {
            Object[] predictions = extractProbabilities(bio, categoryProbs, wordProbs, trainWords);
            String prediction = (String) predictions[0];
            HashMap<String, Double> probabilities = (HashMap<String, Double>) predictions[1];
            boolean isCorrect = bio.category.equals(prediction);
            numCorrect += (isCorrect ? 1 : 0);
            System.out.println(capitalize(bio.name + ". Prediction: " + prediction + ". "
                    + (isCorrect ? "right" : "wrong") + "."));
            System.out.println(
                    capitalize(probabilities.toString().replace("=", ": ").replace("{", "").replace("}", "")
                            .replace(",", ". ") + "\n"));
        }
        System.out.println("Overall accuracy: " + numCorrect + " out of " + testBios.size() + " = "
                + numCorrect / (double) testBios.size() + ".");
    }

    public static Object[] extractProbabilities(Bio bio, HashMap<String, Double> categoryProbs,
            HashMap<String, HashMap<String, Double>> wordProbs, Set<String> trainWords) {
        HashMap<String, Double> predictions = predict(bio, categoryProbs, wordProbs, trainWords);
        String prediction = getPrediction(predictions);
        double m = predictions.get(prediction);
        HashMap<String, Double> X = new HashMap<>();
        double s = 0;
        for (String category : predictions.keySet()) {
            double ci = predictions.get(category);
            double xi = (ci - m < 7) ? Math.pow(2, m - ci) : 0;
            s += xi;
            X.put(category, xi);
        }
        HashMap<String, Double> probabilities = new HashMap<>();
        for (String category : X.keySet()) {
            probabilities.put(category, X.get(category) / s);
        }
        return new Object[] { prediction, probabilities };
    }

    public static String capitalize(String input) {
        return Arrays.stream(input.split(" "))
                .map(word -> word.length() > 1 ? word.substring(0, 1).toUpperCase() + word.substring(1) : word)
                .collect(Collectors.joining(" "));
    }

    public static String getPrediction(HashMap<String, Double> predictions) {
        return predictions.entrySet().stream()
                .min(Comparator.comparing(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public static HashMap<String, Double> predict(Bio bio, HashMap<String, Double> categoryProbs,
            HashMap<String, HashMap<String, Double>> wordProbs, Set<String> trainWords) {
        HashMap<String, Double> predictedProbs = new HashMap<String, Double>();
        for (String category : categoryProbs.keySet()) {
            double sum = 0;
            for (String word : Pattern.compile("\\W+").split(bio.description)) {
                if (!trainWords.contains(word)) {
                    continue;
                }
                sum += wordProbs.get(category).get(word);
            }
            predictedProbs.put(category, sum + categoryProbs.get(category));
        }
        return predictedProbs;
    }

    public static Set<String> getWordSet(List<Bio> trainBios) {
        Set<String> wordSet = new HashSet<String>();
        for (Bio bio : trainBios) {
            String[] words = Pattern.compile("\\W+").split(bio.description);
            for (String word : words) {
                if (word.length() > 0) {
                    wordSet.add(word.toLowerCase());
                }
            }
        }
        return wordSet;
    }

    public static double log2(double num) {
        return Math.log(num) / Math.log(2.0);
    }

    public static HashMap<String, Double> getCategoryProbabilities(HashMap<String, Long> bioCategories, int T) {
        return bioCategories.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> -log2((((double) entry.getValue() / T) + E)
                                / (1 + (double) bioCategories.entrySet().size() * E)),
                        (v1, v2) -> v1,
                        HashMap::new));

    }

    public static HashMap<String, HashMap<String, Double>> getWordProbabilities(
            HashMap<String, HashMap<String, Long>> bioOccurrences, HashMap<String, Long> bioCategories) {
        HashMap<String, HashMap<String, Double>> probs = new HashMap<>();

        for (String category : bioOccurrences.keySet()) {
            HashMap<String, Double> wordProbs = new HashMap<>();
            HashMap<String, Long> wordMap = bioOccurrences.get(category);
            for (String word : wordMap.keySet()) {
                double freq = (wordMap.get(word) / (double) bioCategories.get(category) + E) / (1 + 2.0 * E);
                wordProbs.put(word, -log2(freq));
            }
            probs.put(category, wordProbs);
        }

        return probs;
    }

    public static HashMap<String, Double> getCategoryFreqs(HashMap<String, Long> bioCategories, int T) {
        return bioCategories.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> (double) entry.getValue() / T, (v1, v2) -> v1,
                        HashMap::new));
    }

    public static HashMap<String, Long> countCategories(List<Bio> bios) {
        return bios.stream().collect(Collectors.groupingBy(bio -> bio.category, HashMap::new, Collectors.counting()));
    }

    public static HashMap<String, HashMap<String, Long>> countWordOccurrencesInCategories(List<Bio> bios,
            Set<String> trainWords) {
        HashMap<String, HashMap<String, Long>> occurrences = new HashMap<>(); // { Category: { Word: count of number of
                                                                              // times the word appears in that category
                                                                              // } }

        for (Bio bio : bios) {
            String category = bio.category;
            String[] words = Pattern.compile("\\W+").split(bio.description);

            if (!occurrences.containsKey(category)) {
                occurrences.put(category, new HashMap<>());
            }

            HashMap<String, Long> categoryMap = occurrences.get(category);
            for (String word : words) {
                categoryMap.put(word, categoryMap.getOrDefault(word, 0L) + 1);
            }
            for (String word : trainWords) {
                if (!categoryMap.containsKey(word)) {
                    categoryMap.put(word, 0L);
                }
            }
        }

        return occurrences;

    }

    public static String normalize(String input) {
        return Arrays.stream(input.split("\\s+"))
                .filter(word -> word.length() > 2)
                .filter(word -> !stopWords.contains(word))
                .map(word -> word.toLowerCase())
                .collect(Collectors.joining(" "));
    }

    public static List<Bio> parseBiographies(String filePath) {
        List<Bio> biographies = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            Bio currentBio = new Bio();
            int state = 0; // 0: name, 1: category, 2: biography

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    if (state != 0) {
                        biographies.add(currentBio);
                        currentBio = new Bio();
                        state = 0;
                    }
                } else {
                    switch (state) {
                        case 0:
                            currentBio.name = normalize(line.trim());
                            state = 1;
                            break;
                        case 1:
                            String category = normalize(line.trim());
                            currentBio.category = category;

                            state = 2;
                            break;
                        case 2:
                            currentBio.description += (normalize(line.trim()) + " ");
                            break;
                    }
                }
            }
            if (state != 0) {
                biographies.add(currentBio);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return biographies;
    }
}

class Bio {
    public String name;
    public String category;
    public String description;

    public Bio(String name, String category, String description) {
        this.name = name;
        this.category = category;
        this.description = description;
    }

    public Bio() {
        this.name = "";
        this.category = "";
        this.description = "";
    }

    public String toString() {
        return "Name: " + name + "\nCategory: " + category + "\nDescription: " + description + "\n";
    }
}