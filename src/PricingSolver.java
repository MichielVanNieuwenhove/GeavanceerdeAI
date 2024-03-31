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
        //waarde in array zal overeen komen met het hosting team voor deze match, lengte is het aantal rondes
        GRBVar[] match =new GRBVar[InputManager.getnRounds()];//FIXME matrix (nTeams*nRounds) --> a_{ir}(s is vast)
        for (int i = 0; i < match.length; i++) {
            model.addVar(0, InputManager.getnTeams()/2, 0, GRB.INTEGER, "round" + i);
        }

//constraints
        GRBLinExpr expr = new GRBLinExpr();//TODO iets met de q's   ??en overlap met andere columns??


//objective
        GRBLinExpr expr_obj = new GRBLinExpr();
        //TODO v_u (dual var)
        expr_obj.addConstant(v_u);
        for(int i = 0; i < InputManager.getnTeams(); i++){
            for(int r = 0; r < InputManager.getnRounds(); r++){
                //TODO term: (int) (match[r]==i)/*airs*/ * w_ir
                expr_obj.addTerm();
            }
        }

        model.setObjective(expr_obj, GRB.MAXIMIZE); //maximize --> >d_s
        model.update();
        model.optimize();

        //TODO make column based on gurobi's output
        return column;
    }
}
