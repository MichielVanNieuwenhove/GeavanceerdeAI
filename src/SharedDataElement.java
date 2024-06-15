public class SharedDataElement {
    private int[] previousQ1Teams = new int[Main.q1];
    private int distance = 0;
    private int costReduction = 0;
    private boolean finished = false;

    SharedDataElement() {}

    public void setPreviousQ1Teams(int[] previousQ1Teams) {
        this.previousQ1Teams = previousQ1Teams;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public void setCostReduction(int costReduction) {
        this.costReduction = costReduction;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public int[] getPreviousQ1Teams() {
        return previousQ1Teams;
    }

    public int getDistance() {
        return distance;
    }

    public int getCostReduction() {
        return costReduction;
    }

    public boolean isFinished() {
        return finished;
    }
}
