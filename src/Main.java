import gurobi.*;
public class Main {
    private final static String inputFilename = "Instances/umps8.txt";
    public static final int q1 = 4;  //An umpire crew must wait q1-1 rounds before revisiting a team's home {0-nUmpires}
    public static final int q2 = 2;  //An umpire crew must wait q2-1 rounds before officiating the same team again {0-floor(nUmpires/2)}

    public static void main(String[] args) throws GRBException {
        InputManager inputManager = new InputManager();
        inputManager.readInput(inputFilename);
        InputManager.print();

        masterProblemSolver.init();
        int numNewColumns = 1;
        while (numNewColumns > 0){
            MasterProblemSolution sol = masterProblemSolver.gurobi();
            numNewColumns = 0;
            for (int u = 0; u < InputManager.getnUmpires(); u++) {
                Column c = PricingSolver.gurobi(u, sol.getV()[u], sol.getW());
                if(c != null) {
                    masterProblemSolver.addColumn(c, u);
                    numNewColumns++;
                }
            }
        }
        //TODO integer oplossen
    }
}