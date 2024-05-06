import gurobi.*;
public class Main {
    private final static String inputFilename = "Instances/umps6A.txt";
    public static final int q1 = 3;  //An umpire crew must wait q1-1 rounds before revisiting a team's home {0-nUmpires}
    public static final int q2 = 1;  //An umpire crew must wait q2-1 rounds before officiating the same team again {0-floor(nUmpires/2)}

    public static void main(String[] args) throws GRBException {
        InputManager inputManager = new InputManager();
        inputManager.readInput(inputFilename);
        InputManager.print();

        long startTime = System.currentTimeMillis();

        MasterProblemSolver.init();
        int numNewColumns = 1;
        while (numNewColumns > 0){
            MasterProblemSolution sol = MasterProblemSolver.gurobi();
            numNewColumns = 0;
            for (int u = 0; u < InputManager.getnUmpires(); u++) {
                Column c = ColumnGenerator.gurobi(u, sol.getV()[u], sol.getW());
                if(c != null) {
                    MasterProblemSolver.addColumn(c, u);
                    numNewColumns++;
                }
            }
        }
        Column[] finalSolution = MasterProblemSolver.gurobiInt();
        System.out.println("run time: " + (System.currentTimeMillis() - startTime) + "ms");
        int distance = 0;
        for (Column c : finalSolution) {
            System.out.println(c.toSolutionString());
            distance += c.getDistance();
        }
        System.out.println("distance: " + distance);
    }
}