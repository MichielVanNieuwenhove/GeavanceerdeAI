import gurobi.*;

import java.util.List;

public class Main {
    private static String inputFilename = "Instances/umps8C.txt";
    public static int q1 = 4;  //An umpire crew must wait q1-1 rounds before revisiting a team's home {0-nUmpires}
    public static int q2 = 2;  //An umpire crew must wait q2-1 rounds before officiating the same team again {0-floor(nUmpires/2)}

    public static void main(String[] args) throws GRBException {
        inputFilename = args[0];
        q1 = Integer.parseInt(args[1]);
        q2 = Integer.parseInt(args[2]);
        boolean useGurobi = Boolean.parseBoolean(args[3]);

        InputManager inputManager = new InputManager();
        inputManager.readInput(inputFilename);
        InputManager.print();

        long startTime = System.currentTimeMillis();

        MasterProblemSolver.init();
        long startTime2 = System.currentTimeMillis();
        int numNewColumns = 1;
        while (numNewColumns > 0){
            MasterProblemSolution sol = MasterProblemSolver.gurobi();
            numNewColumns = 0;
            for (int u = 0; u < InputManager.getnUmpires(); u++) {
                Column c;
                if (useGurobi) {
                    c = ColumnGenerator.gurobi(u, sol.getV()[u], sol.getW());
                }
                else {
                    c = ColumnGenerator.BAndB(u, sol.getV()[u], sol.getW());
                }
                if(c != null) {
                    MasterProblemSolver.addColumn(c, u);
                    numNewColumns++;
                }
            }
        }
        Column[] finalSolution = MasterProblemSolver.gurobiInt();
        System.out.println("run time: " + (System.currentTimeMillis() - startTime) + "ms");
        System.out.println("run time after initial solution: " + (System.currentTimeMillis() - startTime2) + "ms");
//        int distance = 0;
//        for (Column c : finalSolution) {
//            System.out.println(c.toSolutionString());
//            distance += c.getDistance();
//        }
//        System.out.println("distance: " + distance);
    }
}