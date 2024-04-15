public class Column {
//    private int[][] games;
    private int [][] a_s;
    private int distance;

    public Column(){}//TODO nodig?

    public Column(int[][] a_s){
        this.a_s = a_s;
//        this.distance = //TODO;
    }

    //getters & setters
//    public void setGames(int[][] games) {
//        this.games = games;
//    }

//    public void setDistance(int distance) {
//        this.distance = distance;
//    }

//    public int[][] getGames() {
//        return games;
//    }

    public int getDistance() {
        return distance;
    }

    public String toString(){
        String s = "";
        for (int i = 0; i < a_s.length; i++) {
            s += "[";
            for (int j = 0; j < a_s[i].length; j++) {
                s += a_s[i][j] + ", ";
            }
            s += "]\n";
        }
        return s;
    }
}
