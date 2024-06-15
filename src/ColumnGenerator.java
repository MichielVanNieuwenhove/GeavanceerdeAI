import gurobi.*;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.lang.Math.max;

public class ColumnGenerator {
    /**
     * @param umpire <p>umpire &#x2208 [0, InputManager.getnUmpires()[ --> umpire to make a column for (wat is the start location)</p>
     *
     * @return generated column > d_s or null if no improvements
     * @throws GRBException gurobi exception
     */
    public static Column gurobi(int umpire, double v_u, double [][] w) throws GRBException {
        GRBEnv env = new GRBEnv(true);
        env.start();
        env.set("LogToConsole", "0");
        GRBModel model = new GRBModel(env);

//beslissings Var:
        //komt overeen met a_{i, r, s} uit de paper
        GRBVar[][] a_s = new GRBVar[InputManager.getnTeams()][InputManager.getnRounds()];
        for (int i = 0; i < InputManager.getnTeams(); i++) {
            for (int r = 0; r < InputManager.getnRounds(); r++) {
                a_s[i][r] = model.addVar(0, 1, 0, GRB.BINARY, "a_{" + i + ", " + r + "}");
            }
        }

        GRBVar distance;
        distance = model.addVar(0, Integer.MAX_VALUE, 0, GRB.INTEGER, "distance");

//constraints
        GRBQuadExpr distLink = new GRBQuadExpr();
        for (int r = 1; r < InputManager.getnRounds(); r++) {
            for (int i = 0; i < InputManager.getnTeams(); i++) {
                for (int j = 0; j < InputManager.getnTeams(); j++) {
                    distLink.addTerm(InputManager.getDist(i, j), a_s[i][r], a_s[j][r - 1]);
                }
            }
        }
        model.addQConstr(distLink, GRB.EQUAL, distance, "distance linking");

        GRBLinExpr constrStartLoc = new GRBLinExpr();
        int startTeam = InputManager.getGames()[0][umpire][0];
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

        //visit every location at least once --> constraint 4
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


        GRBLinExpr[][] constrQ2 = new GRBLinExpr[InputManager.getnTeams()][InputManager.getnRounds()];//q2
        //direct beginnen bij round q2-1 omdat deze ook rekening houdt met alle vorige rondes
        for (int r = max(Main.q2 - 1, 0); r < InputManager.getnRounds(); r++) {
            for (int i = 0; i < InputManager.getnTeams(); i++) {
                constrQ2[i][r] = new GRBLinExpr();
                //loop ook stoppen als index out of bound zou gaan (voor r)
                for (int q = 0; q < Main.q2 && q <= r; q++) {
                    constrQ2[i][r].addTerm(1, a_s[i][r-q]);
                    for (int I = 0; I < InputManager.getnTeams(); I++) {
                        constrQ2[i][r].addTerm(i + 1==InputManager.getOpponent(I, r-q)?1:0, a_s[I][r-q]);
                    }
                }

                model.addConstr(constrQ2[i][r], GRB.LESS_EQUAL, 1,
                        "only refer team " + i + " once every " + Main.q2 + " rounds (q2)"
                );
            }
        }

        GRBLinExpr[][] constrHomeOnly = new GRBLinExpr[InputManager.getnTeams()][InputManager.getnRounds()];//only at home locations
        for (int i = 0; i < InputManager.getnTeams(); i++) {
            for (int r = Main.q2 - 1; r < InputManager.getnRounds(); r++) {
                constrHomeOnly[i][r] = new GRBLinExpr();
                constrHomeOnly[i][r].addTerm(1, a_s[i][r]);
                int hosting = InputManager.getOpponent(i, r) > 0 ? 1 : 0;
                model.addConstr(constrHomeOnly[i][r], GRB.LESS_EQUAL, hosting,
                        "only refer when game is hosted at loc " + i + " in round " + r
                );
            }
        }


//objective
        GRBLinExpr expr_obj = new GRBLinExpr();
        expr_obj.addConstant(v_u);
        for(int i = 0; i < InputManager.getnTeams(); i++){
            for(int r = 0; r < InputManager.getnRounds(); r++){
                expr_obj.addTerm(w[i][r], a_s[i][r]);
            }
        }
        expr_obj.addTerm(-1, distance);

        model.setObjective(expr_obj, GRB.MAXIMIZE); //maximize --> obj > d_s
        model.update();
        model.optimize();

        //construct column based on gurobi output
        int [][] a = new int[InputManager.getnTeams()][InputManager.getnRounds()];
        for (int i = 0; i < InputManager.getnTeams(); i++) {
            for (int r = 0; r < InputManager.getnRounds(); r++) {
                a[i][r] =  a_s[i][r].get(GRB.DoubleAttr.X) < 0.8 ? 0 : 1;
            }
        }
        Column column = new Column(a);
        //return null if distance requirement is not met
        if(model.get(GRB.DoubleAttr.ObjBound) <= 0){
            column = null;
        }

        model.dispose();
        env.dispose();
        return column;
    }


//    public static Column BAndB(int umpire, double v_u, double [][] w){
//        ReadWriteLock lock = new ReentrantReadWriteLock();
//        SharedDataElement data = new SharedDataElement();
//        ColumnGeneratorBFS BFS = new ColumnGeneratorBFS(data, lock, umpire, v_u, w);
//        ColumnGeneratorDFS DFS = new ColumnGeneratorDFS(data, lock, umpire, v_u, w);
//        DFS.start();
//        BFS.start();
//
//        try {
//            DFS.join();
//            BFS.join();
//        }
//        catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        Column col = BFS.getSol();
//        return col;
//    }
}
