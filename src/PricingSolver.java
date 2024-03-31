import gurobi.*;

public class PricingSolver {
    /**
     *
     *
     * @return generated column > d_s
     * @throws GRBException
     */
    public static Column gurobi() throws GRBException {
        GRBEnv env = new GRBEnv(true);
        env.set("logFile", "mip1.log");
        env.start();

        GRBModel model = new GRBModel(env);

//beslissings Var:
        //komt overeen met a_{i, r, s} uit de paper
        GRBVar[][] match =new GRBVar[InputManager.getnRounds()][InputManager.getnTeams()];
        for (int i = 0; i < InputManager.getnTeams(); i++) {
            for (int r = 0; r < InputManager.getnRounds(); r++) {
                model.addVar(0, 1, 0, GRB.BINARY, "a_{" + i + ", " + r + "}");
            }
        }

//constraints
        GRBLinExpr[] constr1LocPerRound = new GRBLinExpr[InputManager.getnRounds()];
        for (int r = 0; r < InputManager.getnRounds(); r++) {
            constr1LocPerRound[r] = new GRBLinExpr();
            int aantalTeamsToRef = 0;
            for (int i = 0; i < InputManager.getnTeams(); i++) {
                constr1LocPerRound[r].addTerm(1, match[i][r]);
            }
            model.addConstr(constr1LocPerRound[r], GRB.EQUAL, 1, "refer exactly 1 team in round" + r);
        }

        GRBLinExpr expr = new GRBLinExpr();//TODO iets met de q's


//objective
        GRBLinExpr expr_obj = new GRBLinExpr();
        //TODO v_u (dual var)
        expr_obj.addConstant(v_u);
        for(int i = 0; i < InputManager.getnTeams(); i++){
            for(int r = 0; r < InputManager.getnRounds(); r++){
                //TODO term: (int) (match[r]==i)/*airs*/ * w_ir
                expr_obj.addTerm(w_ir, match[i][r]);
            }
        }

        model.setObjective(expr_obj, GRB.MAXIMIZE); //maximize --> >d_s
        model.update();
        model.optimize();//TODO enkel een oplossing i.p.v. de optimale?

        //TODO make column based on gurobi's output
        return column;
    }
}
