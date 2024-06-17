import java.util.ArrayList;
import java.util.List;

public class ColumnGeneratorDFS {
    double v_u;
    double[][] w;

    public ColumnGeneratorDFS(double v_u, double[][] w) {
        this.v_u = v_u;
        this.w = w;
    }

    public Column DFS(ColumnGenNode node, int depth) {
        //TODO
        //  maak alle mogelijk child nodes
        List<ColumnGenNode> childNodes = new ArrayList<>(InputManager.getnTeams()/2);
        for (int gameNr = 0; gameNr < InputManager.getnTeams()/2; gameNr++) {
            int[] game = InputManager.getGames()[depth][gameNr];
            ColumnGenNode addingCandidate = new ColumnGenNode(
                    node,
                    game,
                    w[game[0]][depth]
            );
//            if (/*TODO check of dit een feasible game is (q1 en q2)*/) {
//                childNodes.add(addingCandidate);
//            }
        }
        for (ColumnGenNode childNode: childNodes) {
            DFS(childNode, depth + 1);
        }
        return null;//TODO
    }
}
