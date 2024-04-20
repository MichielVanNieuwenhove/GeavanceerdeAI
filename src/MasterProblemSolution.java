import java.util.List;

public class MasterProblemSolution {
    private List<Boolean>[] lambda;
    private double[] v;
    private double[][] w;

    MasterProblemSolution(List<Boolean>[] lambda, double[] v, double[][] w) {
        this.lambda = lambda;
        this.v = v;
        this.w = w;
    }


    public List<Boolean>[] getLambda() {
        return lambda;
    }

    public double[] getV() {
        return v;
    }

    public double[][] getW() {
        return w;
    }
}
