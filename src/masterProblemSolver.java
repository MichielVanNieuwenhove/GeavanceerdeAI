import gurobi.*;

import java.util.List;
import java.util.ArrayList;

public class masterProblemSolver {
    static private final List<Column>[] columns = new List[InputManager.getnUmpires()];

    public void init(){
        for(int u = 0; u < InputManager.getnUmpires(); u++){
            columns[u] = new ArrayList<>();
            //TODO generate initial columns
        }
    }

    public static MasterProblemSolution gurobi() throws GRBException {
        GRBEnv env = new GRBEnv(true);
        env.start();

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
            for (int s = 0; s < constrOneColumn.length; s++){
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
                lambda_solution[u].add(lambda[u].get(s).get(GRB.DoubleAttr.X) == 1);//LETOP fouten met afrondingen? (1==0.9999)
            }
        }


        double[] v = new double[InputManager.getnUmpires()];
        double[][]w = new double[InputManager.getnTeams()][InputManager.getnRounds()];
        double[] duals = model.get(GRB.DoubleAttr.Pi, model.getConstrs());
        GRBConstr[] constr = model.getConstrs();
        for (int dual = 0; dual < duals.length; dual++){
            String constrName = constr[dual].get(GRB.StringAttr.ConstrName);
            String[] constrParts = constrName.split(" ");
            if(constrParts[0].equals("v")){
                v[Integer.parseInt(constrParts[1])] = duals[dual];
            }
            if(constrParts[0].equals("w")){
                w[Integer.parseInt(constrParts[1])][Integer.parseInt(constrParts[2])] = duals[dual];
            }

        }


        model.dispose();
        env.dispose();
        return new MasterProblemSolution(lambda_solution, v, w);
    }

}
