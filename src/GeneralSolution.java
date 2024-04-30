import gurobi.*;

import java.util.*;

public class GeneralSolution {
    private static int[] H(int r){
        List<Integer> teams = new ArrayList<>();
        for (int i = 0; i < InputManager.getnTeams(); i++){
            if(InputManager.getOpponent(i, r) > 0){
                teams.add(i);
            }
        }
        int[] ret = new int[teams.size()];
        for(int i = 0; i < teams.size(); i++) ret[i] = teams.get(i);
        return ret;
    }

    private static Integer[] delta(int i){
        List<Integer> temp = new ArrayList<>();
        for (int r = 0; r < InputManager.getnRounds(); r++){
            if (InputManager.getOpponent(i, r) > 0){
                temp.add(r);
            }
        }
        Integer[] ret = new Integer[temp.size()];
        for (int r = 0; r < temp.size(); r++) ret[r] = temp.get(r);
        return ret;
    }

    public static Column[] gurobi() throws GRBException {
        GRBEnv env = new GRBEnv();
        env.set("LogToConsole", "0");
        GRBModel model = new GRBModel(env);
        model.getEnv().set(GRB.IntParam.SolutionLimit, 1);


//beslissingsVar
        GRBVar[][][][] x = new GRBVar[InputManager.getnTeams()][InputManager.getnTeams()][InputManager.getnRounds()][InputManager.getnUmpires()];
        for (int i = 0; i < InputManager.getnTeams(); i++){
            for (int j = 0; j < InputManager.getnTeams(); j++){
                for (int r = 0; r < InputManager.getnRounds(); r++){
                    for (int u = 0; u < InputManager.getnUmpires(); u++){
                        x[i][j][r][u] = model.addVar(0, 1, 0, GRB.BINARY, "x_{" + i + " " + j + " " + r + " " + u + "}");
                    }
                }
            }
        }
        GRBVar[][][][] x_f = new GRBVar[InputManager.getnTeams()][InputManager.getnTeams()][InputManager.getnRounds()][InputManager.getnUmpires()];
        for (int i = 0; i < InputManager.getnTeams(); i++){
            for (int j = 0; j < InputManager.getnTeams(); j++){
                for (int r = 0; r < InputManager.getnRounds(); r++){
                    for (int u = 0; u < InputManager.getnUmpires(); u++){
                        x_f[i][j][r][u] = model.addVar(0, 1, 0, GRB.BINARY, "x_f(" + i + " " + j + " " + r + " " + u + ")");
                    }
                }
            }
        }

//constr
        for (int i = 0; i < InputManager.getnTeams(); i++) {
            for (int j = 0; j < InputManager.getnTeams(); j++) {
                for (int r = 0; r < InputManager.getnRounds(); r++) {
                    for (int u = 0; u < InputManager.getnUmpires(); u++) {
                        GRBLinExpr linkerLid = new GRBLinExpr();
                        if(r == 0) {
                            linkerLid.addTerm(1, x[i][j][r][u]);
                        }
                        else {
                            linkerLid.addTerm(1, x[j][i][r-1][u]);
                        }
                        GRBLinExpr rechterLid = new GRBLinExpr();
                        rechterLid.addTerm(1, x_f[i][j][r][u]);
                        model.addConstr(rechterLid, GRB.EQUAL, linkerLid,
                                "link besslisings vars" + " " + i + " " + j + " " + r + " " + u
                        );
                    }
                }
            }
        }

        // 2:
        GRBLinExpr[][] constr2 = new GRBLinExpr[InputManager.getnTeams()][InputManager.getnRounds()];
        for (int i = 0; i < InputManager.getnTeams(); i++) {
            for (int r : delta(i)){
                constr2[i][r] = new GRBLinExpr();
                for (int u = 0; u < InputManager.getnUmpires(); u++){
                    for (int j = 0; j < InputManager.getnTeams(); j++){
                        constr2[i][r].addTerm(1, x_f[i][j][r][u]);
                    }
                }
                model.addConstr(constr2[i][r], GRB.EQUAL, 1,
                        "constr2 " + i + " " + r
                );
            }
        }

        //3:
        GRBLinExpr[][] constr3 = new GRBLinExpr[InputManager.getnRounds()][InputManager.getnUmpires()];
        for (int r = 0; r < InputManager.getnRounds(); r++){
            for (int u = 0; u < InputManager.getnUmpires(); u++){
                constr3[r][u] = new GRBLinExpr();
                for (int i : H(r)){
                    for (int j = 0; j < InputManager.getnTeams(); j++){
                        constr3[r][u].addTerm(1, x_f[i][j][r][u]);
                    }
                }
                model.addConstr(constr3[r][u], GRB.EQUAL, 1,
                        "constr3 " + r + " " + u
                );
            }
        }

        //4:
        GRBLinExpr[][] constr4 = new GRBLinExpr[InputManager.getnTeams()][InputManager.getnUmpires()];
        for (int i = 0; i < InputManager.getnTeams(); i++){
            for (int u = 0; u < InputManager.getnUmpires(); u++){
                constr4[i][u] = new GRBLinExpr();
                for (int r : delta(i)) {
                    for (int j = 0; j < InputManager.getnTeams(); j++){
                        constr4[i][u].addTerm(1, x_f[i][j][r][u]);
                    }
                }
                model.addConstr(constr4[i][u], GRB.GREATER_EQUAL, 1,
                        "constr4 " + i + " " + u
                );
            }
        }

        //5:
        GRBLinExpr[][][] constr5 = new GRBLinExpr[InputManager.getnTeams()][InputManager.getnUmpires()][InputManager.getnRounds() - (Main.q1 - 2)];
        for (int i = 0; i < InputManager.getnTeams(); i++){
            for (int u = 0; u < InputManager.getnUmpires(); u++){
                for (int r = 0; r < InputManager.getnRounds() - (Main.q1 - 1); r++){
                    constr5[i][u][r] = new GRBLinExpr();
                    for (int c = r; c < r + Main.q1/* - 1*/; c++) {
                        List<Integer> delta = List.of(delta(i));
                        if(delta.contains(c)) {
                            for (int j = 0; j < InputManager.getnTeams(); j++) {
                                constr5[i][u][r].addTerm(1, x_f[i][j][c][u]);
                            }
                        }
                    }
                    model.addConstr(constr5[i][u][r], GRB.LESS_EQUAL, 1,
                            "constr5 " + i + " " + u + " " + r
                    );
                }
            }
        }


        //6:
        GRBLinExpr[][][] constr6 = new GRBLinExpr[InputManager.getnTeams()][InputManager.getnUmpires()][InputManager.getnRounds() - (Main.q2 - 1)];
        for (int i = 0; i < InputManager.getnTeams(); i++){
            for (int u = 0; u < InputManager.getnUmpires(); u++){
                for (int r = 0; r < InputManager.getnRounds() - (Main.q2 - 1); r++){
                    constr6[i][u][r] = new GRBLinExpr();
                    for (int c = r; c < r + Main.q2/* - 1*/; c++) {
                        for (int j = 0; j < InputManager.getnTeams(); j++){
                            constr6[i][u][r].addTerm(1, x_f[i][j][c][u]);
                            for (int k = 0; k < InputManager.getnTeams(); k++){
                                if (Math.abs(InputManager.getOpponent(k, c))-1 == i){
                                    constr6[i][u][r].addTerm(1, x_f[k][j][c][u]);
                                }
                            }
                        }
                    }
                    model.addConstr(constr6[i][u][r], GRB.LESS_EQUAL, 1,
                            "constr6 " + i + " " + u + " " + r
                    );
                }
            }
        }

        //7:
        GRBLinExpr[][][] constr7 = new GRBLinExpr[InputManager.getnTeams()][InputManager.getnUmpires()][InputManager.getnRounds()];
        for (int i = 0; i < InputManager.getnTeams(); i++){
            for (int u = 0; u < InputManager.getnUmpires(); u++){
                for (int r = 0; r < InputManager.getnRounds(); r++){
                    List<Integer> delta = List.of(delta(i));
                    if(!delta.contains(r)){
                        constr7[i][u][r] = new GRBLinExpr();
                        for (int j = 0; j < InputManager.getnTeams(); j++){
                            constr7[i][u][r].addTerm(1,  x_f[i][j][r][u]);
                        }
                        model.addConstr(constr7[i][u][r], GRB.EQUAL, 0,
                                "constr7" + i + " " + u + " " + r
                        );
                    }
                }
            }
        }

        //8:
        GRBLinExpr[][][] constr8 = new GRBLinExpr[InputManager.getnTeams()][InputManager.getnUmpires()][InputManager.getnRounds()];
        for (int i = 0; i < InputManager.getnTeams(); i++) {
            for (int u = 0; u < InputManager.getnUmpires(); u++) {
                for (int r = 0; r < InputManager.getnRounds() - 1; r++) {
                    constr8[i][u][r] = new GRBLinExpr();
                    for (int j = 0; j < InputManager.getnTeams(); j++){
                        constr8[i][u][r].addTerm(1, x[j][i][r][u]);
                        constr8[i][u][r].addTerm(-1, x[i][j][r+1][u]);
                    }
                    model.addConstr(constr8[i][u][r], GRB.EQUAL, 0,
                            "constr8 " + i + " " + u + " " + r);
                }
            }
        }

//objective
        GRBLinExpr obj = new GRBLinExpr();
        for (int i = 0; i < InputManager.getnTeams(); i++) {
            for (int j = 0; j < InputManager.getnTeams(); j++) {
                for (int r = 0; r < InputManager.getnRounds(); r++) {
                    for (int u = 0; u < InputManager.getnUmpires(); u++) {
                        obj.addTerm(InputManager.getDist(i, j), x[i][j][r][u]);
                    }
                }
            }
        }

        model.setObjective(obj, GRB.MINIMIZE);
        model.update();
        model.optimize();

        Column[] columns = new Column[InputManager.getnUmpires()];
        for (int u = 0; u < InputManager.getnUmpires(); u++) {
            int[][] a_s = new int[InputManager.getnTeams()][InputManager.getnRounds()];
            for (int i = 0; i < InputManager.getnTeams(); i++) {
                for (int j = 0; j < InputManager.getnTeams(); j++) {
                    for (int r = 0; r < InputManager.getnRounds(); r++) {
                        if(x[i][j][r][u].get(GRB.DoubleAttr.X) == 1){
                            a_s[i][r] = 1;
                        }
                    }
                }
            }
            columns[u] = new Column(a_s);
            System.out.println("column[" + u + "]:\n" + columns[u]);
        }

        model.dispose();
        env.dispose();

        return columns;
    }
}
