import gurobi.*;

import java.util.ArrayList;
import java.util.List;

public class PricingSolver {
    /**
     * @param umpire <p>umpire &#x2208 [0, InputManager.getnUmpires()[ --> what umpire to make a column for (wat is the start location)</p>
     *
     * @return generated column > d_s or null if no improvements
     * @throws GRBException gurobi exception
     */
    public static Column gurobi(int umpire) throws GRBException {//TODO param voor welke umpire? (dus welke game er de 1ste round gespeeld wordt)
        GRBEnv env = new GRBEnv(true);
//        env.set("logFile", "mip1.log"); TODO needed?
        env.start();

        GRBModel model = new GRBModel(env);

//beslissings Var:
        //komt overeen met a_{i, r, s} uit de paper
        GRBVar[][] a_s =new GRBVar[InputManager.getnRounds()][InputManager.getnTeams()];
        for (int i = 0; i < InputManager.getnTeams(); i++) {
            for (int r = 0; r < InputManager.getnRounds(); r++) {
                a_s[i][r] = model.addVar(0, 1, 0, GRB.BINARY, "a_{" + i + ", " + r + "}");
            }
        }

//constraints TODO met de q's, enkel op locaties waar teams thuis spelen, ??1ste locatie vast leggen gebaseerd op input var??
        GRBLinExpr[] constr1LocPerRound = new GRBLinExpr[InputManager.getnRounds()];
        for (int r = 0; r < InputManager.getnRounds(); r++) {
            constr1LocPerRound[r] = new GRBLinExpr();
            int aantalTeamsToRef = 0;
            for (int i = 0; i < InputManager.getnTeams(); i++) {
                constr1LocPerRound[r].addTerm(1, a_s[i][r]);
            }
            model.addConstr(constr1LocPerRound[r], GRB.EQUAL, 1, "refer exactly 1 team in round" + r);
        }

        GRBLinExpr expr = new GRBLinExpr();


//objective
        GRBLinExpr expr_obj = new GRBLinExpr();
        //TODO v_u (dual var)
        expr_obj.addConstant(v_u);
        for(int i = 0; i < InputManager.getnTeams(); i++){
            for(int r = 0; r < InputManager.getnRounds(); r++){
                //TODO w_ir (dual var)
                expr_obj.addTerm(w_ir, a_s[i][r]);
            }
        }

        model.setObjective(expr_obj, GRB.MAXIMIZE); //maximize --> obj > d_s
        model.update();
        model.optimize();//TODO ??enkel een oplossing i.p.v. de optimale??

        //TODO d_s bepalen
        double d_s = 0.0;//double om te kunnen vergelijken met objective value

        //construct column based on gurobi output
        Column column = new Column(/*TODO*/);
        //return null if distance requirement is not met
        if(model.getObjective(0).getValue() <= column.getDistance()){
            column = null;
        }

        model.dispose();
        env.dispose();
        return column;
    }
}
