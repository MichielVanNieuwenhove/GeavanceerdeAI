import gurobi.*;


public class Main {
    private final static String inputFilename = "Instances/umps10C.txt";
    public static final int q1 = 5;  //An umpire crew must wait q1-1 rounds before revisiting a team's home {0-nUmpires}
    public static final int q2 = 2;  //An umpire crew must wait q2-1 rounds before officiating the same team again {0-floor(nUmpires/2)}

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
        System.out.println("\nUB: " + UB);
        System.out.println("LB: " + LB);
        System.out.println("solution:");
        int distance = 0;
        for (Column c : UB_sol) {
            System.out.println(c.toSolutionString());
            distance += c.getDistance();
        }
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
            sol = addNewColumns(toFix); //probleem: kan infeasible zijn om 2 redenen:
                                        // - gekozen column is niet mogelijk(goed)
                                        // - er zijn niet genoeg columns om deze column te gebruiken (probleem)
                                        //          omdat dit zo niet op te lossen is kunnen we geen costs bepalen -->
                                        //                          de nodige columns kunnen niet gegenereerd worden
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
//            e.printStackTrace();
            feasible = false;
        }

        if (feasible) {
            boolean stopCriteria =  UB==LB || Math.ceil(sol.getSolutionValue()) < LB;// || intSolValue > UB;

            //selecteren van de column die het verste van een integer value zit om vast te zetten
            double diff = 0.5;
            int ump = 0;
            int column = 0;
            for (int u = 0; u < InputManager.getnUmpires(); u++) {
                for (int c = 0; c < sol.getLambda()[u].size(); c++){
                    double afstandTotHalf = Math.abs(sol.getLambda()[u].get(c) - 0.5);
                    if (afstandTotHalf < diff) {
                        diff = afstandTotHalf;
                        ump = u;
                        column = c;
                    }
                }
            }

            for (int i = 1; i >=0 && !stopCriteria; i--) {
                FixedColumnTreeNode newToFix = new FixedColumnTreeNode(new int[]{ump, column, i}, toFix);
                branchAndPrice(newToFix);
            }
        }
    }
}