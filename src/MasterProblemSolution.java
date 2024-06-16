import java.util.List;

public class MasterProblemSolution {
    private List<Double>[] lambda;
    private double[] v;
    private double[][] w;
    private double solutionValue;

    MasterProblemSolution(List<Double>[] lambda, double[] v, double[][] w, double sol) {
        this.lambda = lambda;
        this.v = v;
        this.w = w;
        solutionValue = sol;
    }


    public List<Double>[] getLambda() {
        return lambda;
    }

    public double[] getV() {
        return v;
    }

    public double[][] getW() {
        return w;
    }

    public double getSolutionValue() {
        return solutionValue;
    }
}
