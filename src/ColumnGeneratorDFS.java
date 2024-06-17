import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ColumnGeneratorDFS {
    double v_u;
    double[][] w;
    Column bestSolution = null;
    double bestSolutionCostReductie = Double.MIN_VALUE;

    public ColumnGeneratorDFS(double v_u, double[][] w) {
        this.v_u = v_u;
        this.w = w;
    }

    private boolean feasibleQ1Q2(ColumnGenNode node) {
        int home = node.getGame()[0];
        int away = node.getGame()[1];
        boolean feasible = true;

        //check Q1 constr
        ColumnGenNode checkingNode = node.getPrevious();
        for (int q = 1; q < Main.q1 && checkingNode != null && feasible; q++){
            feasible = home != checkingNode.getGame()[0];

            checkingNode = checkingNode.getPrevious();
        }

        //check Q2 constr
        checkingNode = node.getPrevious();
        for (int q = 1; q < Main.q2 && checkingNode != null && feasible; q++){
            feasible =  home != checkingNode.getGame()[0] &&
                    away != checkingNode.getGame()[0] &&
                    home != checkingNode.getGame()[1] &&
                    away != checkingNode.getGame()[1];

            checkingNode = checkingNode.getPrevious();
        }

        return feasible;
    }

    public void DFS(ColumnGenNode node, int round) {
        if (node == null) return;
        //TODO bounding     best distance kan uit bestSolution gehaalt worden   shortest distance uit InputManager.getShortestDistance()
        List<ColumnGenNode> childNodes = new ArrayList<>(InputManager.getnTeams()/2);
        if (round + 1 < InputManager.getnRounds()) {
            for (int gameNr = 0; gameNr < InputManager.getnTeams() / 2; gameNr++) {
                int[] game = InputManager.getGames()[round + 1][gameNr];
                ColumnGenNode addingCandidate = new ColumnGenNode(
                        node,
                        game,
                        w[game[0]][round + 1]
                );
                if (feasibleQ1Q2(addingCandidate)) {
                    childNodes.add(addingCandidate);
                }
            }
        }
        childNodes.sort(Comparator.comparingInt(ColumnGenNode::getDistanceCumul));

        for (ColumnGenNode childNode: childNodes) {
            DFS(childNode, round + 1);
        }

        // -1 omdat round een index is
        if(round == InputManager.getnRounds() - 1) {
            int[] aantalKeerVisited = new int[InputManager.getnTeams()];
            ColumnGenNode checkingNode = node;
            int[][] games = new int[InputManager.getnRounds()][2];
            int roundNr = round;
            while (checkingNode != null) {
                aantalKeerVisited[checkingNode.getGame()[0]] += 1;

                games[roundNr] = checkingNode.getGame();

                checkingNode = checkingNode.getPrevious();
                roundNr--;
            }
            boolean feasible = true;
            for (int i = 0; i < aantalKeerVisited.length && feasible; i++) {
                if (aantalKeerVisited[i] == 0) {
                    feasible = false;
                }
            }
            double costreductie = node.getCostReductionCumul() - node.getDistanceCumul();
            if (feasible && bestSolutionCostReductie < costreductie) {
                bestSolution = new Column(games, false);
                bestSolutionCostReductie = costreductie;
            }
        }
        if (round == 0 && bestSolutionCostReductie<0.0){
            bestSolution = null;
        }
    }

    public Column getSolution() {
        return bestSolution;
    }
}
