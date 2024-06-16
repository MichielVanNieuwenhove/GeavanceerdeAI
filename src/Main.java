import gurobi.*;


public class Main {
    private final static String inputFilename = "Instances/umps4.txt";
    public static final int q1 = 3;  //An umpire crew must wait q1-1 rounds before revisiting a team's home {0-nUmpires}
    public static final int q2 = 1;  //An umpire crew must wait q2-1 rounds before officiating the same team again {0-floor(nUmpires/2)}

    private static int LB = 0;
    private static int UB = Integer.MAX_VALUE;
    private static Column[] UB_sol = null;

    public static void main(String[] args) throws GRBException {
        InputManager inputManager = new InputManager();
        inputManager.readInput(inputFilename);
        InputManager.print();

        long startTime = System.currentTimeMillis();

        MasterProblemSolver.init();
        long startTime2 = System.currentTimeMillis();
        branchAndPrice(null);
//        Column[] finalSolution = MasterProblemSolver.gurobiInt();
        System.out.println("run time: " + (System.currentTimeMillis() - startTime) + "ms");
        System.out.println("run time after initial solution: " + (System.currentTimeMillis() - startTime2) + "ms");
//        int distance = 0;
//        for (Column c : finalSolution) {
//            System.out.println(c.toSolutionString());
//            distance += c.getDistance();
//        }
//        System.out.println("distance: " + distance);
    }

    private static MasterProblemSolution addNewColumns(FixedColumnTreeNode toFix) throws GRBException {
        MasterProblemSolution sol = null;
        int numNewColumns = 1;
        while (numNewColumns > 0){
            sol = MasterProblemSolver.gurobi(toFix);
            numNewColumns = 0;
            for (int u = 0; u < InputManager.getnUmpires(); u++) {
                Column c = ColumnGenerator.gurobi(u, sol.getV()[u], sol.getW());
                if(c != null) {
                    MasterProblemSolver.addColumn(c, u);
                    numNewColumns++;
                }
            }
        }
        return sol;
    }

    private static void branchAndPrice(FixedColumnTreeNode toFix) {
        boolean feasible = true;
        MasterProblemSolution sol = null;
        Column[] intSol = null;
        int intSolValue = Integer.MAX_VALUE;

        try {
            sol = addNewColumns(toFix);
            intSol = MasterProblemSolver.gurobiInt();
            intSolValue = 0;
            for (Column c: intSol){
                intSolValue += c.getDistance();
            }

            LB = Math.max((int) Math.ceil(sol.getSolutionValue()), LB);
            if (intSolValue < UB) {
                UB = intSolValue;
                UB_sol = intSol.clone();
            }
        }
        catch (GRBException e) {
            e.printStackTrace();
            feasible = false;
        }

        if (feasible) {
            boolean stopCriteria =  UB==LB || Math.ceil(sol.getSolutionValue()) < LB || intSolValue > UB;

            int ump = 0;//TODO goede ump selecteren         (based on sol)
            int column = 0;//TODO goede column selecteren
            for (int i = 0; i <= 1 && !stopCriteria; i++) {
                FixedColumnTreeNode newToFix = new FixedColumnTreeNode(new int[]{ump, column, i}, toFix);
                branchAndPrice(newToFix);
            }
        }
    }
}