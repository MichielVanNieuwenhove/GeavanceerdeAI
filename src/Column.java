public class Column {
    private int[][] games;
    private int distance;

    public Column(){}

    public Column(int[][] games, int distance){//TODO distance berekenen in de constructor i.p.v. via param
        this.games = games;
        this.distance = distance;
    }

    //getters & setters
    public void setGames(int[][] games) {
        this.games = games;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int[][] getGames() {
        return games;
    }

    public int getDistance() {
        return distance;
    }
}
