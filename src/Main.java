import gurobi.*;
public class Main {
    private final static String inputFilename = "Instances/umps8.txt";
    public static int q1 = 4;  //An umpire crew must wait q1-1 rounds before revisiting a team's home {0-nUmpires}
    public static int q2 = 2;  //An umpire crew must wait q2-1 rounds before officiating the same team again {0-floor(nUmpires/2)}

    public static void main(String[] args) throws GRBException {
        InputManager inputManager = new InputManager();
        inputManager.readInput(inputFilename);
        InputManager.print();
        int [][] w  = new int[InputManager.getnTeams()][InputManager.getnRounds()];
        for (int i = 0; i < InputManager.getnTeams(); i++){
            for (int r = 0; r < InputManager.getnRounds(); r++){
                w[i][r] = 1;
            }
        }
        Column c = PricingSolver.gurobi(0, 1, w);
        System.out.println(c);
    }
}