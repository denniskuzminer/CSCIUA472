import java.util.*;
import java.util.stream.IntStream;
import java.io.*;
import java.text.DecimalFormat;

public class Main {

    public static void main(String[] args) throws Exception {
        File file = new File("./input.txt");
        BufferedReader br = new BufferedReader(new FileReader(file));
        int NSides = Integer.parseInt(br.readLine());
        int LTarget = Integer.parseInt(br.readLine());
        int UTarget = Integer.parseInt(br.readLine());
        int NDice = Integer.parseInt(br.readLine());
        double M = Double.parseDouble(br.readLine());
        int NGames = Integer.parseInt(br.readLine());

        int[][][] WinCount = new int[LTarget][LTarget][NDice + 1];
        int[][][] LoseCount = new int[LTarget][LTarget][NDice + 1];
        for (int i = 0; i < NGames; i++) {
            PlayGame(NDice, NSides, LTarget, UTarget, LoseCount, WinCount, M);
        }
        System.out.println(extractAnswer(WinCount, LoseCount));
    }

    public static String extractAnswer(int[][][] WinCount, int[][][] LoseCount) {
        String play = "Play =\n";
        String prob = "Prob =\n";
        DecimalFormat df = new DecimalFormat("#.####");
        for (int X = 0; X < LoseCount.length; X++) {
            for (int Y = 0; Y < LoseCount.length; Y++) {
                int maxJ = 0;
                double maxJProb = 0;
                for (int J = 0; J < LoseCount[X][Y].length; J++) {
                    double currProb = 0;
                    if (WinCount[X][Y][J] + LoseCount[X][Y][J] == 0)
                        continue;

                    currProb = (double) WinCount[X][Y][J] / (double) (WinCount[X][Y][J] + LoseCount[X][Y][J]);
                    if (currProb > maxJProb) {
                        maxJProb = currProb;
                        maxJ = J;
                    }
                }
                play += ("\t" + maxJ);
                prob += ("\t" + Double.parseDouble(df.format(maxJProb)));
            }
            play += "\n";
            prob += "\n";
        }
        return play + "\n" + prob;
    }

    public static void PlayGame(int NDice, int NSides, int LTarget, int UTarget, int[][][] LoseCount,
            int[][][] WinCount, double M) {
        int playerATotal = 0, playerBTotal = 0;
        char winner = (char) -1;

        ArrayList<String> rollHistory = new ArrayList<String>(); // Each turn looks like Player + " " + PlayerTotal + "
                                                                 // "
                                                                 // + NumDiceRolled + " " + OpponentTotal. E.g. "A 13 2
                                                                 // 7"

        for (;;) {
            int[] roll = rollDice(chooseDice(new int[] { playerATotal, playerBTotal }, LoseCount, WinCount, NDice, M),
                    NSides);
            rollHistory.add('A' + " " + playerATotal + " " + roll.length + " " + playerBTotal);
            playerATotal += Arrays.stream(roll).sum();
            if (isInRange(LTarget, UTarget, playerATotal)) {
                winner = 'A';
                break;
            }
            if (wentBust(UTarget, playerATotal)) {
                winner = 'B';
                break;
            }

            roll = rollDice(chooseDice(new int[] { playerATotal, playerBTotal }, LoseCount, WinCount, NDice, M),
                    NSides);
            rollHistory.add('B' + " " + playerBTotal + " " + roll.length + " " + playerATotal);
            playerBTotal += Arrays.stream(roll).sum();
            if (isInRange(LTarget, UTarget, playerBTotal)) {
                winner = 'B';
                break;
            }
            if (wentBust(UTarget, playerBTotal)) {
                winner = 'A';
                break;
            }
        }

        for (String roll : rollHistory) {
            String[] splitRoll = roll.split(" ");
            char player = splitRoll[0].charAt(0);
            int playerTotal = Integer.parseInt(splitRoll[1]);
            int numDiceRolled = Integer.parseInt(splitRoll[2]);
            int opponentTotal = Integer.parseInt(splitRoll[3]);
            if (player == winner) {
                WinCount[playerTotal][opponentTotal][numDiceRolled]++;
            } else {
                LoseCount[playerTotal][opponentTotal][numDiceRolled]++;
            }
        }
    }

    public static boolean wentBust(int UTarget, int total) {
        return total > UTarget;
    }

    public static boolean isInRange(int LTarget, int UTarget, int total) {
        return LTarget <= total && total <= UTarget;
    }

    public static int chooseDice(int[] Score, int[][][] LoseCount, int[][][] WinCount, int NDice, double M) {
        int K = NDice;
        int X = Score[0], Y = Score[1];
        double[] f = new double[K + 1];
        for (int J = 1; J <= K; J++) {
            f[J] = (WinCount[X][Y][J] + LoseCount[X][Y][J]) == 0 ? .5
                    : (double) WinCount[X][Y][J] / (double) (WinCount[X][Y][J] + LoseCount[X][Y][J]);
        }
        int B = IntStream.range(0, f.length).reduce((i, j) -> f[i] > f[j] ? i : j).orElse(-1);
        double g = IntStream.range(0, f.length).filter(i -> i != B).mapToDouble(i -> f[i]).sum();
        double T = (double) IntStream.range(1, f.length)
                .reduce((acc, J) -> acc + WinCount[X][Y][J] + LoseCount[X][Y][J])
                .orElse(-1);
        double[] p = new double[K + 1];
        p[B] = (double) (T * f[B] + M) / (double) (T * f[B] + K * M);
        for (int J = 1; J <= K; J++) {
            if (J != B) {
                p[J] = (double) (1 - p[B]) * ((T * f[J] + M) / (double) (g * T + (K - 1) * M));
            }
        }
        return chooseFromDist(Arrays.copyOfRange(p, 1, p.length));
    }

    public static int[] rollDice(int NDice, int NSides) {
        return Arrays.stream(new int[NDice]).map(e -> 1 + ((int) (Math.random() * NSides))).toArray();
    }

    public static int chooseFromDist(double[] p) {
        double[] u = new double[p.length];
        u[0] = p[0];
        for (int i = 1; i < p.length; i++) {
            u[i] = u[i - 1] + p[i];
        }
        double x = Math.random();
        for (int i = 0; i < p.length; i++) {
            if (x < u[i]) {
                return i + 1;
            }
        }
        return p.length;
    }

    public static void distTest() {
        double[] p = { 0.1, 0.2, 0.3, 0.4 }; // to change
        int numSamples = 10000000;
        int[] count = new int[p.length];
        for (int i = 0; i < numSamples; i++) {
            int sample = chooseFromDist(p);
            count[sample]++;
        }
        for (int i = 0; i < count.length; i++) {
            System.out.printf("Value %d was chosen %d times\n", i, count[i]);
        }
    }

}