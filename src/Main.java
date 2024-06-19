import gurobi.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    private final static String inputFilename = "Instances/umps8C.txt";
    public static final int q1 = 4;  //An umpire crew must wait q1-1 rounds before revisiting a team's home {0-nUmpires}
    public static final int q2 = 2;  //An umpire crew must wait q2-1 rounds before officiating the same team again {0-floor(nUmpires/2)}

    private static int LB = 0;
    private static int UB = Integer.MAX_VALUE;
    private static Column[] UB_sol = null;

    public static void main(String[] args) throws GRBException {
        InputManager inputManager = new InputManager();
        inputManager.readInput(inputFilename);
        InputManager.print();

        MasterProblemSolver.init();
        MasterProblemSolution sol = RMP();

        System.out.println("\nUB: " + UB);
        System.out.println("LB: " + LB);
        System.out.println("solution:");
        System.out.println(Arrays.toString(sol.getLambda()));

        if (is_integer_solution(sol)){
            System.out.println("Solution value: " + LB);
            for (int r = 0; r < InputManager.getnRounds(); r++) {
                for (Column col : UB_sol) {
                    System.out.print("[" + col.getGame(r, 0) + ", " + col.getGame(r, 1) + "]\t");
                }
                System.out.println("");
            }
        }
        else branch_and_price(sol);

    }

    private static MasterProblemSolution RMP() throws GRBException {
        int numNewColumns = 1;
        MasterProblemSolution sol = null;
        while (numNewColumns > 0){
            sol = MasterProblemSolver.gurobi();
            numNewColumns = 0;
            for (int u = 0; u < InputManager.getnUmpires(); u++) {
                Column c = ColumnGenerator.gurobi(u, sol.getV()[u], sol.getW());
                if(c != null) {
                    MasterProblemSolver.addColumn(c, u);
                    numNewColumns++;
                }
            }
        }

        LB = (int) sol.getSolutionValue();
        Column[] sol_int = MasterProblemSolver.gurobiInt();
        int distance = 0;
        for (Column c : sol_int) {
            System.out.println(c.toSolutionString());
            distance += c.getDistance();
        }
        UB = distance;
        UB_sol = sol_int;

        return sol;
    }

    public static List<X_ijru> fixed_xijru = new ArrayList<>();
    public static List<Integer> values_fixed_xijru = new ArrayList<>();

    private static void branch_and_price(MasterProblemSolution rmp_solution) throws GRBException {   //TODO: stopcrit && TODO: exploring multiple times the same path
        System.out.println("current relaxed solution value: " + rmp_solution.getSolutionValue());
        System.out.println(Arrays.toString(rmp_solution.getLambda()));
        if (is_integer_solution(rmp_solution)){
            //TODO: update best solution
            System.out.println("currently best feasible solution value: " + rmp_solution.getSolutionValue());
        }
        else{
            // Find the most fractional column for this umpire
            int[] column_idx_ump = find_most_fractional_column(rmp_solution.getLambda());
            Column column = MasterProblemSolver.columns[column_idx_ump[1]].get(column_idx_ump[0]);
            X_ijru[] x_ijru_column = convert_to_xijru_arr(column, column_idx_ump[1]);

            // Branch on the first unfixed variable in this column
            for (X_ijru x : x_ijru_column){
                if (!fixed_xijru.contains(x)){   //TODO: contains should work... but try it first
                    fixed_xijru.add(x);
                    for (int i = 0; i <= 1; i++){
                        values_fixed_xijru.add(i);
                        MasterProblemSolution newSol;
                        try{
                            RMP();
                            newSol = MasterProblemSolver.gurobi();
                        }
                        catch (GRBException e){
                            System.err.println("infeasible model generated!");
                            continue;

                        }
                        branch_and_price(newSol);
                        values_fixed_xijru.remove(values_fixed_xijru.size()-1);
                    }
                    fixed_xijru.remove(fixed_xijru.size() - 1);
                    break;
                }
            }
        }
    }

    private static boolean is_integer_solution(MasterProblemSolution sol){
        for (List<Double> doubles : sol.getLambda()) {
            for (Double aDouble : doubles) {
                if (aDouble > 0 && aDouble < 1) {
                    return false;
                }
            }
        }
        return true;
    }

    private static X_ijru[] convert_to_xijru_arr(Column column, int umpire){
        X_ijru[] x_ijru = new X_ijru[InputManager.getnRounds()-1];
        for (int i = 0; i < InputManager.getnRounds()-1; i++){
            x_ijru[i] = new X_ijru(column.getGame(i, 0), column.getGame(i+1, 0), i, umpire);
        }

        return x_ijru;
    }

    private static int[] find_most_fractional_column(List<Double>[] lambdas){
        int best_idx = 0;
        int best_ump = 0;
        double best_diff = 2;
        for (int u = 0; u < lambdas.length; u++){
            for (int i = 0; i < lambdas[u].size(); i++){
                double diff = Math.abs(lambdas[u].get(i) - 0.5);
                if (diff < best_diff){
                    best_idx = i;
                    best_ump = u;
                    best_diff = diff;
                }
            }
        }

        return new int[] {best_idx, best_ump};
    }
}