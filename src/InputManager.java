import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

public class InputManager {
    private static int nUmpires;
    private static int nTeams;
    private static int nRounds;
    private static int[][] dist;
    private static int[][] opponents;
    private static int[][][] games;

    public void readInput(String inputFilename){
        try {
            File file = new File(inputFilename);
            Scanner scanner = new Scanner(file);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                if (line.contains("nTeams")){
                    nTeams = Integer.parseInt(line.split("=")[1].trim().split(";")[0]);
                    nUmpires = nTeams/2;
                    nRounds = 2*nTeams-2;
                    dist  = new int[nTeams][nTeams];
                    opponents  = new int[nRounds][nTeams];
                }
                else if (line.contains("dist")){
                    for (int i = 0; i < nTeams; i++) {
                        line = scanner.nextLine();
                        String[] numbers = line.replaceAll("[\\[\\]]", "").split("\\s+");
                        int notEmptyCounter = 0;
                        for (String number : numbers) {
                            if (!number.isEmpty()) {
                                dist[i][notEmptyCounter] = Integer.parseInt(number);
                                notEmptyCounter++;
                            }
                        }
                    }
                }
                else if (line.contains("opponents")){
                    for (int i = 0; i < nRounds; i++) {
                        line = scanner.nextLine();
                        String[] numbers = line.replaceAll("[\\[\\]]", "").split("\\s+");
                        int notEmptyCounter = 0;
                        for (String number : numbers) {
                            if (!number.isEmpty()) {
                                opponents[i][notEmptyCounter] = Integer.parseInt(number);
                                notEmptyCounter++;
                            }
                        }
                    }
                }
            }
            scanner.close();

        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + inputFilename);
            e.printStackTrace();
        }
        games = new int[nRounds][nTeams/2][2];
        for(int round = 0; round < nRounds;  round++){
            int i = 0;
            for(int teams = 0; teams < nTeams; teams++) {
                int opp = opponents[round][teams];
                if (opp > 0) {
                    games[round][i][0] = teams + 1;
                    games[round][i][1] = opp;
                    i++;
                }
            }
        }
    }

    public static void print(){
        System.out.println("umpires:" + nUmpires);
        System.out.println("teams:" + nTeams);
        System.out.println("rounds:" + nRounds);
        System.out.println("dist:");
        for (int[] ints : dist) {
            System.out.println(Arrays.toString(ints));
        }
        System.out.println("opponents:");
        for (int[] ints : opponents) {
            System.out.println(Arrays.toString(ints));
        }
        System.out.println("games:");
        for (int i = 0; i < nRounds; i++) {
            System.out.print('[');
            for(int j = 0; j < nTeams/2; j++){
                System.out.print(Arrays.toString(games[i][j]) + ", ");
            }
            System.out.println(']');
        }
    }

    public static int getnUmpires() {
        return nUmpires;
    }

    public static int getnTeams() {
        return nTeams;
    }

    public static int getnRounds() {
        return nRounds;
    }

    public static int getDist(int i, int j) {
        return dist[i][j];
    }

    public static int[][][] getGames() {
        return games;
    }

//    public static int[][] getOpponents() {
//        return opponents;
//    }
}
