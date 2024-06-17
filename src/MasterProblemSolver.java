import gurobi.*;

import java.util.List;
import java.util.ArrayList;

public class MasterProblemSolver {
    static private final List<Column>[] columns = new List[InputManager.getnUmpires()];

    public static void init() throws GRBException {
        for(int u = 0; u < InputManager.getnUmpires(); u++){
            columns[u] = new ArrayList<>();
        }
        Column[] c = GeneralSolution.gurobi();
        for (int i = 0; i < c.length; i++) {
            columns[i].add(c[i]);
//            System.out.println(columns[i].get(0).toSolutionString());
        }
    }

    public static void addColumn(Column column, int umpire){
        columns[umpire].add(column);
    }

    /**
     *
     * @param columnsToFix: a list of int[3]: [umpireNr, columnNr, 0 of 1]
     * @return MasterProblemSolution
     * @throws GRBException
     */
    public static MasterProblemSolution gurobi(FixedColumnTreeNode columnsToFix) throws GRBException {
        GRBEnv env = new GRBEnv(true);
        env.start();
//        env.set("LogToConsole", "0");
        GRBModel model = new GRBModel(env);

//beslissings Var:
        List<GRBVar>[] lambda = new List[InputManager.getnUmpires()];
        for(int u = 0; u < InputManager.getnUmpires(); u++){
            lambda[u] = new ArrayList<>(columns[u].size());
            for(int s = 0; s < columns[u].size(); s++){
                //continuous omdat we een lineare relaxatie moeten oplossen om een duale cost te bepalen
                lambda[u].add(
                        model.addVar(0, 1, 0, GRB.CONTINUOUS,
                                "lambda_" + u + s
                        )
                );
            }
        }


//constraints:
        //11: only one column is chosen per umpire
        GRBLinExpr[] constrOneColumn = new GRBLinExpr[InputManager.getnUmpires()];
        for (int u = 0; u < InputManager.getnUmpires(); u++) {
            constrOneColumn[u] = new GRBLinExpr();
            for (int s = 0; s < columns[u].size(); s++){
                constrOneColumn[u].addTerm(1, lambda[u].get(s));
            }
            model.addConstr(constrOneColumn[u], GRB.EQUAL, 1,
                    "v " + u
            );
        }

        //12:
        GRBLinExpr[][] constrOneUmpPerTeam = new GRBLinExpr[InputManager.getnTeams()][InputManager.getnRounds()];
        for (int i = 0; i < InputManager.getnTeams(); i++) {
            for (int r = 0; r < InputManager.getnRounds(); r++) {
                if (InputManager.isHost(i, r)) {
                    constrOneUmpPerTeam[i][r] = new GRBLinExpr();
                    for (int u = 0; u < InputManager.getnUmpires(); u++) {
                        for (int s = 0; s < columns[u].size(); s++) {
                            constrOneUmpPerTeam[i][r].addTerm(columns[u].get(s).getA_s(i, r), lambda[u].get(s));
                        }
                    }
                    model.addConstr(constrOneUmpPerTeam[i][r], GRB.EQUAL, 1,
                            "w " + i + " " + r
                    );
                }
            }
        }

        //vaste columns:
        while (columnsToFix != null) {
            int[] toFix = columnsToFix.getFixedColumn();
            GRBLinExpr expr = new GRBLinExpr();
            expr.addTerm(1, lambda[toFix[0]].get(toFix[1]));
            model.addConstr(expr, GRB.EQUAL, toFix[2],
                    "fixed column" + toFix[1] + "for umpire" + toFix[0] + "to" + toFix[2]
            );
            columnsToFix = columnsToFix.getPrevious();
        }


//Objective:
        GRBLinExpr expr_obj = new GRBLinExpr();
        for (int u = 0; u < InputManager.getnUmpires(); u++){
            for (int s = 0; s < columns[u].size(); s++){
                expr_obj.addTerm(columns[u].get(s).getDistance(), lambda[u].get(s));
            }
        }
        model.setObjective(expr_obj, GRB.MINIMIZE);
        model.update();
//        model = model.relax();
        model.optimize();

        List<Double>[] lambda_solution = new List[InputManager.getnUmpires()];
        for (int u = 0; u < InputManager.getnUmpires(); u++){
            lambda_solution[u] = new ArrayList<>(columns[u].size());
            for (int s = 0; s < columns[u].size(); s++){
                GRBVar lambda_us = lambda[u].get(s);
                lambda_solution[u].add(lambda_us.get(GRB.DoubleAttr.X));
            }
        }


        double[] v = new double[InputManager.getnUmpires()];
        double[][]w = new double[InputManager.getnTeams()][InputManager.getnRounds()];
        GRBConstr[] constr = model.getConstrs();
        for (GRBConstr grbConstr : constr) {
            String constrName = grbConstr.get(GRB.StringAttr.ConstrName);
            String[] constrParts = constrName.split(" ");
            if (constrParts[0].equals("v")) {
                v[Integer.parseInt(constrParts[1])] = grbConstr.get(GRB.DoubleAttr.Pi);
            }
            if (constrParts[0].equals("w")) {
                w[Integer.parseInt(constrParts[1])][Integer.parseInt(constrParts[2])] = grbConstr.get(GRB.DoubleAttr.Pi);
            }

        }
        double sol = model.get(GRB.DoubleAttr.ObjBound);
//        System.out.println("lin: " + model.get(GRB.DoubleAttr.ObjBound));

        model.dispose();
        env.dispose();
        return new MasterProblemSolution(lambda_solution/*null*/, v, w, sol);
    }

    public static Column[] gurobiInt() throws GRBException {
        GRBEnv env = new GRBEnv(true);
        env.start();
        env.set("LogToConsole", "0");
        GRBModel model = new GRBModel(env);
//        for (int u = 0; u < InputManager.getnUmpires(); u++) {
//            for (Column column : columns[u]) {
//                System.out.println(column.toSolutionString());
//            }
//        }

//beslissings Var:
        List<GRBVar>[] lambda = new List[InputManager.getnUmpires()];
        for(int u = 0; u < InputManager.getnUmpires(); u++){
            lambda[u] = new ArrayList<>(columns[u].size());
            for(int s = 0; s < columns[u].size(); s++){
                //continuous omdat we een lineare relaxatie moeten oplossen om een duale cost te bepalen
                lambda[u].add(
                        model.addVar(0, 1, 0, GRB.BINARY,
                                "lambda_" + u + s
                        )
                );
            }
        }


//constraints:
        //11: only one column is chosen per umpire
        GRBLinExpr[] constrOneColumn = new GRBLinExpr[InputManager.getnUmpires()];
        for (int u = 0; u < InputManager.getnUmpires(); u++) {
            constrOneColumn[u] = new GRBLinExpr();
            for (int s = 0; s < columns[u].size(); s++){
                constrOneColumn[u].addTerm(1, lambda[u].get(s));
            }
            model.addConstr(constrOneColumn[u], GRB.EQUAL, 1,
                    "v " + u
            );
        }

        //12:
        GRBLinExpr[][] constrOneUmpPerTeam = new GRBLinExpr[InputManager.getnTeams()][InputManager.getnRounds()];
        for (int i = 0; i < InputManager.getnTeams(); i++) {
            for (int r = 0; r < InputManager.getnRounds(); r++) {
                if (InputManager.isHost(i, r)) {
                    constrOneUmpPerTeam[i][r] = new GRBLinExpr();
                    for (int u = 0; u < InputManager.getnUmpires(); u++) {
                        for (int s = 0; s < columns[u].size(); s++) {
                            constrOneUmpPerTeam[i][r].addTerm(columns[u].get(s).getA_s(i, r), lambda[u].get(s));
                        }
                    }
                    model.addConstr(constrOneUmpPerTeam[i][r], GRB.EQUAL, 1,
                            "w " + i + " " + r
                    );
                }
            }
        }


//Objective:
        GRBLinExpr expr_obj = new GRBLinExpr();
        for (int u = 0; u < InputManager.getnUmpires(); u++){
            for (int s = 0; s < columns[u].size(); s++){
                expr_obj.addTerm(columns[u].get(s).getDistance(), lambda[u].get(s));
            }
        }
        model.setObjective(expr_obj, GRB.MINIMIZE);
        model.update();
        model.optimize();

        List<Boolean>[] lambda_solution = new List[InputManager.getnUmpires()];
        for (int u = 0; u < InputManager.getnUmpires(); u++){
            lambda_solution[u] = new ArrayList<>(columns[u].size());
            for (int s = 0; s < columns[u].size(); s++){
                lambda_solution[u].add(lambda[u].get(s).get(GRB.DoubleAttr.X) == 1);
            }
        }


        Column[] chosen = new Column[InputManager.getnUmpires()];
        for (int u = 0; u < InputManager.getnUmpires(); u++){
            for (int s = 0; s < columns[u].size(); s++){
                if(lambda[u].get(s).get(GRB.DoubleAttr.X) == 1){
                    chosen[u] = columns[u].get(s);
                }
            }
        }
        System.out.println("int: " + model.get(GRB.DoubleAttr.ObjBound));


        model.dispose();
        env.dispose();
        return chosen;
    }

}
