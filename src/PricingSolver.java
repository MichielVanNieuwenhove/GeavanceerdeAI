import gurobi.*;

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
        GRBVar[][] a_s = new GRBVar[InputManager.getnRounds()][InputManager.getnTeams()];
        for (int i = 0; i < InputManager.getnTeams(); i++) {
            for (int r = 0; r < InputManager.getnRounds(); r++) {
                a_s[i][r] = model.addVar(0, 1, 0, GRB.BINARY, "a_{" + i + ", " + r + "}");
            }
        }

//constraints TODO  enkel op locaties waar teams thuis spelen
//                  visit every team at least once                          (constraint 4)
//                  ??1ste locatie vast leggen gebaseerd op input var??
        GRBLinExpr[] constr1LocPerRound = new GRBLinExpr[InputManager.getnRounds()];//constraint 7
        for (int r = 0; r < InputManager.getnRounds(); r++) {
            constr1LocPerRound[r] = new GRBLinExpr();
            for (int i = 0; i < InputManager.getnTeams(); i++) {
                constr1LocPerRound[r].addTerm(1, a_s[i][r]);
            }
            model.addConstr(constr1LocPerRound[r], GRB.EQUAL, 1,
                    "refer exactly 1 team in round " + r
            );
        }


        //vorige Main.q - 1 teams mogen niet gelijk zijn aan 1 van de huidige teams:
        //  a[i][r] + sum(a[i][r-q]) <= 1       LETOP!! r-q > 0 (index out of bounds)
        //                                              bij q2 ook opletten voor awaiy teams
        //                                              --> a[i][r] -> thuis team te vergelijken met -Inputmanager.oponents[r][i]

        //TODO enkel voor home team voor elke ronde --> ook vanaf round 1 beginnen i.p.v. round q1
        GRBLinExpr[][] constrQ1 = new GRBLinExpr[InputManager.getnRounds()][InputManager.getnTeams()];//q1
        //direct beginnen bij round q1-1 omdat deze ook rekening houdt met alle vorige rondes
        for (int r = Main.q1 - 1; r < InputManager.getnRounds(); r++) {
            for (int i = 0; i < InputManager.getnTeams(); i++) {
                constrQ1[r][i] = new GRBLinExpr();
                //loop ook stoppen als index out of bound zou gaan (voor r)
                for (int q = 0; q < Main.q1 && q <= r; q++) {
                    constrQ1[r][i].addTerm(1, a_s[i][r - q]);
                }

                model.addConstr(1, GRB.GREATER_EQUAL, constrQ1[r][i],
                        "only refer at location " + i + " once every " + Main.q1 + " rounds (q1)"
                );
            }
        }

        //TODO enkel voor home en away team voor elke ronde --> ook vanaf round 1 beginnen i.p.v. round q2
        GRBLinExpr[][] constrQ2 = new GRBLinExpr[InputManager.getnRounds()][InputManager.getnTeams()];//q2
        //direct beginnen bij round q2-1 omdat deze ook rekening houdt met alle vorige rondes
        for (int r = Main.q2 - 1; r < InputManager.getnRounds(); r++) {
            for (int i = 0; i < InputManager.getnTeams(); i++) {
                constrQ2[r][i] = new GRBLinExpr();
                //loop ook stoppen als index out of bound zou gaan (voor r)
                for (int q = 0; q < Main.q2 && q <= r; q++) {
                    constrQ2[r][i].addTerm(1, a_s[i][r-q]);
                    constrQ2[r][i].addConstant(i==-InputManager.getOpponent(r, i)?1:0);//FIXME klopt ier nog iets niet
                }

                model.addConstr(1, GRB.GREATER_EQUAL, constrQ2[r][i],
                        "only refer team " + i + " once every " + Main.q2 + " rounds (q2)"
                );
            }
        }


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
