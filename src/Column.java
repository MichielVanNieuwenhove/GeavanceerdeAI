import java.util.Arrays;

public class Column {
    private final int[][] games;

    private final int [][] a_s;
    private final int distance;

    /**
     *
     * @param games the games visited by umpire (in correct order)
     * @param onderscheiding extra param used to differentiate between
     */
    public Column(int[][] games, boolean onderscheiding) {
        this.games = games;
        a_s = new int[InputManager.getnTeams()][InputManager.getnRounds()];
        for (int r = 0; r < InputManager.getnRounds(); r++) {
            a_s[games[r][0]][r] = 1;
        }
        distance = calculateDistance();
    }

    public Column(int[][] a_s){
        this.a_s = a_s;
        games = new int[InputManager.getnRounds()][2];
        for (int round = 0; round < InputManager.getnRounds(); round++) {
            int team;
            for (team = 0; team < InputManager.getnTeams(); team++){
                if (this.a_s[team][round] == 1) break;
            }
            games[round][0] = team;
            games[round][1] = InputManager.getOpponent(team, round) - 1;
        }
        distance = calculateDistance();
    }

    private int calculateDistance(){
        int tmpDistance = 0;
        for (int round = 1; round < games.length; round++) {
            tmpDistance += InputManager.getDist(games[round - 1][0], games[round][0]);
        }
        return tmpDistance;
    }

    //getters & setters
    public int getA_s(int i, int r) {
        return a_s[i][r];
    }

    public int getGame(int round, int away) {
        return games[round][away];
    }

    public int getDistance() {
        return distance;
    }

    public String toString(){
        StringBuilder s = new StringBuilder();
        for (int[] game : games){
            s.append(Arrays.toString(game)).append("\n");
        }
        return s.toString();
    }

    public String to_a_irs_string(){
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < a_s.length; i++) {
            s.append("[");
            for (int j = 0; j < a_s[i].length; j++) {
                s.append(a_s[i][j]).append(", ");
            }
            s.append("]\n");
        }
        return s.toString();
    }

    public String toSolutionString(){
        int[] sol = new int[games.length];
        for (int i = 0; i < games.length; i++){
            sol[i] = games[i][0] + 1;
        }
        return Arrays.toString(sol);
    }
}
