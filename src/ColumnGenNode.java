public class ColumnGenNode {
    private final ColumnGenNode previous;
    private final int[] game;
    private final double costReductionCumul;
    private final int distanceCumul;
// cost: v_u + w[i][r] * a_s[i][r] - distance   --> maximize
    public ColumnGenNode(ColumnGenNode prev, int[] game, double costIncrease) {
        this.previous = prev;
        this.game = game;
        costReductionCumul = prev.costReductionCumul + costIncrease;
        distanceCumul = prev.distanceCumul + InputManager.getDist(prev.game[0], game[0]);
    }

    ColumnGenNode(int umpireId, double v_u, double[][] w, int depth) {
        this.previous = null;
        this.game = InputManager.getGames()[0][umpireId];
        costReductionCumul = v_u + w[game[0]][depth];
        distanceCumul = 0;
    }

    public ColumnGenNode getPrevious() {
        return previous;
    }

    public int[] getGame() {
        return game;
    }

    public double getCostReductionCumul() {
        return costReductionCumul;
    }

    public int getDistanceCumul() {
        return distanceCumul;
    }
}
