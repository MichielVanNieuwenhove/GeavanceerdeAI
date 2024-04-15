import gurobi.*;

import static java.lang.Math.max;

public class PricingSolver {
    /**
     * @param umpire <p>umpire &#x2208 [0, InputManager.getnUmpires()[ --> what umpire to make a column for (wat is the start location)</p>
     *
     * @return generated column > d_s or null if no improvements
     * @throws GRBException gurobi exception
     */
    public static Column gurobi(int umpire, int v_u, int [][] w) throws GRBException {
        GRBEnv env = new GRBEnv(true);
//        env.set("logFile", "mip1.log"); TODO needed?
        env.start();

        GRBModel model = new GRBModel(env);

//beslissings Var:
        //komt overeen met a_{i, r, s} uit de paper
        GRBVar[][] a_s = new GRBVar[InputManager.getnTeams()][InputManager.getnRounds()];
        for (int i = 0; i < InputManager.getnTeams(); i++) {
            for (int r = 0; r < InputManager.getnRounds(); r++) {
                a_s[i][r] = model.addVar(0, 1, 0, GRB.BINARY, "a_{" + i + ", " + r + "}");
            }
        }

//constraints TODO  ??1ste locatie vast leggen gebaseerd op input var??
        GRBLinExpr constrStartLoc = new GRBLinExpr();
        //-1 om naar index te gaan (in plaat van team nr)
        int startTeam = InputManager.getGames()[0][umpire][0] - 1;
        constrStartLoc.addTerm(1, a_s[startTeam][0]);
        model.addConstr(constrStartLoc, GRB.EQUAL, 1,
                "Starting location"
        );

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

        //visit every location at least once --> constraint 4 TODO enkel door rounds gaan waar team i host?
        GRBLinExpr[] constrLocMinOnce = new GRBLinExpr[InputManager.getnTeams()];
        for (int i = 0; i < InputManager.getnTeams(); i++) {
            constrLocMinOnce[i] = new GRBLinExpr();
            for (int r = 0; r < InputManager.getnRounds(); r++) {
                constrLocMinOnce[i].addTerm(1, a_s[i][r]);
            }
            model.addConstr(constrLocMinOnce[i], GRB.GREATER_EQUAL, 1,
                    "Visit every team at least once"
            );
        }


        //vorige Main.q - 1 teams mogen niet gelijk zijn aan 1 van de huidige teams:
        //  a[i][r] + sum(a[i][r-q]) <= 1       LETOP!! r-q > 0 (index out of bounds)
        //                                              bij q2 ook opletten voor awaiy teams
        //                                              --> a[i][r] -> thuis team te vergelijken met -Inputmanager.oponents[r][i]

        //TODO enkel voor home team voor elke ronde --> ook vanaf round 1 beginnen i.p.v. round q1
        GRBLinExpr[][] constrQ1 = new GRBLinExpr[InputManager.getnTeams()][InputManager.getnRounds()];//q1
        //direct beginnen bij round q1-1 omdat deze ook rekening houdt met alle vorige rondes
        for (int r = max(Main.q1 - 1, 0); r < InputManager.getnRounds(); r++) {
            for (int i = 0; i < InputManager.getnTeams(); i++) {
                constrQ1[i][r] = new GRBLinExpr();
                //loop ook stoppen als index out of bound zou gaan (voor r)
                for (int q = 0; q < Main.q1 && q <= r; q++) {
                    constrQ1[i][r].addTerm(1, a_s[i][r - q]);
                }

                model.addConstr(constrQ1[i][r], GRB.LESS_EQUAL, 1,
                        "only refer at location " + i + " once every " + Main.q1 + " rounds (q1)"
                );
            }
        }

        //TODO enkel voor home en away team voor elke ronde --> ook vanaf round 1 beginnen i.p.v. round q2
//TODO faut in constraints --> infeasible
//        GRBLinExpr[][] constrQ2 = new GRBLinExpr[InputManager.getnTeams()][InputManager.getnRounds()];//q2
//        //direct beginnen bij round q2-1 omdat deze ook rekening houdt met alle vorige rondes
//        for (int r = max(Main.q2 - 1, 0); r < InputManager.getnRounds(); r++) {
//            for (int i = 0; i < InputManager.getnTeams(); i++) {
//                constrQ2[i][r] = new GRBLinExpr();
//                //loop ook stoppen als index out of bound zou gaan (voor r)
//                for (int q = 0; q < Main.q2 && q <= r; q++) {
//                    constrQ2[i][r].addTerm(1, a_s[i][r-q]);
//                    for (int I = 0; I < InputManager.getnTeams(); I++) {
//                        constrQ2[i][r].addConstant(i + 1==-InputManager.getOpponent(I, r-q)?1:0);
//                    }
//                }
//
//                model.addConstr(constrQ2[i][r], GRB.LESS_EQUAL, 1,
//                        "only refer team " + i + " once every " + Main.q2 + " rounds (q2)"
//                );
//            }
//        }
//
//        GRBLinExpr[][] constrHomeOnly = new GRBLinExpr[InputManager.getnTeams()][InputManager.getnRounds()];//only at home locations
//        for (int i = 0; i < InputManager.getnTeams(); i++) {
//            for (int r = Main.q2 - 1; r < InputManager.getnRounds(); r++) {
//                constrHomeOnly[i][r] = new GRBLinExpr();
//                constrHomeOnly[i][r].addTerm(1, a_s[i][r]);
//                int equal = InputManager.getOpponent(i, r) > 0 ? 1 : 0;
//                model.addConstr(constrHomeOnly[i][r], GRB.EQUAL, equal,
//                        "only refer when game is hosted at loc " + i + " in round " + r
//                );
//            }
//        }


//objective
        GRBLinExpr expr_obj = new GRBLinExpr();
        //TODO v_u (dual var)
        expr_obj.addConstant(v_u);
        for(int i = 0; i < InputManager.getnTeams(); i++){
            for(int r = 0; r < InputManager.getnRounds(); r++){
                //TODO w_ir (dual var)
                expr_obj.addTerm(w[i][r], a_s[i][r]);
            }
        }

        model.setObjective(expr_obj, GRB.MAXIMIZE); //maximize --> obj > d_s
        model.update();
        model.optimize();

        //TODO d_s bepalen
        double d_s = 0.0;//double om te kunnen vergelijken met objective value

        //construct column based on gurobi output
        int [][] a = new int[InputManager.getnTeams()][InputManager.getnRounds()];
        for (int i = 0; i < InputManager.getnTeams(); i++) {
            for (int r = 0; r < InputManager.getnRounds(); r++) {
                a[i][r] = (int) a_s[i][r].get(GRB.DoubleAttr.X);
            }
        }
        Column column = new Column(a);
        //return null if distance requirement is not met
        if(model.get(GRB.DoubleAttr.ObjBound) <= column.getDistance()){
            column = null;
        }

        model.dispose();
        env.dispose();
        return column;
    }
}
