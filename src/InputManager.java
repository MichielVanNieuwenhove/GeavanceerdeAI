import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

public class InputManager {
    public static int nTeams;
    public static int[][] dist = new int[nTeams][nTeams];
    public static int[][] opponents = new int[nTeams][nTeams];

    public void readInput(String inputFilename){
        try {
            File file = new File(inputFilename);
            Scanner scanner = new Scanner(file);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                if (line.contains("nTeams")){
                    nTeams = Integer.parseInt(line.split("=")[1].trim().split(";")[0]);
                }
                else if (line.contains("dist")){
                    for (int i = 0; i < nTeams; i++) {
                        line = scanner.nextLine();
                        String[] numbers = line.replaceAll("[\\[\\]]", "").split("\\s+");
                        for (int j = 0; j < nTeams; j++) {
                            if (!Objects.equals(numbers[j], "")){
                                dist[i][j] = Integer.parseInt(numbers[j]);
                            }
                        }
                    }
                }
                else if (line.contains("opponents")){

                }
            }

        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + inputFilename);
            e.printStackTrace();
        }

        //System.out.println(nTeams);
        System.out.println(Arrays.toString(Arrays.stream(dist).toArray()));
    }
}
