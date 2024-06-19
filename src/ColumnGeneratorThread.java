import static java.lang.Math.max;

public class ColumnGeneratorThread extends Thread {
    private final int umpireIndex;
    private final double v_u;
    private final double[][] w;
    private Column result;

    public ColumnGeneratorThread(int umpireIndex, double v_u, double[][] w) {
        this.umpireIndex = umpireIndex;
        this.v_u = v_u;
        this.w = w;
    }

    @Override
    public void run() {
        ColumnGenNode rootNode = new ColumnGenNode(umpireIndex, v_u, w, 0);
        ColumnGeneratorDFS dfs = new ColumnGeneratorDFS(v_u, w);
        dfs.DFS(rootNode, 0);
        result = dfs.getSolution();
    }

    public Column getResult() {
        return result;
    }

    public int getUmpireIndex() {
        return umpireIndex;
    }
}